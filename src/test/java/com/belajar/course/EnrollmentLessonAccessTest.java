package com.belajar.course;

import com.belajar.course.exception.BadRequestException;
import com.belajar.course.model.models.*;
import com.belajar.course.model.models.Module;
import com.belajar.course.repository.*;
import com.belajar.course.service.EnrollmentService;
import com.belajar.course.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentLessonAccessTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LessonProgressRepository lessonProgressRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void markLessonCompleteRequiresPreviousLessonToBeCompleted() {
        User student = User.builder().id(10L).build();
        User instructor = User.builder().id(20L).build();
        Course course = Course.builder()
                .id(1L)
                .instructor(instructor)
                .modules(new ArrayList<>())
                .build();

        Module module = Module.builder()
                .id(1L)
                .course(course)
                .orderIndex(1)
                .lessons(new ArrayList<>())
                .build();

        Lesson first = Lesson.builder().id(100L).module(module).orderIndex(1).build();
        Lesson second = Lesson.builder().id(101L).module(module).orderIndex(2).build();
        module.getLessons().add(first);
        module.getLessons().add(second);
        course.getModules().add(module);

        Enrollment enrollment = Enrollment.builder().id(50L).student(student).course(course).build();

        when(securityUtil.getCurrentUser()).thenReturn(student);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findById(101L)).thenReturn(Optional.of(second));
        when(lessonProgressRepository
                .existsByEnrollmentIdAndLessonIdAndCompletedTrue(50L, 100L))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> enrollmentService.markLessonComplete(1L, 101L));
    }
}
