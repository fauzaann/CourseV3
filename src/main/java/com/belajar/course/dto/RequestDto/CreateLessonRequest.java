package com.belajar.course.dto.RequestDto;

import com.belajar.course.model.enums.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLessonRequest {

    @NotBlank(message = "Judul lesson wajib diisi")
    private String title;

    private String content;

    private String videoUrl;

    private Integer durationMinutes;

    @NotNull(message = "Urutan lesson wajib diisi")
    private Integer orderIndex;

    @NotNull(message = "Tipe lesson wajib diisi")
    private LessonType type;
}