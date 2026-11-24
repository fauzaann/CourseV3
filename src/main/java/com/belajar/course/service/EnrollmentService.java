package com.belajar.course.service;

import com.belajar.course.dto.ResponseDto.EnrollmentResponse;
import com.belajar.course.dto.ResponseDto.LessonResponse;
import com.belajar.course.exception.BadRequestException;
import com.belajar.course.exception.ResourceNotFoundException;
import com.belajar.course.exception.UnauthorizedException;
import com.belajar.course.model.enums.Role;
import com.belajar.course.model.models.*;
import com.belajar.course.model.models.Module;
import com.belajar.course.model.enums.EnrollmentStatus;
import com.belajar.course.repository.*;
import com.belajar.course.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
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

    public List<EnrollmentResponse> getInstructorEnrollments() {
        User instructor = securityUtil.getCurrentUser();
        return enrollmentRepository.findAll()
                .stream()
                .filter(enrollment -> enrollment.getCourse().getInstructor() != null
                        && enrollment.getCourse().getInstructor().getId().equals(instructor.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        verifyLessonBelongsToCourse(lesson, course);
        verifyPreviousLessonCompleted(enrollment, course, lesson);

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElse(LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
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

    public LessonResponse getLesson(Long courseId, Long lessonId) {
        User currentUser = securityUtil.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        verifyLessonBelongsToCourse(lesson, course);

        boolean isCourseManager = securityUtil.hasRole(Role.ADMIN)
                || (course.getInstructor() != null
                && course.getInstructor().getId().equals(currentUser.getId()));

        Enrollment enrollment = null;
        if (!isCourseManager) {
            enrollment = enrollmentRepository.findByStudentAndCourse(currentUser, course)
                    .orElseThrow(() -> new UnauthorizedException(
                            "Anda belum terdaftar di course ini"));
            verifyPreviousLessonCompleted(enrollment, course, lesson);
        }

        boolean completed = enrollment != null
                && lessonProgressRepository
                        .existsByEnrollmentIdAndLessonIdAndCompletedTrue(enrollment.getId(), lessonId);

        return mapToLessonResponse(lesson, completed);
    }

    // ---- Private Helpers ----

    private void verifyLessonBelongsToCourse(Lesson lesson, Course course) {
        if (lesson.getModule() == null
                || lesson.getModule().getCourse() == null
                || !lesson.getModule().getCourse().getId().equals(course.getId())) {
            throw new BadRequestException("Lesson tidak termasuk dalam course ini");
        }
    }

    private void verifyPreviousLessonCompleted(
            Enrollment enrollment, Course course, Lesson lesson) {
        List<Lesson> orderedLessons = getOrderedLessons(course);
        int lessonIndex = -1;

        for (int i = 0; i < orderedLessons.size(); i++) {
            if (orderedLessons.get(i).getId().equals(lesson.getId())) {
                lessonIndex = i;
                break;
            }
        }

        if (lessonIndex < 0) {
            throw new BadRequestException("Lesson tidak termasuk dalam course ini");
        }

        if (lessonIndex == 0) {
            return;
        }

        Lesson previousLesson = orderedLessons.get(lessonIndex - 1);
        boolean previousCompleted = lessonProgressRepository
                .existsByEnrollmentIdAndLessonIdAndCompletedTrue(
                        enrollment.getId(), previousLesson.getId());

        if (!previousCompleted) {
            throw new BadRequestException("Selesaikan lesson sebelumnya terlebih dahulu");
        }
    }

    private List<Lesson> getOrderedLessons(Course course) {
        Comparator<Module> moduleComparator = Comparator
                .comparing((Module module) -> module.getOrderIndex() == null
                        ? Integer.MAX_VALUE
                        : module.getOrderIndex())
                .thenComparing(module -> module.getId() == null ? Long.MAX_VALUE : module.getId());

        Comparator<Lesson> lessonComparator = Comparator
                .comparing((Lesson lesson) -> lesson.getOrderIndex() == null
                        ? Integer.MAX_VALUE
                        : lesson.getOrderIndex())
                .thenComparing(lesson -> lesson.getId() == null
                        ? Long.MAX_VALUE
                        : lesson.getId());

        List<Lesson> orderedLessons = new java.util.ArrayList<>();
        if (course.getModules() == null) {
            return orderedLessons;
        }

        course.getModules().stream().sorted(moduleComparator).forEach(module -> {
            if (module.getLessons() != null) {
                orderedLessons.addAll(module.getLessons().stream()
                        .sorted(lessonComparator)
                        .toList());
            }
        });
        return orderedLessons;
    }

    private LessonResponse mapToLessonResponse(Lesson lesson, boolean completed) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .fileUrl(lesson.getFileUrl())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .type(lesson.getType() != null ? lesson.getType().name() : null)
                .completed(completed)
                .build();
    }

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