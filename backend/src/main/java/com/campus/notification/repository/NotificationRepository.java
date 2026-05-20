package com.campus.notification.repository;

import com.campus.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 通知数据访问接口，提供通知的数据库操作。
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 分页查询用户的通知，按创建时间降序。
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 通知分页结果
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计用户未读通知的数量。
     *
     * @param userId 用户ID
     * @return 未读通知数
     */
    long countByUserIdAndReadFalse(Long userId);
}
