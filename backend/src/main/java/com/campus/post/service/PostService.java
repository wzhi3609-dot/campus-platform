package com.campus.post.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.common.util.HtmlSanitizer;
import com.campus.like.repository.LikeRepository;
import com.campus.notification.service.NotificationService;
import com.campus.post.entity.Post;
import com.campus.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子业务逻辑服务，处理帖子的创建、编辑、审核和点赞等业务
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserNameHelper userNameHelper;
    private final NotificationService notificationService;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository, UserNameHelper userNameHelper,
                       NotificationService notificationService, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userNameHelper = userNameHelper;
        this.notificationService = notificationService;
        this.likeRepository = likeRepository;
    }

    /**
     * 创建新帖子（初始状态为待审核）
     * @param title   帖子标题
     * @param content 帖子内容
     * @param userId  发布用户ID
     * @return 创建的帖子
     */
    public Post createPost(String title, String content, Long userId) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(HtmlSanitizer.sanitize(content));
        post.setUserId(userId);
        post.setStatus("PENDING");
        post = postRepository.save(post);
        userNameHelper.setUserName(userId, post::setUserName);
        return post;
    }

    /**
     * 更新帖子（仅作者可操作，更新后重新进入待审核状态）
     * @param id      帖子ID
     * @param userId  操作用户ID
     * @param title   新标题
     * @param content 新内容
     * @return 更新后的帖子
     */
    public Post updatePost(Long id, Long userId, String title, String content) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的帖子");
        }
        post.setTitle(title);
        post.setContent(HtmlSanitizer.sanitize(content));
        post.setStatus("PENDING");
        post = postRepository.save(post);
        userNameHelper.setUserName(userId, post::setUserName);
        return post;
    }

    /**
     * 根据ID获取帖子详情，并增加浏览次数
     * @param id 帖子ID
     * @return 帖子详情
     */
    public Post getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        post = postRepository.save(post);
        userNameHelper.setUserName(post.getUserId(), post::setUserName);
        return post;
    }

    /**
     * 分页查询已审核通过的帖子
     * @param page 页码
     * @param size 每页条数
     * @return 通过审核的帖子分页列表
     */
    public Page<Post> getApprovedPosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Post> result = postRepository.findByStatusOrderByPinnedDescCreatedAtDesc("APPROVED", pageRequest);
        result.forEach(p -> userNameHelper.setUserName(p.getUserId(), p::setUserName));
        return result;
    }

    /**
     * 按关键字搜索已通过的帖子
     */
    public Page<Post> searchApprovedPosts(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Post> result = postRepository.findByStatusAndTitleContainingOrderByPinnedDescCreatedAtDesc("APPROVED", keyword, pageRequest);
        result.forEach(p -> userNameHelper.setUserName(p.getUserId(), p::setUserName));
        return result;
    }

    /**
     * 分页查询待审核的帖子（管理员使用）
     * @param page 页码
     * @param size 每页条数
     * @return 待审核帖子分页列表
     */
    public Page<Post> getPendingPosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = postRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageRequest);
        result.forEach(p -> userNameHelper.setUserName(p.getUserId(), p::setUserName));
        return result;
    }

    /**
     * 审核通过帖子，并发送通知给发帖人
     * @param id 帖子ID
     */
    public void approvePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setStatus("APPROVED");
        postRepository.save(post);
        notificationService.create(post.getUserId(), "post_approved", "帖子通过审核",
                "你的帖子 \"" + post.getTitle() + "\" 已通过审核", id);
    }

    /**
     * 驳回帖子审核，并发送通知给发帖人
     * @param id 帖子ID
     */
    public void rejectPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setStatus("REJECTED");
        postRepository.save(post);
        notificationService.create(post.getUserId(), "post_rejected", "帖子未通过审核",
                "你的帖子 \"" + post.getTitle() + "\" 未通过审核", id);
    }

    /**
     * 删除帖子（仅作者可操作）
     * @param id     帖子ID
     * @param userId 操作用户ID
     */
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的帖子");
        }
        postRepository.delete(post);
    }

    /**
     * 点赞/取消点赞帖子：toggle 模式，同一用户重复点赞会取消
     * @param id     帖子ID
     * @param userId 用户ID
     * @return true=已点赞, false=已取消
     */
    @Transactional
    public boolean toggleLikePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        boolean exists = likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, "post", id);
        if (exists) {
            likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, "post", id)
                    .ifPresent(likeRepository::delete);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postRepository.save(post);
            return false;
        } else {
            com.campus.like.entity.Like like = new com.campus.like.entity.Like();
            like.setUserId(userId);
            like.setTargetType("post");
            like.setTargetId(id);
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            return true;
        }
    }
}
