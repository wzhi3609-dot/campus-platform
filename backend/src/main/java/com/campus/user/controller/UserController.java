package com.campus.user.controller;

import com.campus.common.Result;
import com.campus.log.annotation.Loggable;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.ProfileUpdateRequest;
import com.campus.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器，提供获取当前用户、更新资料和修改密码的 REST 接口
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户的信息
     * @param authentication 认证信息
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 更新个人资料
     * @param authentication 认证信息
     * @param request       资料更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @Loggable(action = "修改个人资料", target = "用户")
    public Result<LoginResponse.UserInfo> updateProfile(Authentication authentication, @RequestBody ProfileUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.updateProfile(userId, request.getName(), request.getPhone(), request.getEmail(), request.getAvatar()));
    }

    /**
     * 修改当前用户密码
     * @param authentication 认证信息
     * @param request       密码修改请求
     * @return 操作结果
     */
    @PutMapping("/password")
    @Loggable(action = "修改密码", target = "用户")
    public Result<Void> changePassword(Authentication authentication, @RequestBody com.campus.user.dto.PasswordChangeRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
