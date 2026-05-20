package com.campus.post.repository;

import com.campus.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 帖子数据访问层接口，提供帖子的分页查询、筛选和搜索功能
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Post> findByStatusOrderByPinnedDescCreatedAtDesc(String status, Pageable pageable);
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Post> findByTitleContainingAndStatus(String keyword, String status, Pageable pageable);
    Page<Post> findByStatusAndTitleContainingOrderByCreatedAtDesc(String status, String keyword, Pageable pageable);
    Page<Post> findByStatusAndTitleContainingOrderByPinnedDescCreatedAtDesc(String status, String keyword, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
