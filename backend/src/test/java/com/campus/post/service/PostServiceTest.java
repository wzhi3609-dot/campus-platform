package com.campus.post.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.like.repository.LikeRepository;
import com.campus.notification.service.NotificationService;
import com.campus.post.entity.Post;
import com.campus.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserNameHelper userNameHelper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private LikeRepository likeRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userNameHelper, notificationService, likeRepository);
    }

    @Test
    void createPost_shouldSetPendingAndSanitize() {
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Post result = postService.createPost("标题", "<p>安全内容</p><script>alert('xss')</script>", 1L);
        assertEquals("PENDING", result.getStatus());
        assertEquals("标题", result.getTitle());
        assertFalse(result.getContent().contains("<script>"));
    }

    @Test
    void approvePost_shouldSetApprovedAndNotify() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("测试帖子");
        post.setUserId(2L);
        post.setStatus("PENDING");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.approvePost(1L);
        assertEquals("APPROVED", post.getStatus());
        verify(notificationService).create(eq(2L), eq("post_approved"), anyString(), anyString(), eq(1L));
    }

    @Test
    void rejectPost_shouldSetRejectedAndNotify() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("测试帖子");
        post.setUserId(2L);
        post.setStatus("PENDING");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.rejectPost(1L);
        assertEquals("REJECTED", post.getStatus());
        verify(notificationService).create(eq(2L), eq("post_rejected"), anyString(), anyString(), eq(1L));
    }

    @Test
    void deletePost_notOwner_shouldThrow() {
        Post post = new Post();
        post.setId(1L);
        post.setUserId(2L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(BusinessException.class, () -> postService.deletePost(1L, 99L));
        verify(postRepository, never()).delete(any());
    }

    @Test
    void toggleLikePost_firstTime_shouldLike() {
        Post post = new Post();
        post.setId(1L);
        post.setLikeCount(0);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndTargetTypeAndTargetId(1L, "post", 1L)).thenReturn(false);

        boolean liked = postService.toggleLikePost(1L, 1L);
        assertTrue(liked);
        assertEquals(1, post.getLikeCount());
    }

    @Test
    void toggleLikePost_secondTime_shouldUnlike() {
        Post post = new Post();
        post.setId(1L);
        post.setLikeCount(1);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByUserIdAndTargetTypeAndTargetId(1L, "post", 1L)).thenReturn(true);

        boolean liked = postService.toggleLikePost(1L, 1L);
        assertFalse(liked);
        assertEquals(0, post.getLikeCount());
    }
}
