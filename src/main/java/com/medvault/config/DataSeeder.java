//package com.medvault.config;
//
//import com.medvault.entity.PersonalDetails;
//import com.medvault.entity.User;
//import com.medvault.entity.WorkExperience;
//import com.medvault.repository.PersonalDetailsRepository;
//import com.medvault.repository.UserRepository;
//import com.medvault.repository.WorkExperienceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserRepository            userRepository;
//    private final PersonalDetailsRepository personalDetailsRepository;
//    private final WorkExperienceRepository  workExperienceRepository;
//    private final PasswordEncoder           passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        seedAdmin("admin@medvault.com", "Admin@123");
//        seedDoctor("dr.smith@medvault.com",   "Doctor@123", "James",   "Smith",   "Cardiologist",    "Cardiology",      2010);
//        seedDoctor("dr.patel@medvault.com",   "Doctor@123", "Priya",   "Patel",   "Dermatologist",   "Dermatology",     2015);
//        seedDoctor("dr.jones@medvault.com",   "Doctor@123", "Emily",   "Jones",   "Neurologist",     "Neurology",       2012);
//        seedDoctor("dr.kumar@medvault.com",   "Doctor@123", "Arjun",   "Kumar",   "Pediatrician",    "Pediatrics",      2018);
//        seedDoctor("dr.chen@medvault.com",    "Doctor@123", "Wei",     "Chen",    "Orthopedist",     "Orthopedics",     2013);
//    }
//
//    private void seedAdmin(String email, String rawPassword) {
//        if (!userRepository.existsByEmail(email)) {
//            User admin = User.builder()
//                    .email(email)
//                    .password(passwordEncoder.encode(rawPassword))
//                    .role("ADMIN")
//                    .status("ACTIVE")
//                    .passwordResetRequired(false)
//                    .l1Approved(true)
//                    .l2Approved(true)
//                    .build();
//            userRepository.save(admin);
//            log.info("✅ Admin created: {} / {}", email, rawPassword);
//        } else {
//            log.info("ℹ️  Admin exists: {}", email);
//        }
//    }
//
//    private void seedDoctor(String email, String rawPassword,
//                             String firstName, String lastName,
//                             String specialization, String department, int startYear) {
//        if (!userRepository.existsByEmail(email)) {
//            User doctor = User.builder()
//                    .email(email)
//                    .password(passwordEncoder.encode(rawPassword))
//                    .role("DOCTOR")
//                    .status("ACTIVE")
//                    .passwordResetRequired(false)
//                    .l1Approved(true)
//                    .l2Approved(true)
//                    .build();
//            doctor = userRepository.save(doctor);
//
//            PersonalDetails pd = PersonalDetails.builder()
//                    .user(doctor)
//                    .firstName(firstName)
//                    .lastName(lastName)
//                    .gender("OTHER")
//                    .phone("9000000000")
//                    .build();
//            personalDetailsRepository.save(pd);
//
//            WorkExperience we = WorkExperience.builder()
//                    .user(doctor)
//                    .role(specialization)
//                    .organizationName(department)
//                    .startDate(LocalDate.of(startYear, 1, 1))
//                    .build();
//            workExperienceRepository.save(we);
//
//            log.info("✅ Doctor created: {} — {}", email, specialization);
//        } else {
//            log.info("ℹ️  Doctor exists: {}", email);
//        }
//    }
//}

//package com.medvault.config;
//
//import com.medvault.entity.PersonalDetails;
//import com.medvault.entity.User;
//import com.medvault.entity.WorkExperience;
//import com.medvault.repository.PersonalDetailsRepository;
//import com.medvault.repository.UserRepository;
//import com.medvault.repository.WorkExperienceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserRepository            userRepository;
//    private final PersonalDetailsRepository personalDetailsRepository;
//    private final WorkExperienceRepository  workExperienceRepository;
//    private final PasswordEncoder           passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        // ✅ Admin1 — sees L1 Approvals only
//        seedAdmin("admin@medvault.com",  "Admin@123");
//
//        // ✅ Admin2 — sees L2 Approvals only (email contains 'admin2')
//        seedAdmin("admin2@medvault.com", "Admin@123");
//
//        seedDoctor("dr.smith@medvault.com", "Doctor@123", "James", "Smith", "Cardiologist",  "Cardiology",  2010);
//        seedDoctor("dr.patel@medvault.com", "Doctor@123", "Priya", "Patel", "Dermatologist", "Dermatology", 2015);
//        seedDoctor("dr.jones@medvault.com", "Doctor@123", "Emily", "Jones", "Neurologist",   "Neurology",   2012);
//        seedDoctor("dr.kumar@medvault.com", "Doctor@123", "Arjun", "Kumar", "Pediatrician",  "Pediatrics",  2018);
//        seedDoctor("dr.chen@medvault.com",  "Doctor@123", "Wei",   "Chen",  "Orthopedist",   "Orthopedics", 2013);
//    }
//
//    private void seedAdmin(String email, String rawPassword) {
//        if (!userRepository.existsByEmail(email)) {
//            User admin = User.builder()
//                    .email(email)
//                    .password(passwordEncoder.encode(rawPassword))
//                    .role("ADMIN")
//                    .status("ACTIVE")
//                    .passwordResetRequired(false)
//                    .l1Approved(true)
//                    .l2Approved(true)
//                    .build();
//            userRepository.save(admin);
//            log.info("✅ Admin created: {} / {}", email, rawPassword);
//        } else {
//            log.info("ℹ️  Admin exists: {}", email);
//        }
//    }
//
//    private void seedDoctor(String email, String rawPassword,
//                             String firstName, String lastName,
//                             String specialization, String department, int startYear) {
//        if (!userRepository.existsByEmail(email)) {
//            User doctor = User.builder()
//                    .email(email)
//                    .password(passwordEncoder.encode(rawPassword))
//                    .role("DOCTOR")
//                    .status("ACTIVE")
//                    .passwordResetRequired(false)
//                    .l1Approved(true)
//                    .l2Approved(true)
//                    .build();
//            doctor = userRepository.save(doctor);
//
//            PersonalDetails pd = PersonalDetails.builder()
//                    .user(doctor)
//                    .firstName(firstName)
//                    .lastName(lastName)
//                    .gender("OTHER")
//                    .phone("9000000000")
//                    .build();
//            personalDetailsRepository.save(pd);
//
//            WorkExperience we = WorkExperience.builder()
//                    .user(doctor)
//                    .role(specialization)
//                    .organizationName(department)
//                    .startDate(LocalDate.of(startYear, 1, 1))
//                    .build();
//            workExperienceRepository.save(we);
//
//            log.info("✅ Doctor created: {} — {}", email, specialization);
//        } else {
//            log.info("ℹ️  Doctor exists: {}", email);
//        }
//    }
//}
//




package com.medvault.config;

import com.medvault.entity.PersonalDetails;
import com.medvault.entity.User;
import com.medvault.entity.WorkExperience;
import com.medvault.repository.PersonalDetailsRepository;
import com.medvault.repository.UserRepository;
import com.medvault.repository.WorkExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ✅ Admins
        seedAdmin("admin@medvault.com", "Admin@123");
        seedAdmin("admin2@medvault.com", "Admin@123");

        // ✅ Doctors (TOTAL = 8)

        seedDoctor("dr.smith@medvault.com", "Doctor@123",
                "James", "Smith", "Cardiologist", "Cardiology", 2010);

        seedDoctor("dr.patel@medvault.com", "Doctor@123",
                "Priya", "Patel", "Dermatologist", "Dermatology", 2015);

        seedDoctor("dr.jones@medvault.com", "Doctor@123",
                "Emily", "Jones", "Neurologist", "Neurology", 2012);

        seedDoctor("dr.kumar@medvault.com", "Doctor@123",
                "Arjun", "Kumar", "Pediatrician", "Pediatrics", 2018);

        seedDoctor("dr.chen@medvault.com", "Doctor@123",
                "Wei", "Chen", "Orthopedist", "Orthopedics", 2013);

        // ➕ Added to match frontend (8 doctors)

        seedDoctor("dr.green@medvault.com", "Doctor@123",
                "Brooklyn", "Green", "Ophthalmologist", "Ophthalmology", 2017);

        seedDoctor("dr.henry@medvault.com", "Doctor@123",
                "Courtney", "Henry", "Gynecologist", "Gynecology", 2014);

        seedDoctor("dr.alexander@medvault.com", "Doctor@123",
                "Leslie", "Alexander", "Psychiatrist", "Psychiatry", 2016);
    }

    // ─────────────────────────────────────────────
    // ADMIN SEED
    // ─────────────────────────────────────────────
    private void seedAdmin(String email, String rawPassword) {
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role("ADMIN")
                    .status("ACTIVE")
                    .passwordResetRequired(false)
                    .l1Approved(true)
                    .l2Approved(true)
                    .build();

            userRepository.save(admin);
            log.info("✅ Admin created: {} / {}", email, rawPassword);
        } else {
            log.info("ℹ️ Admin exists: {}", email);
        }
    }

    // ─────────────────────────────────────────────
    // DOCTOR SEED
    // ─────────────────────────────────────────────
    private void seedDoctor(String email, String rawPassword,
                            String firstName, String lastName,
                            String specialization, String department,
                            int startYear) {

        if (!userRepository.existsByEmail(email)) {

            // 1️⃣ Create user
            User doctor = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role("DOCTOR")
                    .status("ACTIVE")
                    .passwordResetRequired(false)
                    .l1Approved(true)
                    .l2Approved(true)
                    .build();

            doctor = userRepository.save(doctor);

            // 2️⃣ Personal details
            PersonalDetails pd = PersonalDetails.builder()
                    .user(doctor)
                    .firstName(firstName)
                    .lastName(lastName)
                    .gender("OTHER")
                    .phone("9000000000")
                    .build();

            personalDetailsRepository.save(pd);

            // 3️⃣ Work experience
            WorkExperience we = WorkExperience.builder()
                    .user(doctor)
                    .role(specialization)
                    .organizationName(department)
                    .startDate(LocalDate.of(startYear, 1, 1))
                    .build();

            workExperienceRepository.save(we);

            log.info("✅ Doctor created: {} — {}", email, specialization);

        } else {
            log.info("ℹ️ Doctor exists: {}", email);
        }
    }
}









