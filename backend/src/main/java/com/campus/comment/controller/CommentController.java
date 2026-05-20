package com.campus.comment.controller;

import com.campus.comment.entity.Comment;
import com.campus.comment.repository.CommentRepository;
import com.campus.common.Result;
import com.campus.common.helper.UserNameHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器，提供添加评论、查询评论和统计评论数量的接口。
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserNameHelper userNameHelper;

    public CommentController(CommentRepository commentRepository, UserNameHelper userNameHelper) {
        this.commentRepository = commentRepository;
        this.userNameHelper = userNameHelper;
    }

    /**
     * 添加评论。
     *
     * @param body 请求体，包含 targetType、targetId、content
     * @param auth 认证信息
     * @return 新增的评论
     */
    @PostMapping
    public Result<Comment> addComment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String content = (String) body.get("content");

        Comment c = new Comment();
        c.setContent(content);
        c.setUserId(userId);
        c.setTargetType(targetType);
        c.setTargetId(targetId);
        c = commentRepository.save(c);
        userNameHelper.setUserName(userId, c::setUserName);
        return Result.success(c);
    }

    /**
     * 分页查询指定目标的评论。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param page       页码
     * @param size       每页条数
     * @return 评论分页结果
     */
    @GetMapping
    public Result<Page<Comment>> getComments(@RequestParam String targetType,
                                             @RequestParam Long targetId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Page<Comment> result = commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtAsc(targetType, targetId,
                PageRequest.of(page, size));
        result.forEach(c -> userNameHelper.setUserName(c.getUserId(), c::setUserName));
        return Result.success(result);
    }

    /**
     * 统计指定目标的评论数量。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 包含 count 的 Map
     */
    @GetMapping("/count")
    public Result<Map<String, Long>> count(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.success(Map.of("count", commentRepository.countByTargetTypeAndTargetId(targetType, targetId)));
    }
}
