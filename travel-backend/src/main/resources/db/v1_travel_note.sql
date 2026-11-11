-- Phase 1: 游记攻略系统数据库变更
-- 包括游记表 (travel_note) 和 评论表 (travel_note_comment)

USE lvyou;

-- 游记表
CREATE TABLE IF NOT EXISTS `travel_note` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '游记标题',
    `content` LONGTEXT NOT NULL COMMENT '游记内容(富文本)',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `destination` VARCHAR(50) COMMENT '关联目的地',
    `locations` VARCHAR(500) COMMENT '途径地点JSON',
    `travel_date` DATE COMMENT '出发日期',
    `days` INT COMMENT '游玩天数',
    `cost` DECIMAL(10,2) COMMENT '人均花费',
    `people` VARCHAR(50) COMMENT '人物(情侣/亲子/朋友)',
    `views` INT DEFAULT 0 COMMENT '浏览量',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `favorites` INT DEFAULT 0 COMMENT '收藏数',
    `comments` INT DEFAULT 0 COMMENT '评论数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-发布 2-审核中 3-违规',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_destination` (`destination`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记表';

-- 游记评论表
CREATE TABLE IF NOT EXISTS `travel_note_comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `note_id` BIGINT NOT NULL COMMENT '游记ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID(回复)',
    `reply_to_uid` BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-隐藏 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_note_id` (`note_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记评论表';

-- 游记点赞表 (可选，用于记录用户是否赞过)
CREATE TABLE IF NOT EXISTS `travel_note_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `note_id` BIGINT NOT NULL COMMENT '游记ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_note_user` (`note_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记点赞记录';

-- 插入一些测试数据
INSERT INTO `travel_note` (`title`, `content`, `cover_image`, `user_id`, `destination`, `views`, `likes`, `status`) VALUES
('七月在云南，遇见最美的春天', '<p>这是测试游记内容...</p>', 'https://picsum.photos/800/400?random=101', 1, '云南', 1205, 56, 1),
('三亚潜水初体验，太美了！', '<p>潜水真的很不可思议...</p>', 'https://picsum.photos/800/400?random=102', 2, '海南', 890, 32, 1);
