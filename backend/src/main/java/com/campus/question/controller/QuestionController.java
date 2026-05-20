package com.campus.question.controller;

import com.campus.common.Result;
import com.campus.log.annotation.Loggable;
import com.campus.question.dto.AnswerRequest;
import com.campus.question.dto.QuestionRequest;
import com.campus.question.entity.Answer;
import com.campus.question.entity.Question;
import com.campus.question.repository.AnswerRepository;
import com.campus.question.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 问答控制器，提供提问、回答、搜索、点赞等 REST 接口
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerRepository answerRepository;

    public QuestionController(QuestionService questionService, AnswerRepository answerRepository) {
        this.questionService = questionService;
        this.answerRepository = answerRepository;
    }

    /**
     * 创建新问题
     * @param request        问题请求
     * @param authentication 认证信息
     * @return 创建的问题
     */
    @PostMapping
    @Loggable(action = "提问", target = "问题")
    public Result<Question> createQuestion(@Valid @RequestBody QuestionRequest request,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(questionService.createQuestion(request, userId));
    }

    /**
     * 编辑问题
     * @param id             问题ID
     * @param request        问题请求
     * @param authentication 认证信息
     * @return 更新后的问题
     */
    @PutMapping("/{id}")
    @Loggable(action = "编辑问题", target = "问题")
    public Result<Question> updateQuestion(@PathVariable Long id,
                                           @Valid @RequestBody QuestionRequest request,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(questionService.updateQuestion(id, userId, request));
    }

    /**
     * 分页获取问题列表，支持关键词搜索
     * @param page    页码
     * @param size    每页条数
     * @param keyword 搜索关键词（可选）
     * @return 问题分页列表
     */
    @GetMapping
    public Result<Page<Question>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(questionService.getQuestions(page, size, keyword));
    }

    /**
     * 根据ID获取问题详情
     * @param id 问题ID
     * @return 问题详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getQuestion(id));
    }

    /**
     * 删除问题（仅作者可操作）
     * @param id             问题ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Loggable(action = "删除问题", target = "问题")
    public Result<Void> deleteQuestion(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        questionService.deleteQuestion(id, userId);
        return Result.success();
    }

    /**
     * 点赞/取消点赞问题（toggle 模式）
     * @param id 问题ID
     * @param authentication 认证信息
     * @return 操作结果，data 为 true 表示已点赞，false 表示已取消
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> likeQuestion(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(questionService.toggleLikeQuestion(id, userId));
    }

    /**
     * 回答问题
     * @param questionId     问题ID
     * @param request        回答请求
     * @param authentication 认证信息
     * @return 创建的答案
     */
    @PostMapping("/{questionId}/answers")
    @Loggable(action = "回答", target = "问题")
    public Result<Answer> addAnswer(@PathVariable Long questionId,
                                    @Valid @RequestBody AnswerRequest request,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(questionService.addAnswer(questionId, request, userId));
    }

    /**
     * 获取问题的所有回答列表
     * @param questionId 问题ID
     * @return 回答列表
     */
    @GetMapping("/{questionId}/answers")
    public Result<List<Answer>> getAnswers(@PathVariable Long questionId) {
        return Result.success(answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId));
    }

    /**
     * 采纳答案（仅提问者可操作）
     * @param questionId     问题ID
     * @param answerId       答案ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PostMapping("/{questionId}/answers/{answerId}/accept")
    @Loggable(action = "采纳答案", target = "回答")
    public Result<Void> acceptAnswer(@PathVariable Long questionId,
                                     @PathVariable Long answerId,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        questionService.acceptAnswer(questionId, answerId, userId);
        return Result.success();
    }
}
