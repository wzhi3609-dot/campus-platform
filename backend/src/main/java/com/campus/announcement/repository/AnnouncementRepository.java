package com.campus.announcement.repository;

import com.campus.announcement.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 公告数据访问接口，提供公告的数据库操作。
 */
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    /**
     * 查询所有已置顶公告，按创建时间降序。
     */
    List<Announcement> findByPinnedTrueOrderByCreatedAtDesc();

    /**
     * 分页查询公告，置顶优先，再按创建时间降序。
     */
    Page<Announcement> findByOrderByPinnedDescCreatedAtDesc(Pageable pageable);
}
