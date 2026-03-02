package com.belajar.course.repository;

import com.belajar.course.model.enums.CourseCategory;
import com.belajar.course.model.enums.CourseLevel;
import com.belajar.course.model.enums.CourseStatus;
import com.belajar.course.model.models.Course;
import com.belajar.course.model.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 1. Method yang dibutuhkan untuk getPublishedCourses
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    // 2. Method yang dibutuhkan untuk getMyCourses
    List<Course> findByInstructor(User instructor);

    // 3. Method yang dibutuhkan untuk searchCourses
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 4. Method yang dibutuhkan untuk filterCourses
    @Query("SELECT c FROM Course c WHERE " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:level IS NULL OR c.level = :level) AND " +
            "c.status = 'PUBLISHED'")
    Page<Course> findByFilter(@Param("category") CourseCategory category,
                              @Param("level") CourseLevel level,
                              Pageable pageable);

    // --- Method tambahan yang mungkin kamu butuhkan ---
    List<Course> findByCategory(CourseCategory category);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' AND c.rating >= :minRating")
    List<Course> findTopRatedCourses(@Param("minRating") Double minRating);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' ORDER BY c.totalStudents DESC")
    List<Course> findMostPopularCourses(Pageable pageable);

    Long countByStatus(CourseStatus status);
}