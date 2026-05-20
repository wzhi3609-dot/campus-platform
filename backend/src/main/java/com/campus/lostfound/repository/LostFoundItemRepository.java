package com.campus.lostfound.repository;

import com.campus.lostfound.entity.LostFoundItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 失物招领数据访问层接口，提供按类型和状态的分页查询功能
 */
public interface LostFoundItemRepository extends JpaRepository<LostFoundItem, Long> {
    Page<LostFoundItem> findByTypeAndStatusOrderByCreatedAtDesc(String type, String status, Pageable pageable);
    Page<LostFoundItem> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<LostFoundItem> findByStatusOrderByPinnedDescCreatedAtDesc(String status, Pageable pageable);
    Page<LostFoundItem> findByTypeAndStatusOrderByPinnedDescCreatedAtDesc(String type, String status, Pageable pageable);
    Page<LostFoundItem> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<LostFoundItem> findByTitleContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    Page<LostFoundItem> findByTitleContainingOrderByPinnedDescCreatedAtDesc(String keyword, Pageable pageable);
    Page<LostFoundItem> findByTypeAndTitleContainingOrderByCreatedAtDesc(String type, String keyword, Pageable pageable);
    Page<LostFoundItem> findByTypeAndTitleContainingOrderByPinnedDescCreatedAtDesc(String type, String keyword, Pageable pageable);
}
