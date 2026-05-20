package com.campus.trade.repository;

import com.campus.trade.entity.TradeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 二手商品数据访问层接口，提供商品的分页查询、分类筛选和搜索功能
 */
public interface TradeItemRepository extends JpaRepository<TradeItem, Long> {
    Page<TradeItem> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<TradeItem> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<TradeItem> findByTitleContainingOrDescriptionContaining(String title, String desc, Pageable pageable);
    Page<TradeItem> findByCategoryAndStatus(String category, String status, Pageable pageable);
    Page<TradeItem> findByTitleContaining(String keyword, Pageable pageable);
    long countByCategory(String category);
}
