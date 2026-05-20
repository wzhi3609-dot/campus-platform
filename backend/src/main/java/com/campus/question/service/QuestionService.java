package com.campus.question.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.like.repository.LikeRepository;
import com.campus.notification.service.NotificationService;
import com.campus.question.dto.AnswerRequest;
import com.campus.question.dto.QuestionRequest;
import com.campus.question.entity.Answer;
import com.campus.question.entity.Question;
import com.campus.question.repository.AnswerRepository;
import com.campus.question.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问答业务逻辑服务，处理提问、回答、采纳答案等核心业务
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserNameHelper userNameHelper;
    private final NotificationService notificationService;
    private final LikeRepository likeRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository,
                           UserNameHelper userNameHelper, NotificationService notificationService,
                           LikeRepository likeRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userNameHelper = userNameHelper;
        this.notificationService = notificationService;
        this.likeRepository = likeRepository;
    }

    /**
     * 创建新问题
     * @param request 问题请求
     * @param userId  提问用户ID
     * @return 创建的问题
     */
    public Question createQuestion(QuestionRequest request, Long userId) {
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setTags(request.getTags());
        question.setUserId(userId);
        question = questionRepository.save(question);
        userNameHelper.setUserName(userId, question::setUserName);
        return question;
    }

    /**
     * 更新问题（仅作者可操作）
     * @param id      问题ID
     * @param userId  操作用户ID
     * @param request 问题请求
     * @return 更新后的问题
     */
    public Question updateQuestion(Long id, Long userId, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的问题");
        }
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setTags(request.getTags());
        question = questionRepository.save(question);
        userNameHelper.setUserName(userId, question::setUserName);
        return question;
    }

    /**
     * 根据ID获取问题，并增加浏览次数
     * @param id 问题ID
     * @return 问题详情
     */
    public Question getQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        question.setViewCount(question.getViewCount() + 1);
        question = questionRepository.save(question);
        userNameHelper.setUserName(question.getUserId(), question::setUserName);
        return question;
    }

    /**
     * 分页查询问题列表，支持按关键词搜索
     * @param page    页码
     * @param size    每页条数
     * @param keyword 搜索关键词
     * @return 问题分页列表
     */
    public Page<Question> getQuestions(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> result;
        if (keyword != null && !keyword.isBlank()) {
            result = questionRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageRequest);
        } else {
            result = questionRepository.findAll(pageRequest);
        }
        result.forEach(q -> userNameHelper.setUserName(q.getUserId(), q::setUserName));
        return result;
    }

    /**
     * 回答问题：保存回答并更新问题的回答数量，发送通知
     * @param questionId 问题ID
     * @param request    回答请求
     * @param userId     回答用户ID
     * @return 创建的答案
     */
    @Transactional
    public Answer addAnswer(Long questionId, AnswerRequest request, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在"));

        Answer answer = new Answer();
        answer.setContent(request.getContent());
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer = answerRepository.save(answer);

        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);

        notificationService.create(question.getUserId(), "answer", "收到新回答",
                "你的问题 \"" + question.getTitle() + "\" 有了新回答", questionId);
        return answer;
    }

    /**
     * 采纳答案：将答案标记为已采纳，同时将问题标记为已解决
     * @param questionId 问题ID
     * @param answerId   答案ID
     * @param userId     操作用户ID（必须是提问者）
     */
    @Transactional
    public void acceptAnswer(Long questionId, Long answerId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在"));

        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("只有提问者才能采纳答案");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException("答案不存在"));

        if (!answer.getQuestionId().equals(questionId)) {
            throw new BusinessException("答案不属于该问题");
        }

        answer.setAccepted(true);
        answerRepository.save(answer);
        question.setResolved(true);
        questionRepository.save(question);
    }

    /**
     * 删除问题（仅作者可操作）
     * @param id     问题ID
     * @param userId 操作用户ID
     */
    public void deleteQuestion(Long id, Long userId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的问题");
        }
        questionRepository.delete(question);
    }

    /**
     * 点赞/取消点赞问题：toggle 模式，同一用户重复点赞会取消
     * @param id     问题ID
     * @param userId 用户ID
     * @return true=已点赞, false=已取消
     */
    @Transactional
    public boolean toggleLikeQuestion(Long id, Long userId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        boolean exists = likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "question", id);
        if (exists) {
            likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, "question", id)
                    .ifPresent(likeRepository::delete);
            question.setLikeCount(Math.max(0, question.getLikeCount() - 1));
            questionRepository.save(question);
            return false;
        } else {
            com.campus.like.entity.Like like = new com.campus.like.entity.Like();
            like.setUserId(userId);
            like.setTargetType("question");
            like.setTargetId(id);
            likeRepository.save(like);
            question.setLikeCount(question.getLikeCount() + 1);
            questionRepository.save(question);
            return true;
        }
    }
}
