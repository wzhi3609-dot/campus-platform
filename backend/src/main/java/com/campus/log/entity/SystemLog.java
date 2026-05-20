package com.campus.log.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统日志实体，对应 system_logs 表。
 * 存储用户操作的历史记录，用于审计和运营分析。
 */
@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 20)
    private String userType;

    @Column(length = 50)
    private String userName;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 200)
    private String target;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length = 50)
    private String ip;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 持久化前自动设置创建时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
