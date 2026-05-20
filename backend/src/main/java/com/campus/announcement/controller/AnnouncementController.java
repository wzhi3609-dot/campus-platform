package com.campus.announcement.controller;

import com.campus.announcement.entity.Announcement;
import com.campus.announcement.repository.AnnouncementRepository;
import com.campus.common.Result;
import com.campus.common.helper.UserNameHelper;
import com.campus.log.annotation.Loggable;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公告控制器，提供公告的查询、创建和删除接口。
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final UserNameHelper userNameHelper;

    public AnnouncementController(AnnouncementRepository announcementRepository,
                                  UserRepository userRepository, UserNameHelper userNameHelper) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.userNameHelper = userNameHelper;
    }

    /**
     * 获取所有公告，返回置顶列表和近期列表。
     *
     * @return 包含 pinned（置顶）和 recent（近期）的 Map
     */
    @GetMapping
    @Cacheable("announcements")
    public Result<Map<String, Object>> getAll() {
        List<Announcement> pinned = announcementRepository.findByPinnedTrueOrderByCreatedAtDesc();
        Page<Announcement> recent = announcementRepository.findByOrderByPinnedDescCreatedAtDesc(
                PageRequest.of(0, 10));
        pinned.forEach(a -> userNameHelper.setUserName(a.getAdminId(), a::setAdminName));
        recent.forEach(a -> userNameHelper.setUserName(a.getAdminId(), a::setAdminName));
        return Result.success(Map.of("pinned", pinned, "recent", recent.getContent()));
    }

    /**
     * 创建新公告（仅管理员可操作）。
     *
     * @param body 请求体，包含 title、content、pinned
     * @param auth 认证信息
     * @return 创建的公告
     */
    @PostMapping
    @Loggable(action = "发布公告", target = "公告")
    @CacheEvict(value = "announcements", allEntries = true)
    public Result<Announcement> create(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User admin = userRepository.findById(userId).orElseThrow();
        if (!"ADMIN".equals(admin.getRole())) throw new RuntimeException("无权限");

        Announcement a = new Announcement();
        a.setTitle(body.get("title"));
        a.setContent(body.get("content"));
        a.setPinned("true".equals(body.get("pinned")));
        a.setAdminId(userId);
        a = announcementRepository.save(a);
        userNameHelper.setUserName(userId, a::setAdminName);
        return Result.success(a);
    }

    /**
     * 删除指定公告。
     *
     * @param id   公告ID
     * @param auth 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Loggable(action = "删除公告", target = "公告")
    @CacheEvict(value = "announcements", allEntries = true)
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        announcementRepository.deleteById(id);
        return Result.success();
    }
}
