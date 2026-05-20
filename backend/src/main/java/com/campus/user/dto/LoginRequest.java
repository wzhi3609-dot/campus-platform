package com.campus.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO，封装用户登录时提交的账号、密码和用户类型
 */
public class LoginRequest {

    @NotBlank(message = "请输入学号/工号")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    private String userType;

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
