package com.campus.post.controller;

import com.campus.common.Result;
import com.campus.log.annotation.Loggable;
import com.campus.post.entity.Post;
import com.campus.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器，提供发帖、编辑、查询和点赞等 REST 接口
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 帖子请求内部类，封装发帖/编辑时的标题和内容
     */
    public static class PostRequest {
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题最长200个字符")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    /**
     * 发布新帖子
     * @param request        帖子请求
     * @param authentication 认证信息
     * @return 创建的帖子
     */
    @PostMapping
    @Loggable(action = "发帖", target = "帖子")
    public Result<Post> createPost(@Valid @RequestBody PostRequest request,
                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(postService.createPost(request.getTitle(), request.getContent(), userId));
    }

    /**
     * 编辑帖子
     * @param id             帖子ID
     * @param request        帖子请求
     * @param authentication 认证信息
     * @return 更新后的帖子
     */
    @PutMapping("/{id}")
    @Loggable(action = "编辑帖子", target = "帖子")
    public Result<Post> updatePost(@PathVariable Long id,
                                   @Valid @RequestBody PostRequest request,
                                   Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(postService.updatePost(id, userId, request.getTitle(), request.getContent()));
    }

    /**
     * 分页获取已审核通过的帖子列表
     * @param page 页码
     * @param size 每页条数
     * @return 帖子分页列表
     */
    @GetMapping
    public Result<Page<Post>> getApprovedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return Result.success(postService.searchApprovedPosts(keyword, page, size));
        }
        return Result.success(postService.getApprovedPosts(page, size));
    }

    /**
     * 根据ID获取帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/{id}")
    public Result<Post> getPost(@PathVariable Long id) {
        return Result.success(postService.getPost(id));
    }

    /**
     * 删除帖子
     * @param id             帖子ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Loggable(action = "删除帖子", target = "帖子")
    public Result<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(id, userId);
        return Result.success();
    }

    /**
     * 点赞/取消点赞帖子（toggle 模式）
     * @param id 帖子ID
     * @param authentication 认证信息
     * @return 操作结果，data 为 true 表示已点赞，false 表示已取消
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> likePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(postService.toggleLikePost(id, userId));
    }
}
