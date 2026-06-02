package com.medvault.service;

import com.medvault.dto.request.DispenseRequest;
import com.medvault.dto.request.MedicineInventoryRequest;
import com.medvault.dto.request.PrescriptionRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.MedicineInventoryResponse;
import com.medvault.dto.response.PrescriptionResponse;

import java.util.List;

public interface PharmacyService {

    ApiResponse<PrescriptionResponse> writePrescription(Long doctorId, PrescriptionRequest req);

    List<PrescriptionResponse> getDoctorPrescriptions(Long doctorId);

    List<PrescriptionResponse> getPatientPrescriptions(Long patientId);

    List<PrescriptionResponse> getPendingPrescriptions();

    List<PrescriptionResponse> getAllPrescriptions();

 
    PrescriptionResponse getPrescriptionById(Long id);

   
    ApiResponse<PrescriptionResponse> dispensePrescription(Long prescriptionId,
                                                            Long pharmacistId,
                                                            DispenseRequest req);

    List<MedicineInventoryResponse> getAllMedicines();

  
    List<MedicineInventoryResponse> getLowStockMedicines();


    ApiResponse<MedicineInventoryResponse> addMedicine(MedicineInventoryRequest req);

    ApiResponse<MedicineInventoryResponse> updateMedicine(Long id, MedicineInventoryRequest req);

    ApiResponse<String> deleteMedicine(Long id);
}