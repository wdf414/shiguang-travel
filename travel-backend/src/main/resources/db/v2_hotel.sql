-- Phase 2: 酒店预订系统数据库变更
USE lvyou;

-- 酒店表
CREATE TABLE IF NOT EXISTS `hotel` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '酒店名称',
    `city` VARCHAR(50) NOT NULL COMMENT '所在城市',
    `address` VARCHAR(500) NOT NULL COMMENT '详细地址',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `star` TINYINT DEFAULT 3 COMMENT '星级(3-5)',
    `tags` VARCHAR(500) COMMENT '标签JSON(如: 靠近地铁, 免费停车)',
    `facilities` TEXT COMMENT '设施JSON',
    `description` TEXT COMMENT '酒店介绍',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `images` TEXT COMMENT '图集JSON',
    `price_start` DECIMAL(10,2) COMMENT '起步价',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_city` (`city`),
    INDEX `idx_price` (`price_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店表';

-- 酒店房型表
CREATE TABLE IF NOT EXISTS `hotel_room` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `name` VARCHAR(100) NOT NULL COMMENT '房型名称',
    `image` VARCHAR(500) COMMENT '房型图片',
    `bed_type` VARCHAR(50) COMMENT '床型',
    `breakfast` VARCHAR(50) COMMENT '早餐情况',
    `people` INT DEFAULT 2 COMMENT '入住人数',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT DEFAULT 10 COMMENT '每日库存',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店房型表';

-- 酒店订单表
CREATE TABLE IF NOT EXISTS `hotel_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `hotel_name` VARCHAR(200) COMMENT '酒店名称',
    `room_id` BIGINT NOT NULL COMMENT '房型ID',
    `room_name` VARCHAR(100) COMMENT '房型名称',
    `check_in` DATE NOT NULL COMMENT '入住日期',
    `check_out` DATE NOT NULL COMMENT '离店日期',
    `night_count` INT COMMENT '居住晚数',
    `room_count` INT DEFAULT 1 COMMENT '房间数',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `contact_name` VARCHAR(50) COMMENT '入住人',
    `contact_phone` VARCHAR(20) COMMENT '手机号',
    `status` VARCHAR(20) DEFAULT 'paid' COMMENT '状态: pending, paid, cancelled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店订单表';

-- 插入测试数据
INSERT INTO `hotel` (`name`, `city`, `address`, `price_start`, `cover_image`, `star`, `tags`) VALUES
('三亚亚特兰蒂斯酒店', '三亚', '海棠湾海棠北路36号', 2888.00, 'https://picsum.photos/800/600?random=201', 5, '["海景","豪华","水上乐园"]'),
('大理古城漫步云端客栈', '大理', '古城人民路下段', 388.00, 'https://picsum.photos/800/600?random=202', 3, '["古香古色","近洱海"]'),
('上海和平饭店', '上海', '南京东路20号', 1888.00, 'https://picsum.photos/800/600?random=203', 5, '["历史建筑","外滩景观"]'),
('北京王府半岛酒店', '北京', '金鱼胡同8号', 2500.00, 'https://picsum.photos/800/600?random=204', 5, '["奢华","市中心"]');

INSERT INTO `hotel_room` (`hotel_id`, `name`, `price`, `bed_type`, `breakfast`) VALUES
(1, '海景大床房', 2888.00, '大床2m', '含双早'),
(1, '至尊海景套房', 5888.00, '特大床', '含双早+下午茶'),
(2, '观景双床房', 388.00, '双床1.2m', '无早'),
(3, '外滩景观房', 2200.00, '大床1.8m', '含单早');
