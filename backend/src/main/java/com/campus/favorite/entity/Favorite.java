package com.campus.favorite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 收藏实体，对应 favorites 表。
 * 记录用户对某一目标（帖子、二手物品等）的收藏关系，同一用户对同一目标只能收藏一次。
 */
@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "targetType", "targetId"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 持久化前自动设置创建时间。
     */
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
