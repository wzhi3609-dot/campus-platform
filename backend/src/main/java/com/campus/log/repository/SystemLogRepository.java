package com.campus.log.repository;

import com.campus.log.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统日志数据访问接口，提供日志的数据库操作。
 */
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    /**
     * 分页查询所有日志，按创建时间降序。
     *
     * @param pageable 分页参数
     * @return 日志分页结果
     */
    Page<SystemLog> findByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 分页查询指定用户的日志，按创建时间降序。
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 日志分页结果
     */
    Page<SystemLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据操作名称模糊分页查询日志，按创建时间降序。
     *
     * @param action   操作名称关键字
     * @param pageable 分页参数
     * @return 日志分页结果
     */
    Page<SystemLog> findByActionContaining(String action, Pageable pageable);
}
