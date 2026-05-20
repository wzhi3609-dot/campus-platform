package com.campus.question.repository;

import com.campus.question.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 回答数据访问层接口，提供回答的查询和计数功能
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
    int countByQuestionId(Long questionId);
}
