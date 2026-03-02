package com.belajar.course.controller;

    import com.belajar.course.dto.RequestDto.CreateQuizRequest;
import com.belajar.course.dto.RequestDto.SubmitQuizRequest;
import com.belajar.course.dto.ResponseDto.QuizAttemptResponse;
import com.belajar.course.dto.ResponseDto.QuizResponse;
import com.belajar.course.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(courseId, request));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<QuizResponse>> getCourseQuizzes(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getCourseQuizzes(courseId));
    }

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<QuizAttemptResponse> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitQuizRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(quizId, request));
    }
}