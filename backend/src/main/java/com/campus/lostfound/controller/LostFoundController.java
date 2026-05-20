package com.campus.lostfound.controller;

import com.campus.common.Result;
import com.campus.log.annotation.Loggable;
import com.campus.lostfound.dto.LostFoundRequest;
import com.campus.lostfound.entity.LostFoundItem;
import com.campus.lostfound.service.LostFoundService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 失物招领控制器，提供失物招领信息的发布、编辑、查询和解决等 REST 接口
 */
@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {

    private final LostFoundService lostFoundService;

    public LostFoundController(LostFoundService lostFoundService) {
        this.lostFoundService = lostFoundService;
    }

    /**
     * 发布失物招领信息
     * @param request        失物招领请求
     * @param authentication 认证信息
     * @return 创建的记录
     */
    @PostMapping
    @Loggable(action = "发布失物招领", target = "失物招领")
    public Result<LostFoundItem> createItem(@Valid @RequestBody LostFoundRequest request,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(lostFoundService.createItem(request, userId));
    }

    /**
     * 编辑失物招领信息
     * @param id             记录ID
     * @param request        失物招领请求
     * @param authentication 认证信息
     * @return 更新后的记录
     */
    @PutMapping("/{id}")
    @Loggable(action = "编辑失物招领", target = "失物招领")
    public Result<LostFoundItem> updateItem(@PathVariable Long id,
                                            @Valid @RequestBody LostFoundRequest request,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(lostFoundService.updateItem(id, userId, request));
    }

    /**
     * 分页获取失物招领列表，可按类型筛选
     * @param page 页码
     * @param size 每页条数
     * @param type 类型（LOST/FOUND，可选）
     * @return 分页列表
     */
    @GetMapping
    public Result<Page<LostFoundItem>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return Result.success(lostFoundService.searchItems(page, size, type, keyword));
        }
        return Result.success(lostFoundService.getItems(page, size, type));
    }

    /**
     * 根据ID获取失物招领详情
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/{id}")
    public Result<LostFoundItem> getItem(@PathVariable Long id) {
        return Result.success(lostFoundService.getItem(id));
    }

    /**
     * 将失物招领标记为已解决
     * @param id             记录ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PutMapping("/{id}/resolve")
    @Loggable(action = "解决失物招领", target = "失物招领")
    public Result<Void> resolveItem(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        lostFoundService.resolveItem(id, userId);
        return Result.success();
    }

    /**
     * 删除失物招领记录
     * @param id             记录ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        lostFoundService.deleteItem(id, userId);
        return Result.success();
    }
}
