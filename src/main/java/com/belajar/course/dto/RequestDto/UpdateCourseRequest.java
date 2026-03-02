package com.belajar.course.dto.RequestDto;

import com.belajar.course.model.enums.CourseCategory;
import com.belajar.course.model.enums.CourseLevel;
import com.belajar.course.model.enums.CourseStatus;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCourseRequest {
    private String title;
    private String description;

    @PositiveOrZero(message = "Harga tidak boleh negatif")
    private BigDecimal price;

    private CourseCategory category;
    private CourseLevel level;
    private CourseStatus status;
}