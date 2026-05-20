package com.campus.user.dto;

/**
 * 个人资料更新请求 DTO，封装可修改的用户信息字段
 */
public class ProfileUpdateRequest {
    private String name;
    private String phone;
    private String email;
    private String avatar;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
