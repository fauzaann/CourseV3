package com.belajar.course.service;

import com.belajar.course.dto.ResponseDto.EnrollmentResponse;
import com.belajar.course.exception.BadRequestException;
import com.belajar.course.exception.ResourceNotFoundException;
import com.belajar.course.model.models.*;
import com.belajar.course.model.enums.EnrollmentStatus;
import com.belajar.course.repository.*;
import com.belajar.course.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public EnrollmentResponse enroll(Long courseId) {
        User student = securityUtil.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new BadRequestException("Kamu sudah terdaftar di course ini");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();
        enrollment = enrollmentRepository.save(enrollment);

        course.setTotalStudents(course.getTotalStudents() + 1);
        courseRepository.save(course);

        return mapToResponse(enrollment);
    }

    public List<EnrollmentResponse> getMyEnrollments() {
        User student = securityUtil.getCurrentUser();
        return enrollmentRepository.findByStudent(student)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    // Tambahkan setelah method getMyEnrollments()
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponse markLessonComplete(Long courseId, Long lessonId) {
        User student = securityUtil.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new BadRequestException("Kamu belum terdaftar di course ini"));

        // Tandai lesson sebagai selesai
        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElse(LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(Lesson.builder().id(lessonId).build())
                        .build());

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        }

        // Hitung ulang progress
        recalculateProgress(enrollment, course);
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ---- Private Helpers ----

    private void recalculateProgress(Enrollment enrollment, Course course) {
        long totalLessons = course.getModules().stream()
                .mapToLong(m -> m.getLessons() != null ? m.getLessons().size() : 0).sum();
        long completed = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());
        int percent = totalLessons > 0 ? (int) ((completed * 100) / totalLessons) : 0;

        enrollment.setProgressPercent(percent);
        if (percent >= 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .courseThumbnail(enrollment.getCourse().getThumbnail())
                .progressPercent(enrollment.getProgressPercent())
                .status(enrollment.getStatus().name())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .build();
    }

}