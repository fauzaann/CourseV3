package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CourseSummaryResponse {
    private Long id;
    private String title;
    private String thumbnail;
    private BigDecimal price;
    private String category;
    private String level;
    private Double rating;
    private Integer totalStudents;
    private String instructorName;
}