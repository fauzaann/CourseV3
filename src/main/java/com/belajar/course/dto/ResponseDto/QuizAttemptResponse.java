package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizAttemptResponse {
    private Integer score;
    private boolean passed;
    private Integer passingScore;
    private String message;
}