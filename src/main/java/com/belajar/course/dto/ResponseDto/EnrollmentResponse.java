package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private Integer progressPercent;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
}