//package com.medvault.service.impl;
//
//import com.medvault.dto.request.AppointmentRequest;
//import com.medvault.dto.request.RatingRequest;
//import com.medvault.dto.request.RejectRequest;
//import com.medvault.dto.request.SlotRequest;
//import com.medvault.dto.response.*;
//import com.medvault.entity.Appointment;
//import com.medvault.entity.DoctorSlot;
//import com.medvault.entity.PersonalDetails;
//import com.medvault.entity.Rating;
//import com.medvault.entity.User;
//import com.medvault.repository.AppointmentRepository;
//import com.medvault.repository.DoctorSlotRepository;
//import com.medvault.repository.PersonalDetailsRepository;
//import com.medvault.repository.RatingRepository;
//import com.medvault.repository.UserRepository;
//import com.medvault.repository.WorkExperienceRepository;
//import com.medvault.service.AppointmentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AppointmentServiceImpl implements AppointmentService {
//
//    private final UserRepository            userRepository;
//    private final PersonalDetailsRepository personalDetailsRepository;
//    private final DoctorSlotRepository      slotRepository;
//    private final AppointmentRepository     appointmentRepository;
//    private final RatingRepository          ratingRepository;
//    private final WorkExperienceRepository   workExperienceRepository;
//
//    @Value("${app.upload.dir:uploads}")
//    private String uploadDir;
//
//    // ═══════════════════════════════════════════════════
//    // DOCTORS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DoctorResponse> getAllDoctors() {
//        return userRepository.findByRole("DOCTOR").stream()
//                .filter(u -> "ACTIVE".equals(u.getStatus()))
//                .map(this::toDoctorResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public DoctorResponse getDoctorById(Long id) {
//        User doc = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
//        return toDoctorResponse(doc);
//    }
//
//    private DoctorResponse toDoctorResponse(User doc) {
//        Double avgRating   = ratingRepository.getAverageRatingByDoctorId(doc.getId());
//        long   totalRatings = ratingRepository.findByDoctorIdOrderByRatedAtDesc(doc.getId()).size();
//
//        List<String> todaySlots = slotRepository
//                .findByDoctorIdAndDateAndIsActiveTrue(doc.getId(), LocalDate.now())
//                .stream()
//                .flatMap(s -> computeSlotTimes(s).stream())
//                .collect(Collectors.toList());
//
//        String name = "Doctor";
//        String spec = ""; String dept = ""; String phone = ""; String exp = "";
//        var pd = personalDetailsRepository.findByUserId(doc.getId()).orElse(null);
//        if (pd != null) {
//            name  = (pd.getFirstName() != null ? pd.getFirstName() : "") +
//                    (pd.getLastName()  != null ? " " + pd.getLastName() : "");
//            phone = pd.getPhone() != null ? pd.getPhone() : "";
//        }
//        var works = workExperienceRepository.findByUserId(doc.getId());
//        if (!works.isEmpty()) {
//            spec = works.get(0).getRole()             != null ? works.get(0).getRole()             : "";
//            dept = works.get(0).getOrganizationName() != null ? works.get(0).getOrganizationName() : "";
//            if (works.get(0).getStartDate() != null) {
//                int years = java.time.LocalDate.now().getYear() - works.get(0).getStartDate().getYear();
//                exp = years + (years == 1 ? " yr" : " yrs");
//            }
//        }
//
//        return DoctorResponse.builder()
//                .id(doc.getId())
//                .name(name.trim())
//                .specialization(spec)
//                .department(dept)
//                .experience(exp)
//                .rating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
//                .totalRatings((int) totalRatings)
//                .available(!todaySlots.isEmpty())
//                .img("")
//                .about("")
//                .patients((int) appointmentRepository
//                        .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doc.getId()).size())
//                .slots(todaySlots)
//                .build();
//    }
//
//    private List<String> computeSlotTimes(DoctorSlot slot) {
//        List<String> times = new java.util.ArrayList<>();
//        try {
//            String[] sp = slot.getStartTime().split(":");
//            String[] ep = slot.getEndTime().split(":");
//            int cur = Integer.parseInt(sp[0]) * 60 + Integer.parseInt(sp[1]);
//            int end = Integer.parseInt(ep[0]) * 60 + Integer.parseInt(ep[1]);
//            int dur = slot.getDuration();
//            while (cur + dur <= end) {
//                int h = cur / 60, m = cur % 60;
//                String ampm = h >= 12 ? "PM" : "AM";
//                int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
//                times.add(String.format("%d:%02d %s", h12, m, ampm));
//                cur += dur;
//            }
//        } catch (Exception e) {
//            log.warn("Failed to compute slot times for slot {}: {}", slot.getId(), e.getMessage());
//        }
//        return times;
//    }
//
//    // ═══════════════════════════════════════════════════
//    // SLOTS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public SlotResponse createSlot(Long adminId, SlotRequest req) {
//        User doctor = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        DoctorSlot slot = DoctorSlot.builder()
//                .doctor(doctor)
//                .date(LocalDate.parse(req.getDate()))
//                .startTime(req.getStartTime())
//                .endTime(req.getEndTime())
//                .duration(req.getDuration())
//                .maxPatients(req.getMaxPatients() != null ? req.getMaxPatients() : 1)
//                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
//                .bookedCount(0)
//                .build();
//
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override
//    @Transactional
//    public SlotResponse updateSlot(Long slotId, SlotRequest req) {
//        DoctorSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//
//        if (req.getDate()       != null) slot.setDate(LocalDate.parse(req.getDate()));
//        if (req.getStartTime()  != null) slot.setStartTime(req.getStartTime());
//        if (req.getEndTime()    != null) slot.setEndTime(req.getEndTime());
//        if (req.getDuration()   != null) slot.setDuration(req.getDuration());
//        if (req.getMaxPatients()!= null) slot.setMaxPatients(req.getMaxPatients());
//        if (req.getIsActive()   != null) slot.setIsActive(req.getIsActive());
//
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override
//    @Transactional
//    public void deleteSlot(Long slotId) {
//        slotRepository.findById(slotId)
//                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//        slotRepository.deleteById(slotId);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SlotResponse> getAllSlots() {
//        return slotRepository.findAll().stream()
//                .map(this::toSlotResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SlotResponse> getSlotsByDoctorAndDate(Long doctorId, String date) {
//        return slotRepository
//                .findByDoctorIdAndDate(doctorId, LocalDate.parse(date))
//                .stream().map(this::toSlotResponse).collect(Collectors.toList());
//    }
//
//    private SlotResponse toSlotResponse(DoctorSlot slot) {
//        String doctorName = "";
//        var pd = personalDetailsRepository.findByUserId(slot.getDoctor().getId()).orElse(null);
//        if (pd != null) doctorName = (pd.getFirstName() + " " + pd.getLastName()).trim();
//
//        return SlotResponse.builder()
//                .id(slot.getId())
//                .doctorId(slot.getDoctor().getId())
//                .doctorName(doctorName)
//                .date(slot.getDate().toString())
//                .startTime(slot.getStartTime())
//                .endTime(slot.getEndTime())
//                .duration(slot.getDuration())
//                .maxPatients(slot.getMaxPatients())
//                .isActive(slot.getIsActive())
//                .bookedCount(slot.getBookedCount())
//                .build();
//    }
//
//    // ═══════════════════════════════════════════════════
//    // APPOINTMENTS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public ApiResponse<AppointmentResponse> bookAppointment(AppointmentRequest req, MultipartFile file) {
//        User patient = userRepository.findById(req.getPatientId())
//                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
//        User doctor = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        String prescriptionPath = null;
//        if (file != null && !file.isEmpty()) {
//            prescriptionPath = saveFile(file, req.getPatientId());
//        }
//
//        Appointment appt = Appointment.builder()
//                .patient(patient)
//                .doctor(doctor)
//                .appointmentDate(req.getAppointmentDate())
//                .appointmentTime(req.getAppointmentTime())
//                .specialization(req.getSpecialization())
//                .department(req.getDepartment())
//                .symptoms(req.getSymptoms())
//                .duration(req.getDuration())
//                .severity(req.getSeverity())
//                .previousVisit(Boolean.TRUE.equals(req.getPreviousVisit()))
//                .notes(req.getNotes())
//                .prescriptionPath(prescriptionPath)
//                .status("PENDING")
//                .build();
//
//        Appointment saved = appointmentRepository.save(appt);
//        log.info("Appointment booked: patient={}, doctor={}, date={}",
//                patient.getId(), doctor.getId(), req.getAppointmentDate());
//
//        return ApiResponse.ok("Appointment booked successfully!", toApptResponse(saved));
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getMyAppointments(Long patientId) {
//        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
//        return appointmentRepository
//                .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getPendingAppointments(Long doctorId) {
//        return appointmentRepository
//                .findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "PENDING")
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getAllAppointments() {
//        return appointmentRepository.findAllByOrderByCreatedAtDesc()
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> approveAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"PENDING".equals(a.getStatus()))
//            return ApiResponse.error("Appointment is not in PENDING state.");
//        a.setStatus("CONFIRMED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " confirmed.");
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> rejectAppointment(Long id, RejectRequest req) {
//        Appointment a = getAppt(id);
//        a.setStatus("REJECTED");
//        a.setRejectionReason(req.getReason());
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " rejected.");
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> completeAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"CONFIRMED".equals(a.getStatus()))
//            return ApiResponse.error("Only CONFIRMED appointments can be completed.");
//        a.setStatus("COMPLETED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " marked as completed.");
//    }
//
//    private Appointment getAppt(Long id) {
//        return appointmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
//    }
//
//    private AppointmentResponse toApptResponse(Appointment a) {
//        String pName = "", pEmail = a.getPatient().getEmail(), pPhone = "";
//        var ppd = personalDetailsRepository.findByUserId(a.getPatient().getId()).orElse(null);
//        if (ppd != null) {
//            pName  = (ppd.getFirstName() + " " + ppd.getLastName()).trim();
//            pPhone = ppd.getPhone() != null ? ppd.getPhone() : "";
//        }
//
//        // ✅ FIX: Strip directory prefix — return only the filename
//        // DB may store full path like "uploads/prescription_1_123_file.png"
//        // We return just "prescription_1_123_file.png" so frontend can build:
//        // http://localhost:8080/uploads/prescription_1_123_file.png
//        String prescriptionUrl = null;
//        if (a.getPrescriptionPath() != null) {
//            prescriptionUrl = Paths.get(a.getPrescriptionPath()).getFileName().toString();
//        }
//
//        return AppointmentResponse.builder()
//                .id(a.getId())
//                .patientId(a.getPatient().getId())
//                .patientName(pName)
//                .patientEmail(pEmail)
//                .patientPhone(pPhone)
//                .doctorId(a.getDoctor().getId())
//                .doctorName(getDoctorName(a.getDoctor().getId()))
//                .specialization(a.getSpecialization())
//                .department(a.getDepartment())
//                .appointmentDate(a.getAppointmentDate())
//                .appointmentTime(a.getAppointmentTime())
//                .symptoms(a.getSymptoms())
//                .duration(a.getDuration())
//                .severity(a.getSeverity())
//                .previousVisit(a.getPreviousVisit())
//                .notes(a.getNotes())
//                .prescriptionUrl(prescriptionUrl)   // ✅ filename only
//                .status(a.getStatus())
//                .rejectionReason(a.getRejectionReason())
//                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
//                .build();
//    }
//
//    // ═══════════════════════════════════════════════════
//    // RATINGS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public ApiResponse<String> submitRating(Long patientId, RatingRequest req) {
//        if (ratingRepository.existsByAppointmentId(req.getAppointmentId())) {
//            return ApiResponse.error("You have already rated this appointment.");
//        }
//
//        Appointment appt = getAppt(req.getAppointmentId());
//        if (!"COMPLETED".equals(appt.getStatus())) {
//            return ApiResponse.error("You can only rate completed appointments.");
//        }
//
//        User patient = userRepository.findById(patientId)
//                .orElseThrow(() -> new RuntimeException("Patient not found"));
//        User doctor  = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found"));
//
//        Rating rating = Rating.builder()
//                .appointment(appt)
//                .patient(patient)
//                .doctor(doctor)
//                .stars(req.getStars())
//                .feedback(req.getFeedback())
//                .build();
//
//        ratingRepository.save(rating);
//        log.info("Rating submitted: patient={}, doctor={}, stars={}", patientId, req.getDoctorId(), req.getStars());
//        return ApiResponse.ok("Rating submitted successfully!");
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<RatingResponse> getDoctorRatings(Long doctorId) {
//        return ratingRepository.findByDoctorIdOrderByRatedAtDesc(doctorId)
//                .stream().map(r -> RatingResponse.builder()
//                        .id(r.getId())
//                        .appointmentId(r.getAppointment().getId())
//                        .doctorId(r.getDoctor().getId())
//                        .patientId(r.getPatient().getId())
//                        .patientName(getPatientName(r.getPatient().getId()))
//                        .stars(r.getStars())
//                        .feedback(r.getFeedback())
//                        .ratedAt(r.getRatedAt() != null ? r.getRatedAt().toString() : "")
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    // ═══════════════════════════════════════════════════
//    // HELPERS
//    // ═══════════════════════════════════════════════════
//
//    private String getDoctorName(Long doctorId) {
//        return personalDetailsRepository.findByUserId(doctorId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
//                .orElse("Doctor");
//    }
//
//    private String getPatientName(Long patientId) {
//        return personalDetailsRepository.findByUserId(patientId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
//                .orElse("Patient");
//    }
//
//    private String saveFile(MultipartFile file, Long patientId) {
//        try {
//            Path dir = Paths.get(uploadDir);
//            Files.createDirectories(dir);
//            String filename = "prescription_" + patientId + "_" +
//                    System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            Path target = dir.resolve(filename);
//            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
//            return filename;  // ✅ FIX: return filename only, not target.toString()
//        } catch (IOException e) {
//            log.error("File upload failed: {}", e.getMessage());
//            return null;
//        }
//    }
//}


//package com.medvault.service.impl;
//
//import com.medvault.dto.request.AppointmentRequest;
//import com.medvault.dto.request.RatingRequest;
//import com.medvault.dto.request.RejectRequest;
//import com.medvault.dto.request.SlotRequest;
//import com.medvault.dto.response.*;
//import com.medvault.entity.Appointment;
//import com.medvault.entity.DoctorSlot;
//import com.medvault.entity.PersonalDetails;
//import com.medvault.entity.Rating;
//import com.medvault.entity.User;
//import com.medvault.repository.AppointmentRepository;
//import com.medvault.repository.DoctorSlotRepository;
//import com.medvault.repository.PersonalDetailsRepository;
//import com.medvault.repository.RatingRepository;
//import com.medvault.repository.UserRepository;
//import com.medvault.repository.WorkExperienceRepository;
//import com.medvault.service.AppointmentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AppointmentServiceImpl implements AppointmentService {
//
//    private final UserRepository            userRepository;
//    private final PersonalDetailsRepository personalDetailsRepository;
//    private final DoctorSlotRepository      slotRepository;
//    private final AppointmentRepository     appointmentRepository;
//    private final RatingRepository          ratingRepository;
//    private final WorkExperienceRepository   workExperienceRepository;
//
//    @Value("${app.upload.dir:uploads}")
//    private String uploadDir;
//
//    // ═══════════════════════════════════════════════════
//    // DOCTORS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DoctorResponse> getAllDoctors() {
//        return userRepository.findByRole("DOCTOR").stream()
//                .filter(u -> "ACTIVE".equals(u.getStatus()))
//                .map(this::toDoctorResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public DoctorResponse getDoctorById(Long id) {
//        User doc = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
//        return toDoctorResponse(doc);
//    }
//
//    private DoctorResponse toDoctorResponse(User doc) {
//        Double avgRating    = ratingRepository.getAverageRatingByDoctorId(doc.getId());
//        long   totalRatings = ratingRepository.findByDoctorIdOrderByRatedAtDesc(doc.getId()).size();
//
//        List<String> todaySlots = slotRepository
//                .findByDoctorIdAndDateAndIsActiveTrue(doc.getId(), LocalDate.now())
//                .stream()
//                .flatMap(s -> computeSlotTimes(s).stream())
//                .collect(Collectors.toList());
//
//        // ── Personal details (name, phone, profile photo) ──────────────
//        String name             = "Doctor";
//        String phone            = "";
//        String profilePhotoPath = "";
//
//        var pd = personalDetailsRepository.findByUserId(doc.getId()).orElse(null);
//        if (pd != null) {
//            String first = pd.getFirstName() != null ? pd.getFirstName() : "";
//            String last  = pd.getLastName()  != null ? pd.getLastName()  : "";
//            name  = ("Dr. " + first + (last.isBlank() ? "" : " " + last)).trim();
//            phone = pd.getPhone() != null ? pd.getPhone() : "";
//
//            // ✅ THE FIX: use the actual saved profile photo path
//            if (pd.getProfilePhotoPath() != null && !pd.getProfilePhotoPath().isBlank()) {
//                profilePhotoPath = pd.getProfilePhotoPath();
//            }
//        }
//
//        // ── Work experience (specialization, department, experience) ────
//        String spec = ""; String dept = ""; String exp = "";
//        var works = workExperienceRepository.findByUserId(doc.getId());
//        if (!works.isEmpty()) {
//            spec = works.get(0).getRole()             != null ? works.get(0).getRole()             : "";
//            dept = works.get(0).getOrganizationName() != null ? works.get(0).getOrganizationName() : "";
//            if (works.get(0).getStartDate() != null) {
//                int years = java.time.LocalDate.now().getYear() - works.get(0).getStartDate().getYear();
//                exp = years + (years == 1 ? " yr" : " yrs");
//            }
//        }
//
//        return DoctorResponse.builder()
//                .id(doc.getId())
//                .name(name)
//                .specialization(spec)
//                .department(dept)
//                .experience(exp)
//                .rating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
//                .totalRatings((int) totalRatings)
//                .available(!todaySlots.isEmpty())
//                .img(profilePhotoPath)           // ✅ FIXED: was hardcoded ""
//                .profilePhotoPath(profilePhotoPath) // ✅ also exposed as profilePhotoPath
//                .about("")
//                .patients((int) appointmentRepository
//                        .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doc.getId()).size())
//                .slots(todaySlots)
//                .build();
//    }
//
//    private List<String> computeSlotTimes(DoctorSlot slot) {
//        List<String> times = new java.util.ArrayList<>();
//        try {
//            String[] sp = slot.getStartTime().split(":");
//            String[] ep = slot.getEndTime().split(":");
//            int cur = Integer.parseInt(sp[0]) * 60 + Integer.parseInt(sp[1]);
//            int end = Integer.parseInt(ep[0]) * 60 + Integer.parseInt(ep[1]);
//            int dur = slot.getDuration();
//            while (cur + dur <= end) {
//                int h = cur / 60, m = cur % 60;
//                String ampm = h >= 12 ? "PM" : "AM";
//                int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
//                times.add(String.format("%d:%02d %s", h12, m, ampm));
//                cur += dur;
//            }
//        } catch (Exception e) {
//            log.warn("Failed to compute slot times for slot {}: {}", slot.getId(), e.getMessage());
//        }
//        return times;
//    }
//
//    // ═══════════════════════════════════════════════════
//    // SLOTS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public SlotResponse createSlot(Long adminId, SlotRequest req) {
//        User doctor = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        DoctorSlot slot = DoctorSlot.builder()
//                .doctor(doctor)
//                .date(LocalDate.parse(req.getDate()))
//                .startTime(req.getStartTime())
//                .endTime(req.getEndTime())
//                .duration(req.getDuration())
//                .maxPatients(req.getMaxPatients() != null ? req.getMaxPatients() : 1)
//                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
//                .bookedCount(0)
//                .build();
//
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override
//    @Transactional
//    public SlotResponse updateSlot(Long slotId, SlotRequest req) {
//        DoctorSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//
//        if (req.getDate()       != null) slot.setDate(LocalDate.parse(req.getDate()));
//        if (req.getStartTime()  != null) slot.setStartTime(req.getStartTime());
//        if (req.getEndTime()    != null) slot.setEndTime(req.getEndTime());
//        if (req.getDuration()   != null) slot.setDuration(req.getDuration());
//        if (req.getMaxPatients()!= null) slot.setMaxPatients(req.getMaxPatients());
//        if (req.getIsActive()   != null) slot.setIsActive(req.getIsActive());
//
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override
//    @Transactional
//    public void deleteSlot(Long slotId) {
//        slotRepository.findById(slotId)
//                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//        slotRepository.deleteById(slotId);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SlotResponse> getAllSlots() {
//        return slotRepository.findAll().stream()
//                .map(this::toSlotResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SlotResponse> getSlotsByDoctorAndDate(Long doctorId, String date) {
//        return slotRepository
//                .findByDoctorIdAndDate(doctorId, LocalDate.parse(date))
//                .stream().map(this::toSlotResponse).collect(Collectors.toList());
//    }
//
//    private SlotResponse toSlotResponse(DoctorSlot slot) {
//        String doctorName = "";
//        var pd = personalDetailsRepository.findByUserId(slot.getDoctor().getId()).orElse(null);
//        if (pd != null) doctorName = (pd.getFirstName() + " " + pd.getLastName()).trim();
//
//        return SlotResponse.builder()
//                .id(slot.getId())
//                .doctorId(slot.getDoctor().getId())
//                .doctorName(doctorName)
//                .date(slot.getDate().toString())
//                .startTime(slot.getStartTime())
//                .endTime(slot.getEndTime())
//                .duration(slot.getDuration())
//                .maxPatients(slot.getMaxPatients())
//                .isActive(slot.getIsActive())
//                .bookedCount(slot.getBookedCount())
//                .build();
//    }
//
//    // ═══════════════════════════════════════════════════
//    // APPOINTMENTS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public ApiResponse<AppointmentResponse> bookAppointment(AppointmentRequest req, MultipartFile file) {
//        User patient = userRepository.findById(req.getPatientId())
//                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
//        User doctor = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        String prescriptionPath = null;
//        if (file != null && !file.isEmpty()) {
//            prescriptionPath = saveFile(file, req.getPatientId());
//        }
//
//        Appointment appt = Appointment.builder()
//                .patient(patient)
//                .doctor(doctor)
//                .appointmentDate(req.getAppointmentDate())
//                .appointmentTime(req.getAppointmentTime())
//                .specialization(req.getSpecialization())
//                .department(req.getDepartment())
//                .symptoms(req.getSymptoms())
//                .duration(req.getDuration())
//                .severity(req.getSeverity())
//                .previousVisit(Boolean.TRUE.equals(req.getPreviousVisit()))
//                .notes(req.getNotes())
//                .prescriptionPath(prescriptionPath)
//                .status("PENDING")
//                .build();
//
//        Appointment saved = appointmentRepository.save(appt);
//        log.info("Appointment booked: patient={}, doctor={}, date={}",
//                patient.getId(), doctor.getId(), req.getAppointmentDate());
//
//        return ApiResponse.ok("Appointment booked successfully!", toApptResponse(saved));
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getMyAppointments(Long patientId) {
//        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
//        return appointmentRepository
//                .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getPendingAppointments(Long doctorId) {
//        return appointmentRepository
//                .findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "PENDING")
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<AppointmentResponse> getAllAppointments() {
//        return appointmentRepository.findAllByOrderByCreatedAtDesc()
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> approveAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"PENDING".equals(a.getStatus()))
//            return ApiResponse.error("Appointment is not in PENDING state.");
//        a.setStatus("CONFIRMED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " confirmed.");
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> rejectAppointment(Long id, RejectRequest req) {
//        Appointment a = getAppt(id);
//        a.setStatus("REJECTED");
//        a.setRejectionReason(req.getReason());
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " rejected.");
//    }
//
//    @Override
//    @Transactional
//    public ApiResponse<String> completeAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"CONFIRMED".equals(a.getStatus()))
//            return ApiResponse.error("Only CONFIRMED appointments can be completed.");
//        a.setStatus("COMPLETED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " marked as completed.");
//    }
//
//    private Appointment getAppt(Long id) {
//        return appointmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
//    }
//
//    private AppointmentResponse toApptResponse(Appointment a) {
//        String pName = "", pEmail = a.getPatient().getEmail(), pPhone = "";
//        var ppd = personalDetailsRepository.findByUserId(a.getPatient().getId()).orElse(null);
//        if (ppd != null) {
//            pName  = (ppd.getFirstName() + " " + ppd.getLastName()).trim();
//            pPhone = ppd.getPhone() != null ? ppd.getPhone() : "";
//        }
//
//        // ✅ FIX: Strip directory prefix — return only the filename
//        // DB may store full path like "uploads/prescription_1_123_file.png"
//        // We return just "prescription_1_123_file.png" so frontend can build:
//        // http://localhost:8080/uploads/prescription_1_123_file.png
//        String prescriptionUrl = null;
//        if (a.getPrescriptionPath() != null) {
//            prescriptionUrl = Paths.get(a.getPrescriptionPath()).getFileName().toString();
//        }
//
//        return AppointmentResponse.builder()
//                .id(a.getId())
//                .patientId(a.getPatient().getId())
//                .patientName(pName)
//                .patientEmail(pEmail)
//                .patientPhone(pPhone)
//                .doctorId(a.getDoctor().getId())
//                .doctorName(getDoctorName(a.getDoctor().getId()))
//                .specialization(a.getSpecialization())
//                .department(a.getDepartment())
//                .appointmentDate(a.getAppointmentDate())
//                .appointmentTime(a.getAppointmentTime())
//                .symptoms(a.getSymptoms())
//                .duration(a.getDuration())
//                .severity(a.getSeverity())
//                .previousVisit(a.getPreviousVisit())
//                .notes(a.getNotes())
//                .prescriptionUrl(prescriptionUrl)   // ✅ filename only
//                .status(a.getStatus())
//                .rejectionReason(a.getRejectionReason())
//                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
//                .build();
//    }
//
//    // ═══════════════════════════════════════════════════
//    // RATINGS
//    // ═══════════════════════════════════════════════════
//
//    @Override
//    @Transactional
//    public ApiResponse<String> submitRating(Long patientId, RatingRequest req) {
//        if (ratingRepository.existsByAppointmentId(req.getAppointmentId())) {
//            return ApiResponse.error("You have already rated this appointment.");
//        }
//
//        Appointment appt = getAppt(req.getAppointmentId());
//        if (!"COMPLETED".equals(appt.getStatus())) {
//            return ApiResponse.error("You can only rate completed appointments.");
//        }
//
//        User patient = userRepository.findById(patientId)
//                .orElseThrow(() -> new RuntimeException("Patient not found"));
//        User doctor  = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found"));
//
//        Rating rating = Rating.builder()
//                .appointment(appt)
//                .patient(patient)
//                .doctor(doctor)
//                .stars(req.getStars())
//                .feedback(req.getFeedback())
//                .build();
//
//        ratingRepository.save(rating);
//        log.info("Rating submitted: patient={}, doctor={}, stars={}", patientId, req.getDoctorId(), req.getStars());
//        return ApiResponse.ok("Rating submitted successfully!");
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<RatingResponse> getDoctorRatings(Long doctorId) {
//        return ratingRepository.findByDoctorIdOrderByRatedAtDesc(doctorId)
//                .stream().map(r -> RatingResponse.builder()
//                        .id(r.getId())
//                        .appointmentId(r.getAppointment().getId())
//                        .doctorId(r.getDoctor().getId())
//                        .patientId(r.getPatient().getId())
//                        .patientName(getPatientName(r.getPatient().getId()))
//                        .stars(r.getStars())
//                        .feedback(r.getFeedback())
//                        .ratedAt(r.getRatedAt() != null ? r.getRatedAt().toString() : "")
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    // ═══════════════════════════════════════════════════
//    // HELPERS
//    // ═══════════════════════════════════════════════════
//
//    private String getDoctorName(Long doctorId) {
//        return personalDetailsRepository.findByUserId(doctorId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
//                .orElse("Doctor");
//    }
//
//    private String getPatientName(Long patientId) {
//        return personalDetailsRepository.findByUserId(patientId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim())
//                .orElse("Patient");
//    }
//
//    private String saveFile(MultipartFile file, Long patientId) {
//        try {
//            Path dir = Paths.get(uploadDir);
//            Files.createDirectories(dir);
//            String filename = "prescription_" + patientId + "_" +
//                    System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            Path target = dir.resolve(filename);
//            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
//            return filename;  // ✅ FIX: return filename only, not target.toString()
//        } catch (IOException e) {
//            log.error("File upload failed: {}", e.getMessage());
//            return null;
//        }
//    }
//}

//package com.medvault.service.impl;
//
//import com.medvault.dto.request.AppointmentRequest;
//import com.medvault.dto.request.RatingRequest;
//import com.medvault.dto.request.RejectRequest;
//import com.medvault.dto.request.SlotRequest;
//import com.medvault.dto.response.*;
//import com.medvault.entity.Appointment;
//import com.medvault.entity.DoctorSlot;
//import com.medvault.entity.PersonalDetails;
//import com.medvault.entity.Rating;
//import com.medvault.entity.User;
//import com.medvault.repository.AppointmentRepository;
//import com.medvault.repository.DoctorSlotRepository;
//import com.medvault.repository.PersonalDetailsRepository;
//import com.medvault.repository.RatingRepository;
//import com.medvault.repository.UserRepository;
//import com.medvault.repository.WorkExperienceRepository;
//import com.medvault.service.AppointmentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AppointmentServiceImpl implements AppointmentService {
//
//    private final UserRepository             userRepository;
//    private final PersonalDetailsRepository  personalDetailsRepository;
//    private final DoctorSlotRepository       slotRepository;
//    private final AppointmentRepository      appointmentRepository;
//    private final RatingRepository           ratingRepository;
//    private final WorkExperienceRepository   workExperienceRepository;
//
//    @Value("${app.upload.dir:uploads}")
//    private String uploadDir;
//
//    // DOCTORS ====================================================
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DoctorResponse> getAllDoctors() {
//        return userRepository.findByRole("DOCTOR").stream()
//                .filter(u -> "ACTIVE".equals(u.getStatus()))
//                .map(this::toDoctorResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public DoctorResponse getDoctorById(Long id) {
//        User doc = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
//        return toDoctorResponse(doc);
//    }
//
//    private DoctorResponse toDoctorResponse(User doc) {
//        Double avgRating    = ratingRepository.getAverageRatingByDoctorId(doc.getId());
//        long   totalRatings = ratingRepository.findByDoctorIdOrderByRatedAtDesc(doc.getId()).size();
//
//        List<String> todaySlots = slotRepository
//                .findByDoctorIdAndDateAndIsActiveTrue(doc.getId(), LocalDate.now())
//                .stream().flatMap(s -> computeSlotTimes(s).stream())
//                .collect(Collectors.toList());
//
//        String name = "Doctor"; String spec = ""; String dept = ""; String exp = "";
//        var pd = personalDetailsRepository.findByUserId(doc.getId()).orElse(null);
//        if (pd != null) {
//            name = (pd.getFirstName() != null ? pd.getFirstName() : "")
//                 + (pd.getLastName()  != null ? " " + pd.getLastName() : "");
//        }
//
//        var works = workExperienceRepository.findByUserId(doc.getId());
//        if (!works.isEmpty()) {
//            spec = works.get(0).getRole()             != null ? works.get(0).getRole()             : "";
//            dept = works.get(0).getOrganizationName() != null ? works.get(0).getOrganizationName() : "";
//            if (works.get(0).getStartDate() != null) {
//                int years = LocalDate.now().getYear() - works.get(0).getStartDate().getYear();
//                exp = years + (years == 1 ? " yr" : " yrs");
//            }
//        }
//
//        // ✅ Use uploaded profile photo from personal_details
//        String photoPath = (pd != null && pd.getProfilePhotoPath() != null) ? pd.getProfilePhotoPath() : "";
//
//        return DoctorResponse.builder()
//                .id(doc.getId()).name(name.trim()).specialization(spec).department(dept).experience(exp)
//                .rating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
//                .totalRatings((int) totalRatings).available(!todaySlots.isEmpty())
//                .img(photoPath)   // ✅ was img("") — now real path
//                .about("")
//                .patients((int) appointmentRepository
//                        .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doc.getId()).size())
//                .slots(todaySlots).build();
//    }
//
//    // SLOTS ======================================================
//
//    @Override @Transactional
//    public SlotResponse createSlot(Long adminId, SlotRequest req) {
//        User doctor = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//        DoctorSlot slot = DoctorSlot.builder()
//                .doctor(doctor).date(LocalDate.parse(req.getDate()))
//                .startTime(req.getStartTime()).endTime(req.getEndTime())
//                .duration(req.getDuration())
//                .maxPatients(req.getMaxPatients() != null ? req.getMaxPatients() : 1)
//                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
//                .bookedCount(0).build();
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override @Transactional
//    public SlotResponse updateSlot(Long slotId, SlotRequest req) {
//        DoctorSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//        if (req.getDate()       != null) slot.setDate(LocalDate.parse(req.getDate()));
//        if (req.getStartTime()  != null) slot.setStartTime(req.getStartTime());
//        if (req.getEndTime()    != null) slot.setEndTime(req.getEndTime());
//        if (req.getDuration()   != null) slot.setDuration(req.getDuration());
//        if (req.getMaxPatients()!= null) slot.setMaxPatients(req.getMaxPatients());
//        if (req.getIsActive()   != null) slot.setIsActive(req.getIsActive());
//        return toSlotResponse(slotRepository.save(slot));
//    }
//
//    @Override @Transactional
//    public void deleteSlot(Long slotId) {
//        slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
//        slotRepository.deleteById(slotId);
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<SlotResponse> getAllSlots() {
//        return slotRepository.findAll().stream().map(this::toSlotResponse).collect(Collectors.toList());
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<SlotResponse> getSlotsByDoctorAndDate(Long doctorId, String date) {
//        return slotRepository.findByDoctorIdAndDate(doctorId, LocalDate.parse(date))
//                .stream().map(this::toSlotResponse).collect(Collectors.toList());
//    }
//
//    private SlotResponse toSlotResponse(DoctorSlot slot) {
//        String doctorName = "";
//        var pd = personalDetailsRepository.findByUserId(slot.getDoctor().getId()).orElse(null);
//        if (pd != null) doctorName = (pd.getFirstName() + " " + pd.getLastName()).trim();
//        return SlotResponse.builder()
//                .id(slot.getId()).doctorId(slot.getDoctor().getId()).doctorName(doctorName)
//                .date(slot.getDate().toString()).startTime(slot.getStartTime()).endTime(slot.getEndTime())
//                .duration(slot.getDuration()).maxPatients(slot.getMaxPatients())
//                .isActive(slot.getIsActive()).bookedCount(slot.getBookedCount()).build();
//    }
//
//    // APPOINTMENTS ===============================================
//
//    @Override @Transactional
//    public ApiResponse<AppointmentResponse> bookAppointment(AppointmentRequest req, MultipartFile file) {
//        User patient = userRepository.findById(req.getPatientId())
//                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
//        User doctor  = userRepository.findById(req.getDoctorId())
//                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
//
//        String prescriptionPath = (file != null && !file.isEmpty()) ? saveFile(file, req.getPatientId()) : null;
//
//        Appointment appt = Appointment.builder()
//                .patient(patient).doctor(doctor)
//                .appointmentDate(req.getAppointmentDate()).appointmentTime(req.getAppointmentTime())
//                .specialization(req.getSpecialization()).department(req.getDepartment())
//                .symptoms(req.getSymptoms()).duration(req.getDuration()).severity(req.getSeverity())
//                .previousVisit(Boolean.TRUE.equals(req.getPreviousVisit()))
//                .notes(req.getNotes()).prescriptionPath(prescriptionPath).status("PENDING").build();
//
//        Appointment saved = appointmentRepository.save(appt);
//        log.info("Appointment booked: patient={}, doctor={}, date={}", patient.getId(), doctor.getId(), req.getAppointmentDate());
//        return ApiResponse.ok("Appointment booked successfully!", toApptResponse(saved));
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<AppointmentResponse> getMyAppointments(Long patientId) {
//        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
//        return appointmentRepository.findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<AppointmentResponse> getPendingAppointments(Long doctorId) {
//        return appointmentRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "PENDING")
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<AppointmentResponse> getAllAppointments() {
//        return appointmentRepository.findAllByOrderByCreatedAtDesc()
//                .stream().map(this::toApptResponse).collect(Collectors.toList());
//    }
//
//    @Override @Transactional
//    public ApiResponse<String> approveAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"PENDING".equals(a.getStatus())) return ApiResponse.error("Appointment is not in PENDING state.");
//        a.setStatus("CONFIRMED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " confirmed.");
//    }
//
//    @Override @Transactional
//    public ApiResponse<String> rejectAppointment(Long id, RejectRequest req) {
//        Appointment a = getAppt(id);
//        a.setStatus("REJECTED");
//        a.setRejectionReason(req.getReason());
//        appointmentRepository.save(a);
//        // Free the slot
//        if (a.getSlot() != null && a.getSlot().getBookedCount() > 0) {
//            a.getSlot().setBookedCount(a.getSlot().getBookedCount() - 1);
//            slotRepository.save(a.getSlot());
//        }
//        return ApiResponse.ok("Appointment #" + id + " rejected.");
//    }
//
//    @Override @Transactional
//    public ApiResponse<String> completeAppointment(Long id) {
//        Appointment a = getAppt(id);
//        if (!"CONFIRMED".equals(a.getStatus())) return ApiResponse.error("Only CONFIRMED appointments can be completed.");
//        a.setStatus("COMPLETED");
//        appointmentRepository.save(a);
//        return ApiResponse.ok("Appointment #" + id + " marked as completed.");
//    }
//
//    // CANCELLATION — Time-based refund policy =====================
//    //
//    //   > 24 hrs  →  FULL refund
//    //   6-24 hrs  →  PARTIAL refund (50%)
//    //   < 6 hrs   →  NO refund
//    //
//    //   Slot is freed → becomes bookable again
//    //   Doctor + Patient notified (log; extend with email later)
//
//    @Override @Transactional
//    public ApiResponse<CancellationResponse> cancelAppointment(Long appointmentId, Long patientId) {
//        Appointment a = getAppt(appointmentId);
//
//        // Guard: only the booking patient can cancel
//        if (!a.getPatient().getId().equals(patientId)) {
//            return ApiResponse.error("You can only cancel your own appointments.");
//        }
//        // Guard: only PENDING or CONFIRMED can be cancelled
//        String st = a.getStatus();
//        if ("CANCELLED".equals(st) || "COMPLETED".equals(st)) {
//            return ApiResponse.error("Appointment cannot be cancelled (status: " + st + ").");
//        }
//
//        long hoursUntil = calculateHoursUntilAppointment(a);
//
//        String refundType, refundMessage;
//        if (hoursUntil > 24) {
//            refundType    = "FULL";
//            refundMessage = "Full refund applied — cancelled more than 24 hours in advance.";
//        } else if (hoursUntil >= 6) {
//            refundType    = "PARTIAL";
//            refundMessage = "Partial refund (50%) applied — cancelled 6–24 hours before appointment.";
//        } else {
//            refundType    = "NONE";
//            refundMessage = "No refund — cancelled less than 6 hours before scheduled time.";
//        }
//
//        // Cancel appointment
//        a.setStatus("CANCELLED");
//        a.setRejectionReason("Cancelled by patient. Refund: " + refundType);
//        appointmentRepository.save(a);
//
//        // Free slot so it is bookable again
//        boolean slotFreed = false;
//        if (a.getSlot() != null) {
//            DoctorSlot slot = a.getSlot();
//            if (slot.getBookedCount() > 0) {
//                slot.setBookedCount(slot.getBookedCount() - 1);
//                slotRepository.save(slot);
//                slotFreed = true;
//            }
//        }
//
//        String doctorName  = getDoctorName(a.getDoctor().getId());
//        String patientName = getPatientName(patientId);
//
//        log.info("CANCELLATION — Appt #{} | Patient: {} | Doctor: {} | Date: {} {} | Hours: {} | Refund: {} | SlotFreed: {}",
//                appointmentId, patientName, doctorName,
//                a.getAppointmentDate(), a.getAppointmentTime(), hoursUntil, refundType, slotFreed);
//        log.info("NOTIFY DOCTOR ({}) — Appointment #{} on {} at {} cancelled by {}.",
//                doctorName, appointmentId, a.getAppointmentDate(), a.getAppointmentTime(), patientName);
//        log.info("NOTIFY PATIENT ({}) — Appointment #{} cancelled. {}",
//                patientName, appointmentId, refundMessage);
//
//        return ApiResponse.ok(
//            "Appointment #" + appointmentId + " cancelled. " + refundMessage,
//            CancellationResponse.builder()
//                .appointmentId(appointmentId).status("CANCELLED")
//                .refundType(refundType).refundMessage(refundMessage)
//                .hoursBeforeAppointment(hoursUntil).slotFreed(slotFreed)
//                .build()
//        );
//    }
//
//    private long calculateHoursUntilAppointment(Appointment a) {
//        try {
//            LocalDate date = LocalDate.parse(a.getAppointmentDate());
//            LocalTime time = parseTime(a.getAppointmentTime());
//            return Math.max(0, ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.of(date, time)));
//        } catch (Exception e) {
//            log.warn("Could not parse appointment datetime for #{}: {}", a.getId(), e.getMessage());
//            return 0;
//        }
//    }
//
//    private LocalTime parseTime(String timeStr) {
//        if (timeStr == null) return LocalTime.MIDNIGHT;
//        timeStr = timeStr.trim();
//        try { return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a")); } catch (Exception ignored) {}
//        try { return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a")); } catch (Exception ignored) {}
//        try { return LocalTime.parse(timeStr); } catch (Exception ignored) {}
//        return LocalTime.MIDNIGHT;
//    }
//
//    // RATINGS ====================================================
//
//    @Override @Transactional
//    public ApiResponse<String> submitRating(Long patientId, RatingRequest req) {
//        if (ratingRepository.existsByAppointmentId(req.getAppointmentId()))
//            return ApiResponse.error("You have already rated this appointment.");
//        Appointment appt = getAppt(req.getAppointmentId());
//        if (!"COMPLETED".equals(appt.getStatus()))
//            return ApiResponse.error("You can only rate completed appointments.");
//        User patient = userRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));
//        User doctor  = userRepository.findById(req.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
//        ratingRepository.save(Rating.builder()
//                .appointment(appt).patient(patient).doctor(doctor)
//                .stars(req.getStars()).feedback(req.getFeedback()).build());
//        log.info("Rating submitted: patient={}, doctor={}, stars={}", patientId, req.getDoctorId(), req.getStars());
//        return ApiResponse.ok("Rating submitted successfully!");
//    }
//
//    @Override @Transactional(readOnly = true)
//    public List<RatingResponse> getDoctorRatings(Long doctorId) {
//        return ratingRepository.findByDoctorIdOrderByRatedAtDesc(doctorId).stream()
//                .map(r -> RatingResponse.builder()
//                        .id(r.getId()).appointmentId(r.getAppointment().getId())
//                        .doctorId(r.getDoctor().getId()).patientId(r.getPatient().getId())
//                        .patientName(getPatientName(r.getPatient().getId()))
//                        .stars(r.getStars()).feedback(r.getFeedback())
//                        .ratedAt(r.getRatedAt() != null ? r.getRatedAt().toString() : "")
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    // HELPERS ====================================================
//
//    private Appointment getAppt(Long id) {
//        return appointmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
//    }
//
//    private AppointmentResponse toApptResponse(Appointment a) {
//        String pName = "", pEmail = a.getPatient().getEmail(), pPhone = "";
//        var ppd = personalDetailsRepository.findByUserId(a.getPatient().getId()).orElse(null);
//        if (ppd != null) {
//            pName  = (ppd.getFirstName() + " " + ppd.getLastName()).trim();
//            pPhone = ppd.getPhone() != null ? ppd.getPhone() : "";
//        }
//        String prescriptionUrl = a.getPrescriptionPath() != null
//                ? Paths.get(a.getPrescriptionPath()).getFileName().toString() : null;
//        return AppointmentResponse.builder()
//                .id(a.getId()).patientId(a.getPatient().getId()).patientName(pName)
//                .patientEmail(pEmail).patientPhone(pPhone).doctorId(a.getDoctor().getId())
//                .doctorName(getDoctorName(a.getDoctor().getId()))
//                .specialization(a.getSpecialization()).department(a.getDepartment())
//                .appointmentDate(a.getAppointmentDate()).appointmentTime(a.getAppointmentTime())
//                .symptoms(a.getSymptoms()).duration(a.getDuration()).severity(a.getSeverity())
//                .previousVisit(a.getPreviousVisit()).notes(a.getNotes())
//                .prescriptionUrl(prescriptionUrl).status(a.getStatus())
//                .rejectionReason(a.getRejectionReason())
//                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
//                .build();
//    }
//
//    public List<String> computeSlotTimes(DoctorSlot slot) {
//        List<String> times = new java.util.ArrayList<>();
//        try {
//            String[] sp = slot.getStartTime().split(":");
//            String[] ep = slot.getEndTime().split(":");
//            int cur = Integer.parseInt(sp[0]) * 60 + Integer.parseInt(sp[1]);
//            int end = Integer.parseInt(ep[0]) * 60 + Integer.parseInt(ep[1]);
//            int dur = slot.getDuration() != null ? slot.getDuration() : 30;
//            while (cur + dur <= end) {
//                int h = cur / 60; int m = cur % 60;
//                String ampm = h >= 12 ? "PM" : "AM";
//                int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
//                times.add(String.format("%d:%02d %s", h12, m, ampm));
//                cur += dur;
//            }
//        } catch (Exception e) {
//            log.warn("computeSlotTimes failed for slot {}: {}", slot.getId(), e.getMessage());
//        }
//        return times;
//    }
//
//    private String getDoctorName(Long doctorId) {
//        return personalDetailsRepository.findByUserId(doctorId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim()).orElse("Doctor");
//    }
//
//    private String getPatientName(Long patientId) {
//        return personalDetailsRepository.findByUserId(patientId)
//                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim()).orElse("Patient");
//    }
//
//    private String saveFile(MultipartFile file, Long patientId) {
//        try {
//            Path dir = Paths.get(uploadDir);
//            Files.createDirectories(dir);
//            String filename = "prescription_" + patientId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
//            return filename;
//        } catch (IOException e) {
//            log.error("File upload failed: {}", e.getMessage());
//            return null;
//        }
//    }
//}

package com.medvault.service.impl;

import com.medvault.dto.request.AppointmentRequest;
import com.medvault.dto.request.RatingRequest;
import com.medvault.dto.request.RejectRequest;
import com.medvault.dto.request.SlotRequest;
import com.medvault.dto.response.*;
import com.medvault.entity.Appointment;
import com.medvault.entity.DoctorSlot;
import com.medvault.entity.PersonalDetails;
import com.medvault.entity.Rating;
import com.medvault.entity.User;
import com.medvault.repository.AppointmentRepository;
import com.medvault.repository.DoctorSlotRepository;
import com.medvault.repository.PersonalDetailsRepository;
import com.medvault.repository.RatingRepository;
import com.medvault.repository.UserRepository;
import com.medvault.repository.WorkExperienceRepository;
import com.medvault.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final UserRepository             userRepository;
    private final PersonalDetailsRepository  personalDetailsRepository;
    private final DoctorSlotRepository       slotRepository;
    private final AppointmentRepository      appointmentRepository;
    private final RatingRepository           ratingRepository;
    private final WorkExperienceRepository   workExperienceRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // DOCTORS ====================================================

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return userRepository.findByRole("DOCTOR").stream()
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .map(this::toDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        // ✅ FIX: guard against id = 0 or null before hitting the DB
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID: " + id);
        }
        User doc = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + id));
        return toDoctorResponse(doc);
    }

    private DoctorResponse toDoctorResponse(User doc) {
        Double avgRating    = ratingRepository.getAverageRatingByDoctorId(doc.getId());
        long   totalRatings = ratingRepository.findByDoctorIdOrderByRatedAtDesc(doc.getId()).size();

        List<String> todaySlots = slotRepository
                .findByDoctorIdAndDateAndIsActiveTrue(doc.getId(), LocalDate.now())
                .stream().flatMap(s -> computeSlotTimes(s).stream())
                .collect(Collectors.toList());

        String name = "Doctor"; String spec = ""; String dept = ""; String exp = "";
        var pd = personalDetailsRepository.findByUserId(doc.getId()).orElse(null);
        if (pd != null) {
            name = (pd.getFirstName() != null ? pd.getFirstName() : "")
                 + (pd.getLastName()  != null ? " " + pd.getLastName() : "");
        }

        var works = workExperienceRepository.findByUserId(doc.getId());
        if (!works.isEmpty()) {
            spec = works.get(0).getRole()             != null ? works.get(0).getRole()             : "";
            dept = works.get(0).getOrganizationName() != null ? works.get(0).getOrganizationName() : "";
            if (works.get(0).getStartDate() != null) {
                int years = LocalDate.now().getYear() - works.get(0).getStartDate().getYear();
                exp = years + (years == 1 ? " yr" : " yrs");
            }
        }

        String photoPath = (pd != null && pd.getProfilePhotoPath() != null) ? pd.getProfilePhotoPath() : "";

        return DoctorResponse.builder()
                .id(doc.getId()).name(name.trim()).specialization(spec).department(dept).experience(exp)
                .rating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalRatings((int) totalRatings).available(!todaySlots.isEmpty())
                .img(photoPath)
                .about("")
                .patients((int) appointmentRepository
                        .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doc.getId()).size())
                .slots(todaySlots).build();
    }

    // SLOTS ======================================================

    @Override
    @Transactional
    public SlotResponse createSlot(Long adminId, SlotRequest req) {
        // ✅ FIX: guard against doctorId = 0 or null
        if (req.getDoctorId() == null || req.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID: " + req.getDoctorId());
        }
        User doctor = userRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));
        DoctorSlot slot = DoctorSlot.builder()
                .doctor(doctor).date(LocalDate.parse(req.getDate()))
                .startTime(req.getStartTime()).endTime(req.getEndTime())
                .duration(req.getDuration())
                .maxPatients(req.getMaxPatients() != null ? req.getMaxPatients() : 1)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .bookedCount(0).build();
        return toSlotResponse(slotRepository.save(slot));
    }

    @Override
    @Transactional
    public SlotResponse updateSlot(Long slotId, SlotRequest req) {
        // ✅ FIX: guard against timestamp-as-id (any id > max reasonable DB id is suspicious,
        //         but a simple null/<=0 check catches the most common frontend mistake)
        if (slotId == null || slotId <= 0) {
            throw new IllegalArgumentException("Invalid slot ID: " + slotId);
        }
        DoctorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
        if (req.getDate()        != null) slot.setDate(LocalDate.parse(req.getDate()));
        if (req.getStartTime()   != null) slot.setStartTime(req.getStartTime());
        if (req.getEndTime()     != null) slot.setEndTime(req.getEndTime());
        if (req.getDuration()    != null) slot.setDuration(req.getDuration());
        if (req.getMaxPatients() != null) slot.setMaxPatients(req.getMaxPatients());
        if (req.getIsActive()    != null) slot.setIsActive(req.getIsActive());
        return toSlotResponse(slotRepository.save(slot));
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId) {
        // ✅ FIX: guard against timestamp-as-id
        if (slotId == null || slotId <= 0) {
            throw new IllegalArgumentException("Invalid slot ID: " + slotId);
        }
        slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));
        slotRepository.deleteById(slotId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots() {
        return slotRepository.findAll().stream().map(this::toSlotResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByDoctorAndDate(Long doctorId, String date) {
        return slotRepository.findByDoctorIdAndDate(doctorId, LocalDate.parse(date))
                .stream().map(this::toSlotResponse).collect(Collectors.toList());
    }

    private SlotResponse toSlotResponse(DoctorSlot slot) {
        String doctorName = "";
        var pd = personalDetailsRepository.findByUserId(slot.getDoctor().getId()).orElse(null);
        if (pd != null) doctorName = (pd.getFirstName() + " " + pd.getLastName()).trim();
        return SlotResponse.builder()
                .id(slot.getId()).doctorId(slot.getDoctor().getId()).doctorName(doctorName)
                .date(slot.getDate().toString()).startTime(slot.getStartTime()).endTime(slot.getEndTime())
                .duration(slot.getDuration()).maxPatients(slot.getMaxPatients())
                .isActive(slot.getIsActive()).bookedCount(slot.getBookedCount()).build();
    }

    // APPOINTMENTS ===============================================

    @Override
    @Transactional
    public ApiResponse<AppointmentResponse> bookAppointment(AppointmentRequest req, MultipartFile file) {
        // ✅ FIX: validate patientId and doctorId before any DB call
        if (req.getPatientId() == null || req.getPatientId() <= 0) {
            throw new IllegalArgumentException("Invalid patient ID: " + req.getPatientId());
        }
        if (req.getDoctorId() == null || req.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID: " + req.getDoctorId());
        }

        User patient = userRepository.findById(req.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + req.getPatientId()));
        User doctor  = userRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + req.getDoctorId()));

        String prescriptionPath = (file != null && !file.isEmpty()) ? saveFile(file, req.getPatientId()) : null;

        Appointment appt = Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDate(req.getAppointmentDate()).appointmentTime(req.getAppointmentTime())
                .specialization(req.getSpecialization()).department(req.getDepartment())
                .symptoms(req.getSymptoms()).duration(req.getDuration()).severity(req.getSeverity())
                .previousVisit(Boolean.TRUE.equals(req.getPreviousVisit()))
                .notes(req.getNotes()).prescriptionPath(prescriptionPath).status("PENDING").build();

        Appointment saved = appointmentRepository.save(appt);
        log.info("Appointment booked: patient={}, doctor={}, date={}", patient.getId(), doctor.getId(), req.getAppointmentDate());
        return ApiResponse.ok("Appointment booked successfully!", toApptResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toApptResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
                .stream().map(this::toApptResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPendingAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "PENDING")
                .stream().map(this::toApptResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toApptResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<String> approveAppointment(Long id) {
        Appointment a = getAppt(id);
        if (!"PENDING".equals(a.getStatus())) return ApiResponse.error("Appointment is not in PENDING state.");
        a.setStatus("CONFIRMED");
        appointmentRepository.save(a);
        return ApiResponse.ok("Appointment #" + id + " confirmed.");
    }

    @Override
    @Transactional
    public ApiResponse<String> rejectAppointment(Long id, RejectRequest req) {
        Appointment a = getAppt(id);
        a.setStatus("REJECTED");
        a.setRejectionReason(req.getReason());
        appointmentRepository.save(a);
        if (a.getSlot() != null && a.getSlot().getBookedCount() > 0) {
            a.getSlot().setBookedCount(a.getSlot().getBookedCount() - 1);
            slotRepository.save(a.getSlot());
        }
        return ApiResponse.ok("Appointment #" + id + " rejected.");
    }

    @Override
    @Transactional
    public ApiResponse<String> completeAppointment(Long id) {
        Appointment a = getAppt(id);
        if (!"CONFIRMED".equals(a.getStatus())) return ApiResponse.error("Only CONFIRMED appointments can be completed.");
        a.setStatus("COMPLETED");
        appointmentRepository.save(a);
        return ApiResponse.ok("Appointment #" + id + " marked as completed.");
    }

    // CANCELLATION ===============================================

    @Override
    @Transactional
    public ApiResponse<CancellationResponse> cancelAppointment(Long appointmentId, Long patientId) {
        Appointment a = getAppt(appointmentId);

        if (!a.getPatient().getId().equals(patientId)) {
            return ApiResponse.error("You can only cancel your own appointments.");
        }
        String st = a.getStatus();
        if ("CANCELLED".equals(st) || "COMPLETED".equals(st)) {
            return ApiResponse.error("Appointment cannot be cancelled (status: " + st + ").");
        }

        long hoursUntil = calculateHoursUntilAppointment(a);

        String refundType, refundMessage;
        if (hoursUntil > 24) {
            refundType    = "FULL";
            refundMessage = "Full refund applied — cancelled more than 24 hours in advance.";
        } else if (hoursUntil >= 6) {
            refundType    = "PARTIAL";
            refundMessage = "Partial refund (50%) applied — cancelled 6–24 hours before appointment.";
        } else {
            refundType    = "NONE";
            refundMessage = "No refund — cancelled less than 6 hours before scheduled time.";
        }

        a.setStatus("CANCELLED");
        a.setRejectionReason("Cancelled by patient. Refund: " + refundType);
        appointmentRepository.save(a);

        boolean slotFreed = false;
        if (a.getSlot() != null) {
            DoctorSlot slot = a.getSlot();
            if (slot.getBookedCount() > 0) {
                slot.setBookedCount(slot.getBookedCount() - 1);
                slotRepository.save(slot);
                slotFreed = true;
            }
        }

        String doctorName  = getDoctorName(a.getDoctor().getId());
        String patientName = getPatientName(patientId);

        log.info("CANCELLATION — Appt #{} | Patient: {} | Doctor: {} | Date: {} {} | Hours: {} | Refund: {} | SlotFreed: {}",
                appointmentId, patientName, doctorName,
                a.getAppointmentDate(), a.getAppointmentTime(), hoursUntil, refundType, slotFreed);

        return ApiResponse.ok(
            "Appointment #" + appointmentId + " cancelled. " + refundMessage,
            CancellationResponse.builder()
                .appointmentId(appointmentId).status("CANCELLED")
                .refundType(refundType).refundMessage(refundMessage)
                .hoursBeforeAppointment(hoursUntil).slotFreed(slotFreed)
                .build()
        );
    }

    private long calculateHoursUntilAppointment(Appointment a) {
        try {
            LocalDate date = LocalDate.parse(a.getAppointmentDate());
            LocalTime time = parseTime(a.getAppointmentTime());
            return Math.max(0, ChronoUnit.HOURS.between(LocalDateTime.now(), LocalDateTime.of(date, time)));
        } catch (Exception e) {
            log.warn("Could not parse appointment datetime for #{}: {}", a.getId(), e.getMessage());
            return 0;
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null) return LocalTime.MIDNIGHT;
        timeStr = timeStr.trim();
        try { return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a")); } catch (Exception ignored) {}
        try { return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a")); } catch (Exception ignored) {}
        try { return LocalTime.parse(timeStr); } catch (Exception ignored) {}
        return LocalTime.MIDNIGHT;
    }

    // RATINGS ====================================================

    @Override
    @Transactional
    public ApiResponse<String> submitRating(Long patientId, RatingRequest req) {
        if (ratingRepository.existsByAppointmentId(req.getAppointmentId()))
            return ApiResponse.error("You have already rated this appointment.");
        Appointment appt = getAppt(req.getAppointmentId());
        if (!"COMPLETED".equals(appt.getStatus()))
            return ApiResponse.error("You can only rate completed appointments.");
        User patient = userRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));
        User doctor  = userRepository.findById(req.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        ratingRepository.save(Rating.builder()
                .appointment(appt).patient(patient).doctor(doctor)
                .stars(req.getStars()).feedback(req.getFeedback()).build());
        log.info("Rating submitted: patient={}, doctor={}, stars={}", patientId, req.getDoctorId(), req.getStars());
        return ApiResponse.ok("Rating submitted successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getDoctorRatings(Long doctorId) {
        return ratingRepository.findByDoctorIdOrderByRatedAtDesc(doctorId).stream()
                .map(r -> RatingResponse.builder()
                        .id(r.getId()).appointmentId(r.getAppointment().getId())
                        .doctorId(r.getDoctor().getId()).patientId(r.getPatient().getId())
                        .patientName(getPatientName(r.getPatient().getId()))
                        .stars(r.getStars()).feedback(r.getFeedback())
                        .ratedAt(r.getRatedAt() != null ? r.getRatedAt().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }

    // HELPERS ====================================================

    private Appointment getAppt(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
    }

    private AppointmentResponse toApptResponse(Appointment a) {
        String pName = "", pEmail = a.getPatient().getEmail(), pPhone = "";
        var ppd = personalDetailsRepository.findByUserId(a.getPatient().getId()).orElse(null);
        if (ppd != null) {
            pName  = (ppd.getFirstName() + " " + ppd.getLastName()).trim();
            pPhone = ppd.getPhone() != null ? ppd.getPhone() : "";
        }
        String prescriptionUrl = a.getPrescriptionPath() != null
                ? Paths.get(a.getPrescriptionPath()).getFileName().toString() : null;
        return AppointmentResponse.builder()
                .id(a.getId()).patientId(a.getPatient().getId()).patientName(pName)
                .patientEmail(pEmail).patientPhone(pPhone).doctorId(a.getDoctor().getId())
                .doctorName(getDoctorName(a.getDoctor().getId()))
                .specialization(a.getSpecialization()).department(a.getDepartment())
                .appointmentDate(a.getAppointmentDate()).appointmentTime(a.getAppointmentTime())
                .symptoms(a.getSymptoms()).duration(a.getDuration()).severity(a.getSeverity())
                .previousVisit(a.getPreviousVisit()).notes(a.getNotes())
                .prescriptionUrl(prescriptionUrl).status(a.getStatus())
                .rejectionReason(a.getRejectionReason())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
                .build();
    }

    public List<String> computeSlotTimes(DoctorSlot slot) {
        List<String> times = new java.util.ArrayList<>();
        try {
            String[] sp = slot.getStartTime().split(":");
            String[] ep = slot.getEndTime().split(":");
            int cur = Integer.parseInt(sp[0]) * 60 + Integer.parseInt(sp[1]);
            int end = Integer.parseInt(ep[0]) * 60 + Integer.parseInt(ep[1]);
            int dur = slot.getDuration() != null ? slot.getDuration() : 30;
            while (cur + dur <= end) {
                int h = cur / 60; int m = cur % 60;
                String ampm = h >= 12 ? "PM" : "AM";
                int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
                times.add(String.format("%d:%02d %s", h12, m, ampm));
                cur += dur;
            }
        } catch (Exception e) {
            log.warn("computeSlotTimes failed for slot {}: {}", slot.getId(), e.getMessage());
        }
        return times;
    }

    private String getDoctorName(Long doctorId) {
        return personalDetailsRepository.findByUserId(doctorId)
                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim()).orElse("Doctor");
    }

    private String getPatientName(Long patientId) {
        return personalDetailsRepository.findByUserId(patientId)
                .map(pd -> (pd.getFirstName() + " " + pd.getLastName()).trim()).orElse("Patient");
    }

    private String saveFile(MultipartFile file, Long patientId) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            String filename = "prescription_" + patientId + "_" + System.currentTimeMillis()
                            + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            return null;
        }
    }
}

