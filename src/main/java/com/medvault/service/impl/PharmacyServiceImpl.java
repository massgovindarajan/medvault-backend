package com.medvault.service.impl;

import com.medvault.dto.request.DispenseRequest;
import com.medvault.dto.request.MedicineInventoryRequest;
import com.medvault.dto.request.PrescriptionRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.MedicineInventoryResponse;
import com.medvault.dto.response.PrescriptionItemResponse;
import com.medvault.dto.response.PrescriptionResponse;
import com.medvault.entity.*;
import com.medvault.repository.*;
import com.medvault.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final PrescriptionRepository       prescriptionRepository;
    private final PrescriptionItemRepository   prescriptionItemRepository;
    private final MedicineInventoryRepository  medicineRepository;
    private final AppointmentRepository        appointmentRepository;
    private final UserRepository               userRepository;
    private final PersonalDetailsRepository    personalDetailsRepository;

    // ═══════════════════════════════════════════
    // PRESCRIPTIONS — DOCTOR SIDE
    // ═══════════════════════════════════════════

    @Override
    @Transactional
    public ApiResponse<PrescriptionResponse> writePrescription(Long doctorId, PrescriptionRequest req) {

        // Validate appointment exists
        Appointment appt = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + req.getAppointmentId()));

        // Validate doctor owns this appointment
        if (!appt.getDoctor().getId().equals(doctorId)) {
            return ApiResponse.error("You can only write prescriptions for your own appointments.");
        }

        // Check not already prescribed
        if (prescriptionRepository.existsByAppointmentId(req.getAppointmentId())) {
            return ApiResponse.error("A prescription already exists for this appointment.");
        }

        User doctor  = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = appt.getPatient();

        // Create prescription
        Prescription rx = Prescription.builder()
                .appointment(appt)
                .doctor(doctor)
                .patient(patient)
                .diagnosis(req.getDiagnosis())
                .notes(req.getNotes())
                .status("PENDING")
                .build();

        Prescription saved = prescriptionRepository.save(rx);

        // Create items
        if (req.getItems() != null) {
            List<PrescriptionItem> items = req.getItems().stream().map(i ->
                PrescriptionItem.builder()
                    .prescription(saved)
                    .medicineName(i.getMedicineName())
                    .dosage(i.getDosage())
                    .frequency(i.getFrequency())
                    .duration(i.getDuration())
                    .instructions(i.getInstructions())
                    .quantity(i.getQuantity() != null ? i.getQuantity() : 1)
                    .status("PENDING")
                    .build()
            ).collect(Collectors.toList());
            prescriptionItemRepository.saveAll(items);
            saved.setItems(items);
        }

        log.info("Prescription #{} written by doctor={} for patient={}",
                saved.getId(), doctorId, patient.getId());

        return ApiResponse.ok("Prescription written successfully!", toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getDoctorPrescriptions(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPendingPrescriptions() {
        return prescriptionRepository.findByStatusOrderByCreatedAtDesc("PENDING")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAllPrescriptions() {
        return prescriptionRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        return toResponse(prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id)));
    }

    // ═══════════════════════════════════════════
    // DISPENSE — PHARMACIST SIDE
    // ═══════════════════════════════════════════

    /**
     * Pharmacist dispenses a prescription:
     * 1. Mark each PENDING item as DISPENSED (or OUT_OF_STOCK if no stock)
     * 2. Decrement medicine inventory for each dispensed item
     * 3. Mark prescription as DISPENSED
     * 4. Record pharmacist + timestamp
     */
    @Override
    @Transactional
    public ApiResponse<PrescriptionResponse> dispensePrescription(Long prescriptionId,
                                                                   Long pharmacistId,
                                                                   DispenseRequest req) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));

        if ("DISPENSED".equals(rx.getStatus())) {
            return ApiResponse.error("Prescription #" + prescriptionId + " has already been dispensed.");
        }

        User pharmacist = userRepository.findById(pharmacistId)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        // Load items fresh
        List<PrescriptionItem> items = prescriptionItemRepository.findByPrescriptionId(prescriptionId);
        int dispensedCount   = 0;
        int outOfStockCount  = 0;

        for (PrescriptionItem item : items) {
            if (!"PENDING".equals(item.getStatus())) continue;

            // Try to find medicine in inventory (case-insensitive)
            var inventoryOpt = medicineRepository
                    .findByMedicineNameIgnoreCase(item.getMedicineName());

            if (inventoryOpt.isPresent()) {
                MedicineInventory inv = inventoryOpt.get();
                int needed = item.getQuantity() != null ? item.getQuantity() : 1;

                if (inv.getStockQty() >= needed) {
                    // Sufficient stock — dispense
                    inv.setStockQty(inv.getStockQty() - needed);
                    medicineRepository.save(inv);
                    item.setStatus("DISPENSED");
                    dispensedCount++;

                    if (inv.isLowStock()) {
                        log.warn("LOW STOCK ALERT: {} — only {} {} remaining",
                                inv.getMedicineName(), inv.getStockQty(), inv.getUnit());
                    }
                } else {
                    // Insufficient stock
                    item.setStatus("OUT_OF_STOCK");
                    outOfStockCount++;
                    log.warn("OUT OF STOCK: {} — needed={} available={}",
                            item.getMedicineName(), needed, inv.getStockQty());
                }
            } else {
                // Medicine not in inventory system — dispense manually (mark dispensed)
                item.setStatus("DISPENSED");
                dispensedCount++;
                log.warn("Medicine not in inventory: {} — marked dispensed manually",
                        item.getMedicineName());
            }
            prescriptionItemRepository.save(item);
        }

        // Mark prescription DISPENSED if at least one item was dispensed
        rx.setStatus(outOfStockCount > 0 && dispensedCount == 0 ? "PENDING" : "DISPENSED");
        rx.setDispensedAt(LocalDateTime.now());
        rx.setDispensedBy(pharmacist);
        prescriptionRepository.save(rx);

        String msg = String.format(
            "Prescription #%d processed — %d dispensed, %d out of stock.",
            prescriptionId, dispensedCount, outOfStockCount
        );
        log.info(msg);

        return ApiResponse.ok(msg, toResponse(rx));
    }

    // ═══════════════════════════════════════════
    // MEDICINE INVENTORY
    // ═══════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<MedicineInventoryResponse> getAllMedicines() {
        return medicineRepository.findAllByOrderByMedicineNameAsc()
                .stream().map(this::toMedResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineInventoryResponse> getLowStockMedicines() {
        return medicineRepository.findByStockQtyLessThanEqualAndIsActiveTrue(10)
                .stream()
                .filter(m -> m.getStockQty() <= m.getLowStockThreshold())
                .map(this::toMedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<MedicineInventoryResponse> addMedicine(MedicineInventoryRequest req) {
        if (medicineRepository.findByMedicineNameIgnoreCase(req.getMedicineName()).isPresent()) {
            return ApiResponse.error("Medicine already exists: " + req.getMedicineName());
        }
        MedicineInventory med = MedicineInventory.builder()
                .medicineName(req.getMedicineName())
                .genericName(req.getGenericName())
                .category(req.getCategory())
                .stockQty(req.getStockQty() != null ? req.getStockQty() : 0)
                .unit(req.getUnit() != null ? req.getUnit() : "Tablet")
                .lowStockThreshold(req.getLowStockThreshold() != null ? req.getLowStockThreshold() : 10)
                .unitPrice(req.getUnitPrice())
                .manufacturer(req.getManufacturer())
                .isActive(true)
                .build();
        return ApiResponse.ok("Medicine added to inventory!", toMedResponse(medicineRepository.save(med)));
    }

    @Override
    @Transactional
    public ApiResponse<MedicineInventoryResponse> updateMedicine(Long id, MedicineInventoryRequest req) {
        MedicineInventory med = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        if (req.getMedicineName()      != null) med.setMedicineName(req.getMedicineName());
        if (req.getGenericName()       != null) med.setGenericName(req.getGenericName());
        if (req.getCategory()          != null) med.setCategory(req.getCategory());
        if (req.getStockQty()          != null) med.setStockQty(req.getStockQty());
        if (req.getUnit()              != null) med.setUnit(req.getUnit());
        if (req.getLowStockThreshold() != null) med.setLowStockThreshold(req.getLowStockThreshold());
        if (req.getUnitPrice()         != null) med.setUnitPrice(req.getUnitPrice());
        if (req.getManufacturer()      != null) med.setManufacturer(req.getManufacturer());
        return ApiResponse.ok("Medicine updated!", toMedResponse(medicineRepository.save(med)));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteMedicine(Long id) {
        MedicineInventory med = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        med.setIsActive(false);
        medicineRepository.save(med);
        return ApiResponse.ok("Medicine deactivated.");
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private PrescriptionResponse toResponse(Prescription rx) {
        String doctorName  = getName(rx.getDoctor().getId());
        String patientName = getName(rx.getPatient().getId());
        String pharmName   = rx.getDispensedBy() != null ? getName(rx.getDispensedBy().getId()) : null;

        List<PrescriptionItemResponse> items = prescriptionItemRepository
                .findByPrescriptionId(rx.getId())
                .stream().map(i -> PrescriptionItemResponse.builder()
                        .id(i.getId())
                        .medicineName(i.getMedicineName())
                        .dosage(i.getDosage())
                        .frequency(i.getFrequency())
                        .duration(i.getDuration())
                        .instructions(i.getInstructions())
                        .quantity(i.getQuantity())
                        .status(i.getStatus())
                        .build())
                .collect(Collectors.toList());

        return PrescriptionResponse.builder()
                .id(rx.getId())
                .appointmentId(rx.getAppointment().getId())
                .doctorId(rx.getDoctor().getId())
                .doctorName(doctorName)
                .patientId(rx.getPatient().getId())
                .patientName(patientName)
                .patientEmail(rx.getPatient().getEmail())
                .diagnosis(rx.getDiagnosis())
                .notes(rx.getNotes())
                .status(rx.getStatus())
                .createdAt(rx.getCreatedAt() != null ? rx.getCreatedAt().toString() : "")
                .dispensedAt(rx.getDispensedAt() != null ? rx.getDispensedAt().toString() : null)
                .dispensedByName(pharmName)
                .items(items)
                .build();
    }

    private MedicineInventoryResponse toMedResponse(MedicineInventory m) {
        return MedicineInventoryResponse.builder()
                .id(m.getId())
                .medicineName(m.getMedicineName())
                .genericName(m.getGenericName())
                .category(m.getCategory())
                .stockQty(m.getStockQty())
                .unit(m.getUnit())
                .lowStockThreshold(m.getLowStockThreshold())
                .unitPrice(m.getUnitPrice())
                .manufacturer(m.getManufacturer())
                .isActive(m.getIsActive())
                .isLowStock(m.isLowStock())
                .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : "")
                .build();
    }

    private String getName(Long userId) {
        return personalDetailsRepository.findByUserId(userId)
                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
                .orElse("Unknown");
    }
}