package com.medvault.dto.response;

import lombok.*;
import java.util.List;

/**
 * DTO returned by GET /api/doctors and GET /api/doctors/{id}
 *
 * CHANGE LOG:
 *  - Added `profilePhotoPath` field  (mirrors `img`)
 *  - `img` now populated from PersonalDetails.profilePhotoPath instead of ""
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long         id;
    private String       name;
    private String       specialization;
    private String       department;
    private String       experience;
    private double       rating;
    private int          totalRatings;
    private boolean      available;
    

    /**
     * Relative path of the uploaded profile photo, e.g.
     *   "profiles/18/79151bdf-2700-44dc-b34c-c7c8f7fa8a90.png"
     *
     * Frontend builds the full URL:
     *   http://localhost:8080/uploads/ + img
     *
     * ✅ FIXED: was always "" — now set from PersonalDetails.profilePhotoPath
     */
    private String       img;

    /**
     * Same value as img — exposed under both field names so the frontend
     * can check whichever field name it prefers (img OR profilePhotoPath).
     */
    private String       profilePhotoPath;

    private String       about;
    private int          patients;
    private List<String> slots;
}