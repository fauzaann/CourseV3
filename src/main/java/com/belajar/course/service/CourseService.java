package com.belajar.course.service;

import com.belajar.course.config.storage.FileStorageService;
import com.belajar.course.dto.RequestDto.CreateCourseRequest;
import com.belajar.course.dto.RequestDto.CreateModuleRequest;
import com.belajar.course.dto.RequestDto.UpdateCourseRequest;
import com.belajar.course.dto.ResponseDto.*;
import com.belajar.course.exception.ResourceNotFoundException;
import com.belajar.course.exception.UnauthorizedException;
import com.belajar.course.model.models.*;
import com.belajar.course.model.models.Module;
import com.belajar.course.model.enums.CourseCategory;
import com.belajar.course.model.enums.CourseLevel;
import com.belajar.course.model.enums.CourseStatus;
import com.belajar.course.model.enums.Role;
import com.belajar.course.repository.CourseRepository;
import com.belajar.course.repository.EnrollmentRepository;
import com.belajar.course.repository.ModuleRepository;
import com.belajar.course.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final SecurityUtil securityUtil;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        User instructor = securityUtil.getCurrentUser();
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .level(request.getLevel())
                .instructor(instructor)
                .status(CourseStatus.DRAFT)
                .build();
        return mapToCourseResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = findCourseOrThrow(id);
        verifyInstructor(course);

        if (request.getTitle() != null)       course.setTitle(request.getTitle());
        if (request.getDescription() != null)  course.setDescription(request.getDescription());
        if (request.getPrice() != null)        course.setPrice(request.getPrice());
        if (request.getCategory() != null)     course.setCategory(request.getCategory());
        if (request.getLevel() != null)        course.setLevel(request.getLevel());
        if (request.getStatus() != null)       course.setStatus(request.getStatus());

        return mapToCourseResponse(courseRepository.save(course));
    }

    public CourseResponse getCourseById(Long id) {
        Course course = findCourseOrThrow(id);
        return mapToCourseResponse(course, canAccessLessons(course));
    }

    public Page<CourseSummaryResponse> getPublishedCourses(Pageable pageable) {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable)
                .map(this::mapToSummaryResponse);
    }

    public Page<CourseSummaryResponse> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToSummaryResponse);
    }

    public Page<CourseSummaryResponse> filterCourses(String category, String level, Pageable pageable) {
        CourseCategory cat = category != null ? CourseCategory.valueOf(category.toUpperCase()) : null;
        CourseLevel lvl = level != null ? CourseLevel.valueOf(level.toUpperCase()) : null;
        return courseRepository.findByFilter(cat, lvl, pageable).map(this::mapToSummaryResponse);
    }

    public List<CourseSummaryResponse> getMyCourses() {
        User instructor = securityUtil.getCurrentUser();
        return courseRepository.findByInstructor(instructor)
                .stream().map(this::mapToSummaryResponse).collect(Collectors.toList());
    }

    @Transactional
    public void uploadThumbnail(Long courseId, MultipartFile file) throws IOException {
        Course course = findCourseOrThrow(courseId);
        verifyInstructor(course);
        if (course.getThumbnail() != null) fileStorageService.deleteFile(course.getThumbnail());
        course.setThumbnail(fileStorageService.storeImage(file, "thumbnails"));
        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = findCourseOrThrow(id);
        verifyInstructor(course);
        courseRepository.delete(course);
    }

    @Transactional
    public ModuleResponse addModule(Long courseId, CreateModuleRequest request) {
        Course course = findCourseOrThrow(courseId);
        verifyInstructor(course);
        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();
        return mapToModuleResponse(moduleRepository.save(module));
    }

    // ---- Private Helpers ----

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private void verifyInstructor(Course course) {
        if (!securityUtil.isOwner(course.getInstructor().getId()) && !securityUtil.hasRole(Role.ADMIN)) {
            throw new UnauthorizedException("Anda tidak memiliki akses ke course ini");
        }
    }

    private boolean canAccessLessons(Course course) {
        if (!securityUtil.isAuthenticated()) {
            return false;
        }

        if (securityUtil.hasRole(Role.ADMIN)
                || securityUtil.isOwner(course.getInstructor().getId())) {
            return true;
        }

        User currentUser = securityUtil.getCurrentUser();
        return enrollmentRepository.findByStudentAndCourse(currentUser, course).isPresent();
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return mapToCourseResponse(course, true);
    }

    private CourseResponse mapToCourseResponse(Course course, boolean canAccessLessons) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnail(course.getThumbnail())
                .price(course.getPrice())
                .category(course.getCategory() != null ? course.getCategory().name() : null)
                .level(course.getLevel() != null ? course.getLevel().name() : null)
                .status(course.getStatus() != null ? course.getStatus().name() : null)
                .rating(course.getRating())
                .totalStudents(course.getTotalStudents())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .createdAt(course.getCreatedAt())
                .modules(course.getModules() != null
                        ? course.getModules().stream()
                        .map(module -> mapToModuleResponse(module, canAccessLessons))
                        .collect(Collectors.toList())
                        : List.of())
                .build();
    }

    private CourseSummaryResponse mapToSummaryResponse(Course course) {
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnail(course.getThumbnail())
                .price(course.getPrice())
                .category(course.getCategory() != null ? course.getCategory().name() : null)
                .level(course.getLevel() != null ? course.getLevel().name() : null)
                .rating(course.getRating())
                .totalStudents(course.getTotalStudents())
                .instructorName(course.getInstructor().getFullName())
                .build();
    }

    private ModuleResponse mapToModuleResponse(Module module) {
        return mapToModuleResponse(module, true);
    }

    private ModuleResponse mapToModuleResponse(Module module, boolean canAccessLessons) {
        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .lessons(module.getLessons() != null
                        ? module.getLessons().stream().map(l -> LessonResponse.builder()
                        .id(l.getId())
                        .title(l.getTitle())
                        .content(canAccessLessons ? l.getContent() : null)
                        .videoUrl(canAccessLessons ? l.getVideoUrl() : null)
                        .fileUrl(canAccessLessons ? l.getFileUrl() : null)
                        .durationMinutes(l.getDurationMinutes())
                        .orderIndex(l.getOrderIndex())
                        .type(l.getType() != null ? l.getType().name() : null)
                        .build()).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}