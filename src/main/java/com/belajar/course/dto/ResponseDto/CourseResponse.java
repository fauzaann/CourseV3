package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private BigDecimal price;
    private String category;
    private String level;
    private String status;
    private Double rating;
    private Integer totalStudents;
    private Long instructorId;
    private String instructorName;
    private LocalDateTime createdAt;
    private List<ModuleResponse> modules;
}