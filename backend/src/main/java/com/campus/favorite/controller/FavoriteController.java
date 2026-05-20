package com.campus.favorite.controller;

import com.campus.common.Result;
import com.campus.favorite.entity.Favorite;
import com.campus.favorite.repository.FavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏控制器，提供收藏切换、状态检查、数量统计和列表查询接口。
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;

    public FavoriteController(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 切换收藏状态：若已收藏则取消收藏，否则添加收藏。
     *
     * @param body 请求体，包含 targetType、targetId
     * @param auth 认证信息
     * @return 包含 favorited 布尔值的 Map
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());

        var existing = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return Result.success(Map.of("favorited", false));
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setTargetType(targetType);
            f.setTargetId(targetId);
            favoriteRepository.save(f);
            return Result.success(Map.of("favorited", true));
        }
    }

    /**
     * 检查当前用户是否已收藏指定目标。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param auth       认证信息
     * @return 包含 favorited 布尔值的 Map
     */
    @GetMapping("/check")
    public Result<Map<String, Boolean>> check(@RequestParam String targetType, @RequestParam Long targetId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boolean exists = favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        return Result.success(Map.of("favorited", exists));
    }

    /**
     * 统计指定目标的收藏数量。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 包含 count 的 Map
     */
    @GetMapping("/count")
    public Result<Map<String, Long>> count(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.success(Map.of("count", favoriteRepository.countByTargetTypeAndTargetId(targetType, targetId)));
    }

    /**
     * 分页获取当前用户的某类收藏列表。
     *
     * @param auth       认证信息
     * @param targetType 目标类型
     * @param page       页码
     * @param size       每页条数
     * @return 收藏分页结果
     */
    @GetMapping
    public Result<Page<Favorite>> getFavorites(Authentication auth,
                                               @RequestParam String targetType,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(favoriteRepository.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, targetType,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
