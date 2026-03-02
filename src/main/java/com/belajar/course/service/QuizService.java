package com.belajar.course.service;

import com.belajar.course.dto.RequestDto.CreateQuizRequest;
import com.belajar.course.dto.RequestDto.SubmitQuizRequest;
import com.belajar.course.dto.ResponseDto.QuizAttemptResponse;
import com.belajar.course.dto.ResponseDto.QuizResponse;
import com.belajar.course.exception.ResourceNotFoundException;
import com.belajar.course.model.models.*;
import com.belajar.course.repository.CourseRepository;
import com.belajar.course.repository.QuizRepository;
import com.belajar.course.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public QuizResponse createQuiz(Long courseId, CreateQuizRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .passingScore(request.getPassingScore())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .course(course)
                .build();

        if (request.getQuestions() != null) {
            List<Question> questions = request.getQuestions().stream().map(qr -> {
                Question question = Question.builder()
                        .questionText(qr.getQuestionText())
                        .type(qr.getType())
                        .points(qr.getPoints())
                        .quiz(quiz)
                        .build();
                if (qr.getOptions() != null) {
                    List<AnswerOption> options = qr.getOptions().stream().map(or ->
                            AnswerOption.builder()
                                    .optionText(or.getOptionText())
                                    .correct(or.isCorrect())
                                    .question(question)
                                    .build()
                    ).collect(Collectors.toList());
                    question.setOptions(options);
                }
                return question;
            }).collect(Collectors.toList());
            quiz.setQuestions(questions);
        }

        return mapToResponse(quizRepository.save(quiz), true);
    }

    public List<QuizResponse> getCourseQuizzes(Long courseId) {
        boolean isInstructor = securityUtil.isInstructor();
        return quizRepository.findByCourseId(courseId)
                .stream().map(q -> mapToResponse(q, isInstructor)).collect(Collectors.toList());
    }

    @Transactional
    public QuizAttemptResponse submitQuiz(Long quizId, SubmitQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        int totalPoints = quiz.getQuestions().stream()
                .mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1).sum();

        int earnedPoints = 0;
        for (Question question : quiz.getQuestions()) {
            int pts = question.getPoints() != null ? question.getPoints() : 1;
            boolean correct = request.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .map(a -> question.getOptions().stream()
                            .anyMatch(o -> o.getId().equals(a.getSelectedOptionId()) && o.isCorrect()))
                    .orElse(false);
            if (correct) earnedPoints += pts;
        }

        int score = totalPoints > 0 ? (earnedPoints * 100) / totalPoints : 0;
        boolean passed = score >= quiz.getPassingScore();

        return QuizAttemptResponse.builder()
                .score(score)
                .passed(passed)
                .passingScore(quiz.getPassingScore())
                .message(passed ? "Selamat! Kamu lulus quiz ini." : "Belum lulus, coba lagi!")
                .build();
    }

    // ---- Private Helpers ----

    private QuizResponse mapToResponse(Quiz quiz, boolean showAnswers) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .passingScore(quiz.getPassingScore())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .questions(quiz.getQuestions() != null
                        ? quiz.getQuestions().stream().map(q -> QuizResponse.QuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .type(q.getType() != null ? q.getType().name() : null)
                        .points(q.getPoints())
                        .options(q.getOptions() != null
                                ? q.getOptions().stream().map(o -> QuizResponse.OptionResponse.builder()
                                .id(o.getId())
                                .optionText(o.getOptionText())
                                .correct(showAnswers ? o.isCorrect() : null)
                                .build()).collect(Collectors.toList())
                                : List.of())
                        .build()).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}