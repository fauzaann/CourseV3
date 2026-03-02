package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private String fileUrl;
    private Integer durationMinutes;
    private Integer orderIndex;
    private String type;
    private boolean completed;
}