package com.campus.like.repository;

import com.campus.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
