package com.belajar.course.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private List<QuestionResponse> questions;

    @Data
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String type;
        private Integer points;
        private List<OptionResponse> options;
    }

    @Data
    @Builder
    public static class OptionResponse {
        private Long id;
        private String optionText;
        private Boolean correct; // null untuk student, true/false untuk instructor
    }
}