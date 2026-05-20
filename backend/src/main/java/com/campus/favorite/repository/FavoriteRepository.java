package com.campus.favorite.repository;

import com.campus.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 收藏数据访问接口，提供收藏的数据库操作。
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    /**
     * 根据用户ID、目标类型和目标ID查找收藏记录。
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏记录（可能为空）
     */
    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    /**
     * 判断用户是否已收藏指定目标。
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 是否已收藏
     */
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    /**
     * 分页查询用户的某类收藏，按创建时间降序。
     *
     * @param userId     用户ID
     * @param targetType 目标类型
     * @param pageable   分页参数
     * @return 收藏分页结果
     */
    Page<Favorite> findByUserIdAndTargetTypeOrderByCreatedAtDesc(Long userId, String targetType, Pageable pageable);

    /**
     * 统计指定目标的收藏数量。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 收藏总数
     */
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
