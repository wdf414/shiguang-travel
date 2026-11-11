-- 使用数据库
USE lvyou;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 旅游线路表
CREATE TABLE IF NOT EXISTS `tour_line` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '线路标题',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `images` TEXT COMMENT '图片列表JSON',
    `destination` VARCHAR(50) NOT NULL COMMENT '目的地',
    `departure_city` VARCHAR(50) NOT NULL COMMENT '出发城市',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `days` INT NOT NULL COMMENT '行程天数',
    `description` TEXT COMMENT '线路描述',
    `schedule` TEXT COMMENT '行程安排JSON',
    `includes` TEXT COMMENT '费用包含JSON',
    `excludes` TEXT COMMENT '费用不含JSON',
    `stock` INT DEFAULT 100 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `review_count` INT DEFAULT 0 COMMENT '评价数',
    `category` VARCHAR(50) COMMENT '分类',
    `tags` VARCHAR(200) COMMENT '标签',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_destination` (`destination`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游线路表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `line_id` BIGINT NOT NULL COMMENT '线路ID',
    `line_name` VARCHAR(200) COMMENT '线路名称',
    `line_cover` VARCHAR(500) COMMENT '线路封面',
    `departure_date` DATE NOT NULL COMMENT '出发日期',
    `traveler_count` INT NOT NULL COMMENT '出行人数',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `contact_name` VARCHAR(50) COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) COMMENT '联系人电话',
    `remark` VARCHAR(500) COMMENT '备注',
    `pay_type` VARCHAR(20) COMMENT '支付方式',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, paid, completed, cancelled',
    `pay_time` DATETIME COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 出行人表
CREATE TABLE IF NOT EXISTS `traveler` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
    `phone` VARCHAR(20) COMMENT '手机号',
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出行人表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `line_id` BIGINT NOT NULL COMMENT '线路ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_line` (`user_id`, `line_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `line_id` BIGINT NOT NULL COMMENT '线路ID',
    `order_id` BIGINT COMMENT '订单ID',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `content` TEXT COMMENT '评价内容',
    `images` TEXT COMMENT '图片JSON',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_line_id` (`line_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 结伴游表
CREATE TABLE IF NOT EXISTS `companion` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发起人ID',
    `line_id` BIGINT COMMENT '关联线路ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '详情描述',
    `departure_date` DATE COMMENT '预计出发日期',
    `member_count` INT DEFAULT 1 COMMENT '期望人数',
    `current_count` INT DEFAULT 1 COMMENT '当前人数',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-招募中 1-已成行 2-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结伴游表';

-- 结伴游成员表
CREATE TABLE IF NOT EXISTS `companion_member` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `companion_id` BIGINT NOT NULL COMMENT '结伴游ID',
    `user_id` BIGINT NOT NULL COMMENT '成员ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待确认 1-已加入 2-已退出',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_companion_user` (`companion_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结伴游成员表';

-- 旅游攻略表
CREATE TABLE IF NOT EXISTS `guide` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '正文',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游攻略表';

-- 景点表（用于用户查询景点）
CREATE TABLE IF NOT EXISTS `scenic_spot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '景点名称',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `district` VARCHAR(50) COMMENT '区/县',
    `address` VARCHAR(255) COMMENT '详细地址',
    `latitude` DECIMAL(10,6) COMMENT '纬度',
    `longitude` DECIMAL(10,6) COMMENT '经度',
    `tag` VARCHAR(100) COMMENT '标签',
    `type` VARCHAR(50) COMMENT '类型',
    `search_city` VARCHAR(50) COMMENT '搜索城市',
    `cover_image` VARCHAR(500) COMMENT '景点照片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_city` (`city`),
    INDEX `idx_search_city` (`search_city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

-- 用户景点路线表（用户将景点加入自己的旅游路线）
CREATE TABLE IF NOT EXISTS `user_route_spot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `scenic_spot_id` BIGINT NOT NULL COMMENT '景点ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_spot` (`user_id`, `scenic_spot_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户景点路线表';


-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1-满减',
    `discount_amount` DECIMAL(10,2) NOT NULL COMMENT '减免金额',
    `threshold_amount` DECIMAL(10,2) NOT NULL COMMENT '门槛金额',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `quantity` INT NOT NULL COMMENT '总量',
    `issued_count` INT DEFAULT 0 COMMENT '已领数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券领取表
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已过期',
    `order_id` BIGINT COMMENT '核销订单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `use_time` DATETIME COMMENT '使用时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券领取表';

-- 营销活动表
CREATE TABLE IF NOT EXISTS `activity` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description` TEXT COMMENT '活动描述',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';
