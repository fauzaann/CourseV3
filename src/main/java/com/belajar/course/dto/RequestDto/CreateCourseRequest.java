package com.belajar.course.dto.RequestDto;

import com.belajar.course.model.enums.CourseCategory;
import com.belajar.course.model.enums.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Judul course wajib diisi")
    private String title;

    private String description;

    @NotNull(message = "Harga wajib diisi")
    @PositiveOrZero(message = "Harga tidak boleh negatif")
    private BigDecimal price;

    private CourseCategory category;

    private CourseLevel level;
}