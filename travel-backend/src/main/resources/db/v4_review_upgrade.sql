-- 1. 用户表增加学生标识
ALTER TABLE user ADD COLUMN is_student TINYINT DEFAULT 0 COMMENT '0-非学生 1-学生';

-- 2. 评价表扩展
ALTER TABLE review ADD COLUMN hotel_id BIGINT COMMENT '酒店ID';
ALTER TABLE review ADD COLUMN guide_id BIGINT COMMENT '导游ID';
ALTER TABLE review ADD COLUMN tags VARCHAR(255) COMMENT '评价标签';

-- 3. 酒店表增加评论统计
ALTER TABLE hotel ADD COLUMN review_count INT DEFAULT 0 COMMENT '评论数';

-- 4. 导游表增加评分和评论统计
ALTER TABLE guide ADD COLUMN rating DECIMAL(2,1) DEFAULT 0 COMMENT '评分';
ALTER TABLE guide ADD COLUMN review_count INT DEFAULT 0 COMMENT '评论数';

-- 5. 投诉建议表
CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type TINYINT DEFAULT 1 COMMENT '1-建议 2-投诉',
    content TEXT NOT NULL COMMENT '内容',
    contact VARCHAR(100) COMMENT '联系方式',
    reply TEXT COMMENT '回复内容',
    status TINYINT DEFAULT 0 COMMENT '0-待处理 1-已回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    reply_time DATETIME COMMENT '回复时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉建议表';
