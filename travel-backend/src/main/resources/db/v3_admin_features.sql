-- =====================================================
-- 管理后台扩展功能 - 数据库表结构
-- =====================================================

-- 1. 内容管理模块
-- =====================================================

-- 攻略/游记表
CREATE TABLE IF NOT EXISTS travel_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images JSON COMMENT '图片列表',
    destination VARCHAR(100) COMMENT '目的地',
    days INT COMMENT '行程天数',
    tags VARCHAR(255) COMMENT '标签',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游攻略表';

-- 攻略评论表
CREATE TABLE IF NOT EXISTS travel_note_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '攻略ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID（回复）',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='攻略评论表';

-- 问答表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '提问用户ID',
    title VARCHAR(255) NOT NULL COMMENT '问题标题',
    content TEXT NOT NULL COMMENT '问题内容',
    category VARCHAR(50) COMMENT '分类',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待回答 1-已回答 2-已关闭',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    answer_count INT DEFAULT 0 COMMENT '回答数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答表';

-- 问答回答表
CREATE TABLE IF NOT EXISTS question_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL COMMENT '问题ID',
    user_id BIGINT NOT NULL COMMENT '回答用户ID',
    content TEXT NOT NULL COMMENT '回答内容',
    is_best TINYINT DEFAULT 0 COMMENT '是否最佳答案：0-否 1-是',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_question_id (question_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答回答表';

-- 标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    type VARCHAR(20) NOT NULL COMMENT '标签类型：line-线路 hotel-酒店 note-攻略',
    color VARCHAR(20) COMMENT '标签颜色',
    sort_order INT DEFAULT 0 COMMENT '排序',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 内容与标签关联表
CREATE TABLE IF NOT EXISTS content_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_type VARCHAR(20) NOT NULL COMMENT '内容类型：line hotel note',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_content_tag (content_type, content_id, tag_id),
    INDEX idx_content (content_type, content_id),
    INDEX idx_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

-- =====================================================
-- 2. 营销推广模块
-- =====================================================

-- 优惠券表
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT NOT NULL COMMENT '类型：1-满减 2-折扣 3-立减',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '优惠值',
    min_amount DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    max_discount DECIMAL(10,2) COMMENT '最大优惠金额',
    total_count INT DEFAULT 0 COMMENT '发放总量',
    used_count INT DEFAULT 0 COMMENT '已使用数量',
    per_user_limit INT DEFAULT 1 COMMENT '每人限领数量',
    valid_days INT COMMENT '有效天数',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    description VARCHAR(500) COMMENT '使用说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未使用 1-已使用 2-已过期',
    order_id BIGINT COMMENT '使用的订单ID',
    use_time DATETIME COMMENT '使用时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    UNIQUE KEY uk_user_coupon (user_id, coupon_id),
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '活动标题',
    type VARCHAR(50) NOT NULL COMMENT '活动类型：discount-促销 flash-限时秒杀',
    description TEXT COMMENT '活动描述',
    image VARCHAR(500) COMMENT '活动图片',
    discount_value DECIMAL(10,2) COMMENT '优惠值',
    min_amount DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    max_discount DECIMAL(10,2) COMMENT '最大优惠金额',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    participant_count INT DEFAULT 0 COMMENT '参与人数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- =====================================================
-- 3. 首页推荐管理模块
-- =====================================================

-- 轮播图表
CREATE TABLE IF NOT EXISTS banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    image VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 首页推荐表
CREATE TABLE IF NOT EXISTS home_recommend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recommend_type VARCHAR(20) NOT NULL COMMENT '推荐类型：line-线路 hotel-酒店',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    title VARCHAR(255) COMMENT '标题',
    image VARCHAR(500) COMMENT '图片URL',
    link_url VARCHAR(500) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_type (recommend_type),
    INDEX idx_status (status),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页推荐表';

-- =====================================================
-- 4. 数据统计与分析模块
-- =====================================================

-- 用户行为分析表
CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    behavior_type VARCHAR(50) NOT NULL COMMENT '行为类型：view-浏览 favorite-收藏 order-下单 search-搜索 login-登录',
    content_type VARCHAR(20) COMMENT '内容类型：line hotel note',
    content_id BIGINT COMMENT '内容ID',
    page_url VARCHAR(500) COMMENT '页面URL',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (behavior_type),
    INDEX idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为分析表';

-- =====================================================
-- 5. 库存与价格管理模块
-- =====================================================

-- 线路库存表
CREATE TABLE IF NOT EXISTS line_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    line_id BIGINT NOT NULL COMMENT '线路ID',
    stock INT DEFAULT 0 COMMENT '库存数量',
    sold_count INT DEFAULT 0 COMMENT '已售数量',
    warning_threshold INT DEFAULT 10 COMMENT '库存预警阈值',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_line_id (line_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线路库存表';

-- 酒店房型库存表
CREATE TABLE IF NOT EXISTS hotel_room_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL COMMENT '房型ID',
    stock INT DEFAULT 0 COMMENT '库存数量',
    sold_count INT DEFAULT 0 COMMENT '已售数量',
    warning_threshold INT DEFAULT 5 COMMENT '库存预警阈值',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_id (room_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店房型库存表';

-- 动态价格表
CREATE TABLE IF NOT EXISTS dynamic_price (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_type VARCHAR(20) NOT NULL COMMENT '内容类型：line hotel',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    price_type VARCHAR(20) NOT NULL COMMENT '价格类型：season-旺季 off-淡季 holiday-节假日',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_content (content_type, content_id),
    INDEX idx_time (start_time, end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态价格表';

-- =====================================================
-- 6. 财务管理模块
-- =====================================================

-- 交易记录表
CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    payment_method VARCHAR(50) COMMENT '支付方式：alipay-支付宝 wechat-微信 card-银行卡',
    transaction_no VARCHAR(100) COMMENT '交易流水号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-待支付 1-已支付 2-已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(500) COMMENT '退款原因',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 对账表
CREATE TABLE IF NOT EXISTS reconciliation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    order_id BIGINT COMMENT '订单ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    commission DECIMAL(10,2) DEFAULT 0 COMMENT '佣金',
    settlement_status TINYINT DEFAULT 0 COMMENT '结算状态：0-未结算 1-已结算',
    settlement_time DATETIME COMMENT '结算时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_platform (platform),
    INDEX idx_status (settlement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账表';

-- =====================================================
-- 7. 供应商管理模块
-- =====================================================

-- 供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(500) COMMENT '地址',
    business_license VARCHAR(100) COMMENT '营业执照',
    rating DECIMAL(3,2) DEFAULT 5.0 COMMENT '供应商评级',
    commission_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '佣金比例',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 供应商结算表
CREATE TABLE IF NOT EXISTS supplier_settlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    commission DECIMAL(10,2) NOT NULL COMMENT '佣金',
    settlement_status TINYINT DEFAULT 0 COMMENT '结算状态：0-未结算 1-已结算',
    settlement_time DATETIME COMMENT '结算时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (settlement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商结算表';

-- =====================================================
-- 8. 客服与售后模块
-- =====================================================

-- 工单表
CREATE TABLE IF NOT EXISTS work_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(255) NOT NULL COMMENT '工单标题',
    content TEXT NOT NULL COMMENT '工单内容',
    order_id BIGINT COMMENT '关联订单ID',
    type VARCHAR(50) COMMENT '工单类型：question-咨询 complaint-投诉 refund-退款',
    priority VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低 medium-中 high-高 urgent-紧急',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已完成 3-已关闭',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 投诉表
CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    content TEXT NOT NULL COMMENT '投诉内容',
    images JSON COMMENT '图片列表',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已完成 3-已关闭',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉表';

-- =====================================================
-- 9. 系统管理模块
-- =====================================================

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(100) COMMENT '路由路径',
    component VARCHAR(100) COMMENT '组件名称',
    icon VARCHAR(100) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否显示：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(20) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    error_msg VARCHAR(500) COMMENT '错误信息',
    execute_time INT COMMENT '执行时间（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    INDEX idx_user_id (user_id),
    INDEX idx_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string-字符串 number-数字 boolean-布尔',
    description VARCHAR(255) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    type VARCHAR(20) DEFAULT 'info' COMMENT '公告类型：info-信息 warning-警告 error-错误',
    priority VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低 medium-中 high-高',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已撤回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =====================================================
-- 10. 酒店管理模块
-- =====================================================

-- 酒店表（扩展现有hotel表）
-- ALTER TABLE hotel ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用';
-- ALTER TABLE hotel ADD COLUMN rating DECIMAL(3,2) DEFAULT 5.0 COMMENT '酒店评级';
-- ALTER TABLE hotel ADD COLUMN commission_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '佣金比例';

-- 酒店图片表
CREATE TABLE IF NOT EXISTS hotel_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL COMMENT '酒店ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店图片表';

-- 酒店订单表
CREATE TABLE IF NOT EXISTS hotel_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    hotel_id BIGINT NOT NULL COMMENT '酒店ID',
    room_id BIGINT COMMENT '房型ID',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '退房日期',
    room_count INT DEFAULT 1 COMMENT '房间数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已完成 3-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_hotel_id (hotel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店订单表';

-- =====================================================
-- 11. 结伴游管理模块
-- =====================================================

-- 结伴游表
CREATE TABLE IF NOT EXISTS companion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    destination VARCHAR(100) COMMENT '目的地',
    start_date DATE COMMENT '出发日期',
    end_date DATE COMMENT '结束日期',
    max_members INT COMMENT '最大人数',
    current_members INT DEFAULT 1 COMMENT '当前人数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝 3-已满员 4-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结伴游表';

-- 结伴游成员表
CREATE TABLE IF NOT EXISTS companion_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    companion_id BIGINT NOT NULL COMMENT '结伴游ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    INDEX idx_companion_id (companion_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结伴游成员表';

-- =====================================================
-- 12. 移动端管理模块
-- =====================================================

-- APP推送表
CREATE TABLE IF NOT EXISTS app_push (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT '推送标题',
    content TEXT COMMENT '推送内容',
    push_type VARCHAR(20) COMMENT '推送类型：all-全部 user-指定用户',
    target_users JSON COMMENT '目标用户列表',
    send_time DATETIME COMMENT '发送时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待发送 1-已发送 2-发送失败',
    sent_count INT DEFAULT 0 COMMENT '已发送数量',
    read_count INT DEFAULT 0 COMMENT '已读数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP推送表';

-- APP版本表
CREATE TABLE IF NOT EXISTS app_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(50) NOT NULL COMMENT '版本号',
    version_code INT NOT NULL COMMENT '版本代码',
    update_url VARCHAR(500) COMMENT '更新地址',
    update_content TEXT COMMENT '更新内容',
    force_update TINYINT DEFAULT 0 COMMENT '是否强制更新：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP版本表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化角色数据
INSERT INTO role (name, code, description, sort_order) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1),
('运营管理员', 'ADMIN', '负责日常运营管理', 2),
('客服人员', 'SERVICE', '负责客户服务', 3),
('财务人员', 'FINANCE', '负责财务管理', 4);

-- 初始化菜单数据
INSERT INTO menu (parent_id, name, path, component, icon, sort_order) VALUES
(0, '数据看板', '/admin/dashboard', 'Dashboard', 'Dashboard', 1),
(0, '内容管理', '/admin/content', 'Content', 'Content', 2),
(0, '营销推广', '/admin/marketing', 'Marketing', 'Marketing', 3),
(0, '首页推荐', '/admin/home', 'Home', 'Home', 4),
(0, '库存管理', '/admin/inventory', 'Inventory', 'Inventory', 5),
(0, '财务管理', '/admin/finance', 'Finance', 'Finance', 6),
(0, '供应商管理', '/admin/supplier', 'Supplier', 'Supplier', 7),
(0, '客服售后', '/admin/service', 'Service', 'Service', 8),
(0, '系统管理', '/admin/system', 'System', 'System', 9),
(0, '酒店管理', '/admin/hotel', 'Hotel', 'Hotel', 10),
(0, '结伴管理', '/admin/companion', 'Companion', 'Companion', 11),
(0, '移动端', '/admin/mobile', 'Mobile', 'Mobile', 12);

-- 初始化系统配置
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('site_name', '畅游天下', 'string', '网站名称'),
('site_logo', '', 'string', '网站Logo'),
('site_keywords', '旅游,线路,酒店,攻略', 'string', '网站关键词'),
('site_description', '专为学生群体打造的旅游电商平台', 'string', '网站描述'),
('contact_phone', '', 'string', '联系电话'),
('contact_email', '', 'string', '联系邮箱'),
('icp_number', '', 'string', 'ICP备案号'),
('enable_register', 'true', 'boolean', '是否开放注册'),
('enable_comment', 'true', 'boolean', '是否开放评论'),
('enable_question', 'true', 'boolean', '是否开放问答');

-- 初始化标签数据
INSERT INTO tag (name, type, color, sort_order) VALUES
('热门', 'line', '#ff6b6b', 1),
('亲子游', 'line', '#52c41a', 2),
('摄影', 'line', '#1890ff', 3),
('美食', 'line', '#f5222d', 4),
('海岛', 'line', '#13ce66', 5),
('古城', 'line', '#faad14', 6),
('徒步', 'line', '#00c853', 7),
('历史', 'line', '#ff9800', 8),
('自然', 'line', '#52c41a', 9),
('研学', 'line', '#722ed1', 10);
