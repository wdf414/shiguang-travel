-- =====================================================
-- 内容审核功能 - 数据库表结构
-- =====================================================

-- 审核记录表
CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_type VARCHAR(50) NOT NULL COMMENT '内容类型',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    content_title VARCHAR(255) COMMENT '内容标题',
    content_author_id BIGINT COMMENT '内容作者ID',
    old_status TINYINT COMMENT '审核前状态',
    new_status TINYINT NOT NULL COMMENT '审核后状态',
    review_type TINYINT DEFAULT 1 COMMENT '审核类型',
    reviewer_id BIGINT COMMENT '审核人ID',
    reviewer_name VARCHAR(100) COMMENT '审核人姓名',
    review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '驳回原因',
    auto_review_result VARCHAR(50) COMMENT '自动审核结果',
    sensitive_words VARCHAR(500) COMMENT '检测到的敏感词',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_content_type_id (content_type, content_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_review_time (review_time),
    INDEX idx_new_status (new_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容审核记录表';

-- 敏感词库表
CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    word_type TINYINT DEFAULT 1 COMMENT '词类型',
    category VARCHAR(50) COMMENT '分类',
    match_mode TINYINT DEFAULT 1 COMMENT '匹配模式',
    replace_word VARCHAR(100) COMMENT '替换词',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_word (word),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库表';

-- 审核配置表
CREATE TABLE IF NOT EXISTS review_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL COMMENT '配置项',
    config_value VARCHAR(500) COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核配置表';

-- 初始化敏感词（示例数据）
INSERT IGNORE INTO sensitive_word (word, word_type, category, match_mode) VALUES
('反动', 1, 'politics', 1),
('分裂', 1, 'politics', 1),
('暴乱', 1, 'politics', 1),
('暴力', 2, 'violence', 1),
('恐怖', 2, 'violence', 1),
('杀人', 1, 'violence', 1),
('色情', 1, 'porn', 1),
('淫秽', 1, 'porn', 1),
('赌博', 1, 'porn', 1),
('诈骗', 1, 'fraud', 1),
('传销', 1, 'fraud', 1),
('洗钱', 1, 'fraud', 1),
('加微信', 2, 'ad', 1),
('加QQ', 2, 'ad', 1),
('联系方式', 2, 'ad', 2),
('电话', 2, 'ad', 2),
('微信', 2, 'ad', 2);

-- 初始化审核配置
INSERT IGNORE INTO review_config (config_key, config_value, description) VALUES
('auto_review_enabled', 'true', '是否开启自动审核'),
('auto_review_travel_note', 'true', '游记是否开启自动审核'),
('auto_review_companion', 'true', '结伴是否开启自动审核'),
('auto_review_comment', 'true', '评论是否开启自动审核'),
('sensitive_word_action', 'block', '检测到敏感词时的操作'),
('max_reject_reason_length', '500', '最大驳回原因长度'),
('pending_review_expire_days', '7', '待审核内容过期天数');

-- 修改现有表：为travel_note表添加审核相关字段
ALTER TABLE travel_note 
ADD COLUMN IF NOT EXISTS review_time DATETIME COMMENT '审核时间' AFTER status,
ADD COLUMN IF NOT EXISTS reviewer_id BIGINT COMMENT '审核人ID' AFTER review_time,
ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(500) COMMENT '驳回原因' AFTER reviewer_id;

-- 修改现有表：为companion表添加审核相关字段
ALTER TABLE companion 
ADD COLUMN IF NOT EXISTS review_time DATETIME COMMENT '审核时间' AFTER status,
ADD COLUMN IF NOT EXISTS reviewer_id BIGINT COMMENT '审核人ID' AFTER review_time,
ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(500) COMMENT '驳回原因' AFTER reviewer_id;

-- 修改现有表：为travel_note_comment表添加审核相关字段
ALTER TABLE travel_note_comment 
ADD COLUMN IF NOT EXISTS review_time DATETIME COMMENT '审核时间' AFTER status,
ADD COLUMN IF NOT EXISTS reviewer_id BIGINT COMMENT '审核人ID' AFTER review_time,
ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(500) COMMENT '驳回原因' AFTER reviewer_id;
