# 数据库设计文档

## 总览

共 11 张表，JPA 自动建表（`ddl-auto: update`），也可手动执行 SQL 初始化。

```
users              用户表（学生/教师/管理员）
questions          问答-问题表
answers            问答-回答表
trade_items        二手交易表
posts              论坛帖表
lost_found_items   失物招领表
comments           评论表（通用）
favorites          收藏表（通用）
likes              点赞表（通用）
notifications      通知表
announcements      公告表
system_logs        操作日志表
reports            举报表
```

---

## 1. users — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 登录名（等于学号或工号） |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| user_type | VARCHAR(20) | NOT NULL | `STUDENT` / `TEACHER` |
| student_id | VARCHAR(50) | UNIQUE | 学号（学生必填） |
| teacher_id | VARCHAR(50) | UNIQUE | 工号（教师必填） |
| name | VARCHAR(50) | NOT NULL | 真实姓名 |
| email | VARCHAR(100) | | 邮箱 |
| phone | VARCHAR(20) | | 手机号 |
| avatar | VARCHAR(500) | | 头像 URL |
| role | VARCHAR(20) | DEFAULT 'USER' | `USER` / `ADMIN` |
| enabled | BIT | DEFAULT 0 | 是否通过审核 |
| created_at | DATETIME | | 注册时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 2. questions — 问题表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 问题 ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容 |
| tags | VARCHAR(200) | | 标签（逗号分隔） |
| user_id | BIGINT | NOT NULL | 提问者 ID |
| view_count | INT | DEFAULT 0 | 浏览数 |
| like_count | INT | DEFAULT 0 | 点赞数 |
| answer_count | INT | DEFAULT 0 | 回答数 |
| resolved | BIT | DEFAULT 0 | 是否已解决 |
| created_at | DATETIME | | 发布时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 3. answers — 回答表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 回答 ID |
| content | TEXT | NOT NULL | 回答内容 |
| question_id | BIGINT | NOT NULL | 所属问题 ID |
| user_id | BIGINT | NOT NULL | 回答者 ID |
| like_count | INT | DEFAULT 0 | 点赞数 |
| accepted | BIT | DEFAULT 0 | 是否被采纳 |
| created_at | DATETIME | | 回答时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 4. trade_items — 二手交易表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 商品 ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | | 描述 |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| category | VARCHAR(100) | | 分类 |
| images | VARCHAR(500) | | 图片 URL（逗号分隔） |
| contact_person | VARCHAR(100) | | 联系人 |
| contact_phone | VARCHAR(20) | | 联系电话 |
| location | VARCHAR(200) | | 位置 |
| status | VARCHAR(20) | DEFAULT 'AVAILABLE' | `AVAILABLE` / `SOLD` / `TAKEN_DOWN` |
| user_id | BIGINT | NOT NULL | 发布者 ID |
| view_count | INT | DEFAULT 0 | 浏览数 |
| created_at | DATETIME | | 发布时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 5. posts — 论坛帖表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 帖子 ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | HTML 富文本内容（经 XSS 过滤） |
| user_id | BIGINT | NOT NULL | 发帖者 ID |
| status | VARCHAR(20) | DEFAULT 'PENDING' | `PENDING` / `APPROVED` / `REJECTED` |
| view_count | INT | DEFAULT 0 | 浏览数 |
| like_count | INT | DEFAULT 0 | 点赞数 |
| created_at | DATETIME | | 发帖时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 6. lost_found_items — 失物招领表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| type | VARCHAR(20) | NOT NULL | `LOST`(寻物) / `FOUND`(招领) |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | | 描述 |
| location | VARCHAR(200) | | 地点 |
| image | VARCHAR(500) | | 图片 URL |
| contact_phone | VARCHAR(20) | | 联系电话 |
| contact_person | VARCHAR(100) | | 联系人 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | `ACTIVE` / `RESOLVED` |
| user_id | BIGINT | NOT NULL | 发布者 ID |
| created_at | DATETIME | | 发布时间 |
| updated_at | DATETIME | | 更新时间 |

---

## 7. comments — 评论表（通用）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 评论 ID |
| content | TEXT | NOT NULL | 评论内容 |
| user_id | BIGINT | NOT NULL | 评论者 ID |
| target_type | VARCHAR(20) | NOT NULL | 目标类型（post/question/trade） |
| target_id | BIGINT | NOT NULL | 目标 ID |
| created_at | DATETIME | | 评论时间 |

---

## 8. favorites — 收藏表（通用）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 收藏 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| target_type | VARCHAR(20) | NOT NULL | 目标类型 |
| target_id | BIGINT | NOT NULL | 目标 ID |
| created_at | DATETIME | | 收藏时间 |

唯一约束：`(user_id, target_type, target_id)`

---

## 9. likes — 点赞表（通用）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 点赞 ID |
| user_id | BIGINT | NOT NULL | 用户 ID |
| target_type | VARCHAR(20) | NOT NULL | 目标类型（question/post） |
| target_id | BIGINT | NOT NULL | 目标 ID |
| created_at | DATETIME | | 点赞时间 |

唯一约束：`(user_id, target_type, target_id)` — 确保同一用户对同一目标只能点赞一次

---

## 10. notifications — 通知表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 通知 ID |
| user_id | BIGINT | NOT NULL | 接收者 ID |
| type | VARCHAR(50) | | 通知类型 |
| title | VARCHAR(200) | | 标题 |
| content | TEXT | | 内容 |
| is_read | BIT | DEFAULT 0 | 是否已读 |
| related_id | BIGINT | | 关联业务 ID |
| created_at | DATETIME | | 通知时间 |

---

## 11. reports — 举报表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 举报 ID |
| reporter_id | BIGINT | NOT NULL | 举报者 ID |
| target_type | VARCHAR(20) | NOT NULL | 目标类型 |
| target_id | BIGINT | NOT NULL | 目标 ID |
| reason | VARCHAR(500) | NOT NULL | 举报原因 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | `PENDING` / `RESOLVED` |
| created_at | DATETIME | | 举报时间 |

---

## 实体关系（文字版）

```
users (1) ──< questions (N)         发布问题
users (1) ──< answers (N)           回答问题
users (1) ──< trade_items (N)       发布二手
users (1) ──< posts (N)             发表帖子（需审核）
users (1) ──< lost_found_items (N)  发布失物招领
users (1) ──< comments (N)          发表评论
users (1) ──< likes (N)             点赞
users (1) ──< favorites (N)         收藏
users (1) ──< reports (N)           举报
users (1) ──< notifications (N)     接收通知
questions (1) ──< answers (N)       问题下的回答

comments  → (target_type, target_id) → posts / questions / trade_items
favorites → (target_type, target_id) → posts / questions
likes     → (target_type, target_id) → posts / questions
reports   → (target_type, target_id) → posts / questions / trade_items
```

## 设计说明

- 所有业务表通过 `user_id` 与 `users` 表关联，采用 `Long userId` 手动关联（非 JPA `@ManyToOne`），减少查询耦合
- 通用关联表（comments/favorites/likes/reports）通过 `(target_type, target_id)` 组合实现多态关联
- 点赞表 `likes` 有唯一约束防止同一用户重复点赞
- 收藏表 `favorites` 有唯一约束，toggle 模式（收藏/取消收藏）
