package com.campus.user.dto;

/**
 * 登录响应 DTO，包含 JWT token 和用户基本信息
 */
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserInfo user;

    /**
     * 构造登录响应
     * @param token JWT 令牌
     * @param user  用户信息
     */
    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    /**
     * 用户基本信息内部类，用于返回给前端
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String userType;
        private String studentId;
        private String teacherId;
        private String name;
        private String email;
        private String phone;
        private String avatar;
        private String role;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getTeacherId() { return teacherId; }
        public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
