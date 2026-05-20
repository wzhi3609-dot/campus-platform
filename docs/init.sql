-- ============================================
-- 校园互助交流平台 - MySQL 初始化脚本
-- 使用前请先创建数据库：CREATE DATABASE campus CHARACTER SET utf8mb4;
-- ============================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL COMMENT 'STUDENT / TEACHER',
    student_id VARCHAR(50) UNIQUE COMMENT '学号',
    teacher_id VARCHAR(50) UNIQUE COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像',
    role VARCHAR(20) DEFAULT 'USER' COMMENT 'USER / ADMIN',
    enabled BIT DEFAULT 0 COMMENT '是否通过审核',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_type (user_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    tags VARCHAR(200) COMMENT '标签(逗号分隔)',
    user_id BIGINT NOT NULL COMMENT '提问者ID',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    answer_count INT DEFAULT 0 COMMENT '回答数',
    resolved BIT DEFAULT 0 COMMENT '是否解决',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_resolved (resolved),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答-问题表';

CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '回答内容',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    user_id BIGINT NOT NULL COMMENT '回答者ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    accepted BIT DEFAULT 0 COMMENT '是否采纳',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_question_id (question_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答-回答表';

CREATE TABLE trade_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    category VARCHAR(100) COMMENT '分类',
    images VARCHAR(500) COMMENT '图片URL(逗号分隔)',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    location VARCHAR(200) COMMENT '位置',
    status VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/SOLD/TAKEN_DOWN',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手交易表';

CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT 'HTML富文本内容',
    user_id BIGINT NOT NULL COMMENT '发帖者ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖表';

CREATE TABLE lost_found_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL COMMENT 'LOST(寻物)/FOUND(招领)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    location VARCHAR(200) COMMENT '地点',
    image VARCHAR(500) COMMENT '图片URL',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_person VARCHAR(100) COMMENT '联系人',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESOLVED',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物招领表';

CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '评论内容',
    user_id BIGINT NOT NULL COMMENT '评论者ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型(post/question/trade)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收者ID',
    type VARCHAR(50) COMMENT '通知类型',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read BIT DEFAULT 0 COMMENT '是否已读',
    related_id BIGINT COMMENT '关联业务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    pinned BIT DEFAULT 0 COMMENT '是否置顶',
    admin_id BIGINT NOT NULL COMMENT '发布者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_type VARCHAR(20) COMMENT '用户类型',
    user_name VARCHAR(50) COMMENT '用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作',
    target VARCHAR(200) COMMENT '目标',
    detail TEXT COMMENT '详情',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型(question/post)',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL COMMENT '举报者ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    reason VARCHAR(500) NOT NULL COMMENT '举报原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/RESOLVED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 默认管理员（密码 admin123 的 BCrypt 哈希）
INSERT INTO users (username, password, user_type, teacher_id, name, email, phone, role, enabled)
VALUES ('ADMIN001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', 'ADMIN001', '管理员', 'admin@campus.com', '13800000000', 'ADMIN', 1);
