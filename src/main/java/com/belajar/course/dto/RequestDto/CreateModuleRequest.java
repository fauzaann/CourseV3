package com.belajar.course.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateModuleRequest {

    @NotBlank(message = "Judul modul wajib diisi")
    private String title;

    private String description;

    @NotNull(message = "Urutan modul wajib diisi")
    private Integer orderIndex;
}