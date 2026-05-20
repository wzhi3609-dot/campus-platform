package com.campus.notification.controller;

import com.campus.common.Result;
import com.campus.notification.entity.Notification;
import com.campus.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器，提供通知列表、未读统计和标记已读接口。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 分页获取当前用户的通知列表。
     *
     * @param auth 认证信息
     * @param page 页码
     * @param size 每页条数
     * @return 通知分页结果
     */
    @GetMapping
    public Result<Page<Notification>> getNotifications(Authentication auth,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationService.getNotifications(userId, page, size));
    }

    /**
     * 获取当前用户的未读通知数量。
     *
     * @param auth 认证信息
     * @return 包含 count 的 Map
     */
    @GetMapping("/unread")
    public Result<Map<String, Long>> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(Map.of("count", notificationService.unreadCount(userId)));
    }

    /**
     * 将指定通知标记为已读。
     *
     * @param id   通知ID
     * @param auth 认证信息
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markRead(id, userId);
        return Result.success();
    }
}
