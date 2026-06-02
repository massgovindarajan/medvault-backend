//package com.medvault.controller;
//
//import com.medvault.dto.request.QueueStatusRequest;
//import com.medvault.entity.Patient;
//import com.medvault.entity.Queue;
//import com.medvault.repository.PatientRepository;
//import com.medvault.repository.QueueRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/queue")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class QueueController {
//
//    private final QueueRepository queueRepository;
//    private final PatientRepository patientRepository;
//
//    // Generate Token
//    @PostMapping("/token/{patientId}")
//    public ResponseEntity<?> generateToken(
//            @PathVariable Long patientId) {
//
//        try {
//
//            Patient patient = patientRepository.findById(patientId)
//                    .orElseThrow(() ->
//                            new RuntimeException("Patient not found"));
//
//            String token = "T-" + String.format("%03d",
//                    queueRepository.count() + 1);
//
//            Queue queue = Queue.builder()
//                    .tokenNumber(token)
//                    .patient(patient)
//                    .status(Queue.QueueStatus.WAITING)
//                    .checkInTime(LocalDateTime.now())
//                    .build();
//
//            Queue savedQueue = queueRepository.save(queue);
//
//            return ResponseEntity.ok(savedQueue);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                    .body(e.getMessage());
//        }
//    }
//
//    // Get All Queue Entries
//    @GetMapping
//    public ResponseEntity<?> getAllQueue() {
//
//        try {
//            List<Queue> queues = queueRepository.findAll();
//            return ResponseEntity.ok(queues);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                    .body(e.getMessage());
//        }
//    }
//
//    // Get Queue By Id
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getQueueById(
//            @PathVariable Long id) {
//
//        try {
//
//            Queue queue = queueRepository.findById(id)
//                    .orElseThrow(() ->
//                            new RuntimeException("Queue not found"));
//
//            return ResponseEntity.ok(queue);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                    .body(e.getMessage());
//        }
//    }
//
//    // Update Queue Status
//    @PutMapping("/{id}/status")
//    public ResponseEntity<?> updateStatus(
//            @PathVariable Long id,
//            @RequestBody QueueStatusRequest request) {
//
//        try {
//
//            System.out.println("========== QUEUE STATUS UPDATE ==========");
//            System.out.println("Queue ID = " + id);
//            System.out.println("Status = " + request.getStatus());
//
//            Queue queue = queueRepository.findById(id)
//                    .orElseThrow(() ->
//                            new RuntimeException("Queue not found"));
//
//            queue.setStatus(
//                    Queue.QueueStatus.valueOf(
//                            request.getStatus().toUpperCase()
//                    )
//            );
//
//            Queue updatedQueue = queueRepository.save(queue);
//
//            return ResponseEntity.ok(updatedQueue);
//
//        } catch (Exception e) {
//
//            System.out.println("========== ERROR ==========");
//            e.printStackTrace();
//
//            return ResponseEntity.internalServerError()
//                    .body(e.getMessage());
//        }
//    }
//
//    // Delete Queue Entry
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteQueue(
//            @PathVariable Long id) {
//
//        try {
//
//            if (!queueRepository.existsById(id)) {
//                return ResponseEntity.notFound().build();
//            }
//
//            queueRepository.deleteById(id);
//
//            return ResponseEntity.ok(
//                    "Queue entry deleted successfully"
//            );
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            return ResponseEntity.internalServerError()
//                    .body(e.getMessage());
//        }
//    }
//}
package com.medvault.controller;

import com.medvault.dto.request.QueueStatusRequest;
import com.medvault.entity.Patient;
import com.medvault.entity.Queue;
import com.medvault.repository.PatientRepository;
import com.medvault.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueRepository queueRepository;
    private final PatientRepository patientRepository;

    // ── Generate Token ────────────────────────────────────────────────────────

    @PostMapping("/token/{patientId}")
    public ResponseEntity<?> generateToken(@PathVariable Long patientId) {
        try {

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            // Use the highest token ever issued, not the row count.
            // This prevents duplicates when rows have been deleted or completed.
            String lastToken  = queueRepository.findMaxTokenNumber();
            int    nextNumber = 1;

            if (lastToken != null && lastToken.matches("T-\\d+")) {
                nextNumber = Integer.parseInt(lastToken.substring(2)) + 1;
            }

            String token = "T-" + String.format("%03d", nextNumber);

            Queue queue = Queue.builder()
                    .tokenNumber(token)
                    .patient(patient)
                    .status(Queue.QueueStatus.WAITING)
                    .checkInTime(LocalDateTime.now())
                    .build();

            Queue savedQueue = queueRepository.save(queue);
            return ResponseEntity.ok(savedQueue);

        } catch (DataIntegrityViolationException e) {
            // Safety net for rare concurrent requests that collide
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Token conflict detected — please try again.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── Get All Queue Entries ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> getAllQueue() {
        try {
            List<Queue> queues = queueRepository.findAll();
            return ResponseEntity.ok(queues);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── Get Queue By Id ───────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getQueueById(@PathVariable Long id) {
        try {
            Queue queue = queueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Queue not found"));
            return ResponseEntity.ok(queue);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── Update Queue Status ───────────────────────────────────────────────────

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody QueueStatusRequest request) {
        try {

            System.out.println("========== QUEUE STATUS UPDATE ==========");
            System.out.println("Queue ID = " + id);
            System.out.println("Status   = " + request.getStatus());

            Queue queue = queueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Queue not found"));

            queue.setStatus(
                    Queue.QueueStatus.valueOf(request.getStatus().toUpperCase())
            );

            Queue updatedQueue = queueRepository.save(queue);
            return ResponseEntity.ok(updatedQueue);

        } catch (Exception e) {
            System.out.println("========== ERROR ==========");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── Delete Queue Entry ────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQueue(@PathVariable Long id) {
        try {
            if (!queueRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            queueRepository.deleteById(id);
            return ResponseEntity.ok("Queue entry deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}