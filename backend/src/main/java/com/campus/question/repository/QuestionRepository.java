package com.campus.question.repository;

import com.campus.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 问题数据访问层接口，提供问题的分页查询和搜索功能
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Page<Question> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Question> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Question> findByResolved(Boolean resolved, Pageable pageable);
    Page<Question> findByTitleContaining(String keyword, Pageable pageable);
}
