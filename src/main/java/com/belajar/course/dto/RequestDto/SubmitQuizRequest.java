package com.belajar.course.dto.RequestDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class SubmitQuizRequest {

    @NotEmpty(message = "Jawaban tidak boleh kosong")
    private List<AnswerRequest> answers;

    @Data
    public static class AnswerRequest {
        private Long questionId;
        private Long selectedOptionId;
        private String textAnswer;
    }
}