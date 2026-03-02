package com.belajar.course.controller;

import com.belajar.course.dto.ResponseDto.EnrollmentResponse;
import com.belajar.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /* POST /api/enrollments/{courseId} — enroll kursus */
    @PostMapping("/{courseId}")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(courseId));
    }

    /* GET /api/enrollments/my — enrollment milik user login */
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    /* GET /api/enrollments — semua enrollment (admin only) ← TAMBAH INI */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    /* POST /api/enrollments/{courseId}/lessons/{lessonId}/complete */
    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<EnrollmentResponse> markLessonComplete(
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(enrollmentService.markLessonComplete(courseId, lessonId));
    }
}