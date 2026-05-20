package com.campus.user.controller;

import com.campus.common.Result;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器，提供注册、登录和管理员登录的 REST 接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册接口
     * @param request 注册请求
     * @return 注册结果（包含用户信息）
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录接口
     * @param request 登录请求
     * @return 登录结果（包含 token 和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * 管理员登录接口
     * @param body 包含 username 和 password 的请求体
     * @return 登录结果（包含 token 和用户信息）
     */
    @PostMapping("/admin-login")
    public Result<LoginResponse> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.error(400, "请输入账号和密码");
        }
        return Result.success(userService.adminLogin(username, password));
    }

    /**
     * 密码重置接口：验证姓名、手机号和学号/工号，返回新密码
     */
    @PostMapping("/reset-password")
    public Result<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String phone = body.get("phone");
        String studentId = body.get("studentId");
        String teacherId = body.get("teacherId");
        if (name == null || name.isBlank()) {
            return Result.error(400, "请输入姓名");
        }
        if (phone == null || phone.isBlank()) {
            return Result.error(400, "请输入注册手机号");
        }
        String newPassword = userService.resetPassword(name, phone, studentId, teacherId);
        Map<String, String> result = new HashMap<>();
        result.put("newPassword", newPassword);
        return Result.success(result);
    }
}
