package com.belajar.course.dto.RequestDto;

import com.belajar.course.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateQuizRequest {

    @NotBlank(message = "Judul quiz wajib diisi")
    private String title;

    private String description;

    private Integer passingScore = 70;

    private Integer timeLimitMinutes;

    @NotEmpty(message = "Quiz harus memiliki minimal 1 pertanyaan")
    private List<QuestionRequest> questions;

    @Data
    public static class QuestionRequest {

        @NotBlank(message = "Teks pertanyaan wajib diisi")
        private String questionText;

        private QuestionType type = QuestionType.MULTIPLE_CHOICE;

        private Integer points = 1;

        @NotEmpty(message = "Pertanyaan harus memiliki pilihan jawaban")
        private List<OptionRequest> options;
    }

    @Data
    public static class OptionRequest {

        @NotBlank(message = "Teks opsi wajib diisi")
        private String optionText;

        private boolean correct = false;
    }
}