-- 校园拼团/结伴升级
USE lvyou;

-- 用户表新增校园相关字段
ALTER TABLE `user` ADD COLUMN `school` VARCHAR(100) DEFAULT NULL COMMENT '学校名称';
ALTER TABLE `user` ADD COLUMN `major` VARCHAR(100) DEFAULT NULL COMMENT '专业';
ALTER TABLE `user` ADD COLUMN `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级，如2024级';

-- 结伴游表新增字段
ALTER TABLE `companion` ADD COLUMN `school` VARCHAR(100) DEFAULT NULL COMMENT '限定学校（可选）';
ALTER TABLE `companion` ADD COLUMN `group_qrcode` VARCHAR(500) DEFAULT NULL COMMENT '群聊二维码图片链接';

-- 校园领队表
CREATE TABLE IF NOT EXISTS `campus_leader` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `school` VARCHAR(100) NOT NULL COMMENT '学校',
    `major` VARCHAR(100) COMMENT '专业',
    `grade` VARCHAR(20) COMMENT '年级',
    `description` TEXT COMMENT '领队简介',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已认证 2-已拒绝',
    `commission_rate` DECIMAL(5,2) DEFAULT 5.00 COMMENT '佣金比例(%)',
    `total_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计佣金',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_school` (`school`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园领队表';

-- 领队佣金记录表
CREATE TABLE IF NOT EXISTS `leader_commission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `leader_id` BIGINT NOT NULL COMMENT '领队ID',
    `companion_id` BIGINT NOT NULL COMMENT '结伴游ID',
    `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
    `commission_rate` DECIMAL(5,2) COMMENT '佣金比例(%)',
    `commission_amount` DECIMAL(10,2) COMMENT '佣金金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待结算 1-已结算',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_leader_id` (`leader_id`),
    INDEX `idx_companion_id` (`companion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领队佣金记录表';
