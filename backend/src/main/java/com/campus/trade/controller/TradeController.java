package com.campus.trade.controller;

import com.campus.common.Result;
import com.campus.log.annotation.Loggable;
import com.campus.trade.dto.TradeItemRequest;
import com.campus.trade.entity.TradeItem;
import com.campus.trade.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 二手交易控制器，提供商品发布、编辑、查询和状态管理的 REST 接口
 */
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * 发布二手商品
     * @param request        商品请求
     * @param authentication 认证信息
     * @return 创建的商品
     */
    @PostMapping
    @Loggable(action = "发布二手", target = "商品")
    public Result<TradeItem> createItem(@Valid @RequestBody TradeItemRequest request,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(tradeService.createItem(request, userId));
    }

    /**
     * 编辑二手商品
     * @param id             商品ID
     * @param request        商品请求
     * @param authentication 认证信息
     * @return 更新后的商品
     */
    @PutMapping("/{id}")
    @Loggable(action = "编辑二手", target = "商品")
    public Result<TradeItem> updateItem(@PathVariable Long id,
                                        @Valid @RequestBody TradeItemRequest request,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(tradeService.updateItem(id, userId, request));
    }

    /**
     * 分页获取商品列表，支持按关键词和分类筛选
     * @param page     页码
     * @param size     每页条数
     * @param keyword  搜索关键词（可选）
     * @param category 商品分类（可选）
     * @return 商品分页列表
     */
    @GetMapping
    public Result<Page<TradeItem>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(tradeService.getItems(page, size, keyword, category));
    }

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result<TradeItem> getItem(@PathVariable Long id) {
        return Result.success(tradeService.getItem(id));
    }

    /**
     * 更新商品状态
     * @param id             商品ID
     * @param status         新状态
     * @param authentication 认证信息
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Loggable(action = "更新二手状态", target = "商品")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tradeService.updateStatus(id, status, userId);
        return Result.success();
    }

    /**
     * 删除商品
     * @param id             商品ID
     * @param authentication 认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Loggable(action = "删除二手", target = "商品")
    public Result<Void> deleteItem(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tradeService.deleteItem(id, userId);
        return Result.success();
    }
}
