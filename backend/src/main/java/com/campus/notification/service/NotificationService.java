package com.campus.notification.service;

import com.campus.notification.entity.Notification;
import com.campus.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 通知业务服务，提供通知的创建、查询、统计和标记已读功能。
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 创建一条通知。
     *
     * @param userId    接收通知的用户ID
     * @param type      通知类型
     * @param title     通知标题
     * @param content   通知内容
     * @param relatedId 关联的业务ID
     */
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        notificationRepository.save(n);
    }

    /**
     * 分页查询用户的通知列表。
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 通知分页结果
     */
    public Page<Notification> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * 获取用户未读通知数量。
     *
     * @param userId 用户ID
     * @return 未读通知数
     */
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * 将指定通知标记为已读（需校验通知所属用户）。
     *
     * @param id     通知ID
     * @param userId 当前用户ID
     */
    public void markRead(Long id, Long userId) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUserId().equals(userId)) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
}
