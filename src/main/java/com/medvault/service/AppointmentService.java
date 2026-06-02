package com.medvault.service;

import com.medvault.dto.request.AppointmentRequest;
import com.medvault.dto.request.RatingRequest;
import com.medvault.dto.request.RejectRequest;
import com.medvault.dto.request.SlotRequest;
import com.medvault.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppointmentService {

    // Doctors
    List<DoctorResponse> getAllDoctors();
    DoctorResponse       getDoctorById(Long id);

    // Slots
    SlotResponse         createSlot(Long adminId, SlotRequest req);
    SlotResponse         updateSlot(Long slotId, SlotRequest req);
    void                 deleteSlot(Long slotId);
    List<SlotResponse>   getAllSlots();
    List<SlotResponse>   getSlotsByDoctorAndDate(Long doctorId, String date);

    // Appointments
    ApiResponse<AppointmentResponse>  bookAppointment(AppointmentRequest req, MultipartFile file);
    List<AppointmentResponse>         getMyAppointments(Long patientId);
    List<AppointmentResponse>         getDoctorAppointments(Long doctorId);
    List<AppointmentResponse>         getPendingAppointments(Long doctorId);
    List<AppointmentResponse>         getAllAppointments();
    ApiResponse<String>               approveAppointment(Long appointmentId);
    ApiResponse<String>               rejectAppointment(Long appointmentId, RejectRequest req);
    ApiResponse<String>               completeAppointment(Long appointmentId);

    /**
     * Patient cancels their own appointment.
     * Time-based refund policy:
     *   > 24 hrs before  → FULL refund
     *   6–24 hrs before  → PARTIAL refund
     *   < 6 hrs before   → NO refund
     * Slot bookedCount is decremented so it becomes bookable again.
     */
    ApiResponse<CancellationResponse> cancelAppointment(Long appointmentId, Long patientId);

    // Ratings
    ApiResponse<String>  submitRating(Long patientId, RatingRequest req);
    List<RatingResponse> getDoctorRatings(Long doctorId);
}