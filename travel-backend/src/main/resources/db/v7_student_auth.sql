-- =====================================================
-- 学生认证与专属权益功能 - 数据库表结构
-- =====================================================

-- 学生认证申请表
CREATE TABLE IF NOT EXISTS student_auth_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    
    -- 学生信息
    real_name VARCHAR(100) COMMENT '真实姓名',
    school_name VARCHAR(200) COMMENT '学校名称',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(50) COMMENT '年级，如2024级',
    student_id VARCHAR(100) COMMENT '学号',
    
    -- 认证材料
    id_card_front VARCHAR(500) COMMENT '身份证正面照片URL',
    id_card_back VARCHAR(500) COMMENT '身份证反面照片URL',
    student_card VARCHAR(500) COMMENT '学生证照片URL',
    chsi_screenshot VARCHAR(500) COMMENT '学信网学籍截图URL',
    
    -- 认证状态
    status TINYINT DEFAULT 0 COMMENT '认证状态：0-待审核 1-已通过 2-已拒绝',
    
    -- 审核信息
    reviewer_id BIGINT COMMENT '审核人ID',
    reviewer_name VARCHAR(100) COMMENT '审核人姓名',
    review_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    
    -- 有效期
    valid_start_date DATE COMMENT '认证有效期开始',
    valid_end_date DATE COMMENT '认证有效期结束',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_review_time (review_time),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生认证申请表';

-- 学生权益配置表
CREATE TABLE IF NOT EXISTS student_benefit_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL COMMENT '配置项',
    config_value VARCHAR(500) COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生权益配置表';

-- 初始化学生权益配置
INSERT IGNORE INTO student_benefit_config (config_key, config_value, description) VALUES
('student_discount_rate', '0.95', '学生折扣率（0.95表示95折）'),
('student_discount_min_amount', '10', '学生优惠最低金额'),
('student_discount_max_amount', '500', '学生优惠最高金额'),
('student_friendly_tags', '免押金,学生票,学生优惠', '学生友好标签'),
('student_auth_valid_years', '4', '学生认证有效期（年）');

-- 修改用户表，添加学生认证相关字段
-- 使用存储过程来检查列是否存在
DELIMITER $$

DROP PROCEDURE IF EXISTS AddUserColumns$$
CREATE PROCEDURE AddUserColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'user' AND COLUMN_NAME = 'student_auth_status') THEN
        ALTER TABLE user ADD COLUMN student_auth_status TINYINT DEFAULT 0 COMMENT '学生认证状态：0-未认证 1-待审核 2-已认证 3-已拒绝' AFTER is_student;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'user' AND COLUMN_NAME = 'student_auth_id') THEN
        ALTER TABLE user ADD COLUMN student_auth_id BIGINT COMMENT '学生认证申请ID' AFTER student_auth_status;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'user' AND COLUMN_NAME = 'student_discount_used_count') THEN
        ALTER TABLE user ADD COLUMN student_discount_used_count INT DEFAULT 0 COMMENT '学生优惠使用次数' AFTER student_auth_id;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'user' AND COLUMN_NAME = 'student_discount_total_saved') THEN
        ALTER TABLE user ADD COLUMN student_discount_total_saved DECIMAL(10,2) DEFAULT 0 COMMENT '学生优惠累计节省金额' AFTER student_discount_used_count;
    END IF;
END$$

DROP PROCEDURE IF EXISTS AddTourLineColumns$$
CREATE PROCEDURE AddTourLineColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'tour_line' AND COLUMN_NAME = 'student_price') THEN
        ALTER TABLE tour_line ADD COLUMN student_price DECIMAL(10,2) COMMENT '学生价' AFTER price;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'tour_line' AND COLUMN_NAME = 'student_friendly') THEN
        ALTER TABLE tour_line ADD COLUMN student_friendly TINYINT DEFAULT 0 COMMENT '是否学生友好：0-否 1-是' AFTER student_price;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'tour_line' AND COLUMN_NAME = 'student_benefits') THEN
        ALTER TABLE tour_line ADD COLUMN student_benefits VARCHAR(500) COMMENT '学生权益说明' AFTER student_friendly;
    END IF;
END$$

DROP PROCEDURE IF EXISTS AddHotelColumns$$
CREATE PROCEDURE AddHotelColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'hotel' AND COLUMN_NAME = 'student_friendly') THEN
        ALTER TABLE hotel ADD COLUMN student_friendly TINYINT DEFAULT 0 COMMENT '是否学生友好：0-否 1-是' AFTER star_rating;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'hotel' AND COLUMN_NAME = 'student_benefits') THEN
        ALTER TABLE hotel ADD COLUMN student_benefits VARCHAR(500) COMMENT '学生权益说明' AFTER student_friendly;
    END IF;
END$$

DROP PROCEDURE IF EXISTS AddActivityColumns$$
CREATE PROCEDURE AddActivityColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'activity' AND COLUMN_NAME = 'student_only') THEN
        ALTER TABLE activity ADD COLUMN student_only TINYINT DEFAULT 0 COMMENT '是否仅学生可见：0-否 1-是' AFTER status;
    END IF;
END$$

DROP PROCEDURE IF EXISTS AddOrderColumns$$
CREATE PROCEDURE AddOrderColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'order' AND COLUMN_NAME = 'student_discount_amount') THEN
        ALTER TABLE `order` ADD COLUMN student_discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '学生优惠金额' AFTER total_price;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'order' AND COLUMN_NAME = 'original_price') THEN
        ALTER TABLE `order` ADD COLUMN original_price DECIMAL(10,2) COMMENT '原价（优惠前）' AFTER student_discount_amount;
    END IF;
END$$

DROP PROCEDURE IF EXISTS AddHotelOrderColumns$$
CREATE PROCEDURE AddHotelOrderColumns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'hotel_order' AND COLUMN_NAME = 'student_discount_amount') THEN
        ALTER TABLE hotel_order ADD COLUMN student_discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '学生优惠金额' AFTER total_price;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_NAME = 'hotel_order' AND COLUMN_NAME = 'original_price') THEN
        ALTER TABLE hotel_order ADD COLUMN original_price DECIMAL(10,2) COMMENT '原价（优惠前）' AFTER student_discount_amount;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL AddUserColumns();
CALL AddTourLineColumns();
CALL AddHotelColumns();
CALL AddActivityColumns();
CALL AddOrderColumns();
CALL AddHotelOrderColumns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS AddUserColumns;
DROP PROCEDURE IF EXISTS AddTourLineColumns;
DROP PROCEDURE IF EXISTS AddHotelColumns;
DROP PROCEDURE IF EXISTS AddActivityColumns;
DROP PROCEDURE IF EXISTS AddOrderColumns;
DROP PROCEDURE IF EXISTS AddHotelOrderColumns;
