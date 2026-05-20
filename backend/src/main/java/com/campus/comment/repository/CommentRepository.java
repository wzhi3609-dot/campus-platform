package com.campus.comment.repository;

import com.campus.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 评论数据访问接口，提供评论的数据库操作。
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 根据目标类型和目标ID分页查询评论，按创建时间升序。
     *
     * @param targetType 目标类型（如 "post"）
     * @param targetId   目标ID
     * @param pageable   分页参数
     * @return 评论分页结果
     */
    Page<Comment> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(String targetType, Long targetId, Pageable pageable);

    /**
     * 统计指定目标的评论数量。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 评论总数
     */
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
