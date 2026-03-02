package com.belajar.course.controller;

import com.belajar.course.dto.RequestDto.CreateCourseRequest;
import com.belajar.course.dto.RequestDto.CreateModuleRequest;
import com.belajar.course.dto.RequestDto.UpdateCourseRequest;
import com.belajar.course.dto.ResponseDto.CourseResponse;
import com.belajar.course.dto.ResponseDto.CourseSummaryResponse;
import com.belajar.course.dto.ResponseDto.ModuleResponse;
import com.belajar.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    // ---- PUBLIC ----

    @GetMapping
    public ResponseEntity<Page<CourseSummaryResponse>> getPublishedCourses(
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(courseService.getPublishedCourses(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CourseSummaryResponse>> searchCourses(
            @RequestParam String keyword,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(courseService.searchCourses(keyword, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CourseSummaryResponse>> filterCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(courseService.filterCourses(category, level, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // ---- INSTRUCTOR ----

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @PostMapping("/{id}/thumbnail")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> uploadThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        courseService.uploadThumbnail(id, file);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<CourseSummaryResponse>> getMyCourses() {
        return ResponseEntity.ok(courseService.getMyCourses());
    }

    // ---- MODULES ----

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ModuleResponse> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.addModule(courseId, request));
    }
}