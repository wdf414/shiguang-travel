package com.travel.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.demo-data-enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database schema...");

        // 1. Check and add columns to tour_line
        try {
            jdbcTemplate.execute("SELECT category FROM tour_line LIMIT 1");
        } catch (Exception e) {
            log.info("Adding missing columns to tour_line table...");
            try {
                jdbcTemplate.execute("ALTER TABLE tour_line ADD COLUMN category VARCHAR(50) COMMENT '分类'");
                jdbcTemplate.execute("ALTER TABLE tour_line ADD COLUMN tags VARCHAR(200) COMMENT '标签'");
            } catch (Exception ex) {
                log.warn("Failed to alter tour_line table: {}", ex.getMessage());
            }
        }

        // 2. Create new business tables if not exists
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `companion` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL," +
                    "`line_id` BIGINT," +
                    "`title` VARCHAR(200) NOT NULL," +
                    "`content` TEXT NOT NULL," +
                    "`departure_date` DATE," +
                    "`member_count` INT DEFAULT 1," +
                    "`current_count` INT DEFAULT 1," +
                    "`status` TINYINT DEFAULT 0," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "`deleted` TINYINT DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `companion_member` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`companion_id` BIGINT NOT NULL," +
                    "`user_id` BIGINT NOT NULL," +
                    "`status` TINYINT DEFAULT 0," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY `uk_companion_user` (`companion_id`, `user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `guide` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL," +
                    "`title` VARCHAR(200) NOT NULL," +
                    "`content` LONGTEXT NOT NULL," +
                    "`cover_image` LONGTEXT," +
                    "`view_count` INT DEFAULT 0," +
                    "`like_count` INT DEFAULT 0," +
                    "`status` TINYINT DEFAULT 1," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "`deleted` TINYINT DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `scenic_spot` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`name` VARCHAR(200) NOT NULL COMMENT '景点名称'," +
                    "`province` VARCHAR(50) COMMENT '省份'," +
                    "`city` VARCHAR(50) COMMENT '城市'," +
                    "`district` VARCHAR(50) COMMENT '区/县'," +
                    "`address` VARCHAR(255) COMMENT '详细地址'," +
                    "`latitude` DECIMAL(10,6) COMMENT '纬度'," +
                    "`longitude` DECIMAL(10,6) COMMENT '经度'," +
                    "`tag` VARCHAR(100) COMMENT '标签'," +
                    "`type` VARCHAR(50) COMMENT '类型'," +
                    "`search_city` VARCHAR(50) COMMENT '搜索城市'," +
                    "`cover_image` VARCHAR(500) COMMENT '景点照片'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "INDEX `idx_city` (`city`)," +
                    "INDEX `idx_search_city` (`search_city`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user_route_spot` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL COMMENT '用户ID'," +
                    "`scenic_spot_id` BIGINT NOT NULL COMMENT '景点ID'," +
                    "`sort_order` INT DEFAULT 0 COMMENT '排序'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY `uk_user_spot` (`user_id`, `scenic_spot_id`)," +
                    "INDEX `idx_user_id` (`user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `travel_note` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL COMMENT '用户ID'," +
                    "`title` VARCHAR(255) NOT NULL COMMENT '标题'," +
                    "`content` LONGTEXT NOT NULL COMMENT '内容'," +
                    "`cover_image` LONGTEXT COMMENT '封面图'," +
                    "`destination` VARCHAR(100) COMMENT '目的地'," +
                    "`days` INT COMMENT '天数'," +
                    "`cost` DECIMAL(10,2) COMMENT '花费'," +
                    "`travel_date` DATETIME COMMENT '出发日期'," +
                    "`views` INT DEFAULT 0 COMMENT '浏览量'," +
                    "`likes` INT DEFAULT 0 COMMENT '点赞量'," +
                    "`favorites` INT DEFAULT 0 COMMENT '收藏量'," +
                    "`comments` INT DEFAULT 0 COMMENT '评论量'," +
                    "`status` TINYINT DEFAULT 1 COMMENT '状态：0-暂存 1-已发布'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "`deleted` TINYINT DEFAULT 0," +
                    "INDEX `idx_user_id` (`user_id`)," +
                    "INDEX `idx_status` (`status`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `travel_note_comment` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`note_id` BIGINT NOT NULL COMMENT '游记ID'," +
                    "`user_id` BIGINT NOT NULL COMMENT '用户ID'," +
                    "`content` TEXT NOT NULL COMMENT '内容'," +
                    "`reply_to_uid` BIGINT COMMENT '回复用户ID'," +
                    "`likes` INT DEFAULT 0 COMMENT '点赞量'," +
                    "`status` TINYINT DEFAULT 1 COMMENT '状态：0-屏蔽 1-正常'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`deleted` TINYINT DEFAULT 0," +
                    "INDEX `idx_note_id` (`note_id`)," +
                    "INDEX `idx_user_id` (`user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            log.info("Companion, guide, scenic_spot, user_route_spot, travel_note and travel_note_comment tables checked/created.");
        } catch (Exception e) {
            log.warn("Failed to create new tables: {}", e.getMessage());
        }

        // 2.3 Ensure travel_note columns support large content and images
        try {
            jdbcTemplate.execute("ALTER TABLE `travel_note` MODIFY COLUMN `content` LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE `travel_note` MODIFY COLUMN `cover_image` LONGTEXT");
            
            // Also ensure guide table supports large content and images
            jdbcTemplate.execute("ALTER TABLE `guide` MODIFY COLUMN `content` LONGTEXT");
            jdbcTemplate.execute("ALTER TABLE `guide` MODIFY COLUMN `cover_image` LONGTEXT");
            
            // Add missing columns if they don't exist
            try { jdbcTemplate.execute("ALTER TABLE `travel_note` ADD COLUMN `days` INT COMMENT '天数'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE `travel_note` ADD COLUMN `cost` DECIMAL(10,2) COMMENT '花费'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE `travel_note` ADD COLUMN `travel_date` DATETIME COMMENT '出发日期'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE `travel_note` ADD COLUMN `favorites` INT DEFAULT 0 COMMENT '收藏量'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE `travel_note` ADD COLUMN `comments` INT DEFAULT 0 COMMENT '评论量'"); } catch (Exception ignored) {}
            
            log.info("Travel note table columns ensured.");
        } catch (Exception e) {
            log.warn("Failed to ensure travel_note columns: {}", e.getMessage());
        }

        // 2.2 Ensure scenic_spot has cover_image column for frontend images
        try {
            Integer colExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.columns " +
                            "WHERE table_schema = DATABASE() AND table_name = 'scenic_spot' AND column_name = 'cover_image'",
                    Integer.class
            );
            if (colExists == null || colExists == 0) {
                jdbcTemplate.execute("ALTER TABLE `scenic_spot` ADD COLUMN `cover_image` VARCHAR(500) COMMENT '景点照片'");
                log.info("Added cover_image column to scenic_spot table.");
            }
        } catch (Exception e) {
            log.warn("Failed to ensure scenic_spot.cover_image column: {}", e.getMessage());
        }

        // 2.1 Ensure user.avatar column can store base64 (expand to TEXT if currently VARCHAR)
        try {
            String dataType = jdbcTemplate.queryForObject(
                    "SELECT DATA_TYPE FROM information_schema.columns " +
                            "WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'avatar'",
                    String.class
            );
            if ("varchar".equalsIgnoreCase(dataType)) {
                jdbcTemplate.execute("ALTER TABLE `user` MODIFY `avatar` TEXT COMMENT '头像'");
                log.info("User.avatar column upgraded to TEXT for base64 support.");
            }
        } catch (Exception e) {
            log.warn("Failed to ensure user.avatar column type: {}", e.getMessage());
        }

        // 3. Migrate any legacy plaintext password to BCrypt, then clear plaintext copies.
        try {
            List<Map<String, Object>> legacyUsers = jdbcTemplate.queryForList(
                    "SELECT id, password FROM `user` "
                            + "WHERE password IS NOT NULL AND password <> '' "
                            + "AND password NOT LIKE '$2a$%' AND password NOT LIKE '$2b$%' AND password NOT LIKE '$2y$%'");
            for (Map<String, Object> row : legacyUsers) {
                String rawPassword = String.valueOf(row.get("password"));
                jdbcTemplate.update("UPDATE `user` SET `password` = ? WHERE id = ?",
                        passwordEncoder.encode(rawPassword), row.get("id"));
            }
            // The column is kept for compatibility with old dumps, but it must never store secrets.
            try {
                jdbcTemplate.update("UPDATE `user` SET `plain_password` = NULL");
            } catch (Exception ignored) {
                // Older schema may not have this column.
            }
            log.info("Migrated {} legacy password(s) to BCrypt and cleared plaintext copies.", legacyUsers.size());
        } catch (Exception e) {
            log.warn("Failed to migrate passwords: {}", e.getMessage());
        }

        try {
            Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user'",
                    Integer.class
            );
            if (exists != null && exists > 0) {
                jdbcTemplate.update(
                        "INSERT INTO `user` (`phone`, `password`, `nickname`, `role`, `status`) VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`), `role` = VALUES(`role`), `status` = VALUES(`status`)",
                        "13800138001", passwordEncoder.encode("123456"), "旅行者小明", "USER", 1
                );
                jdbcTemplate.update(
                        "INSERT INTO `user` (`phone`, `password`, `nickname`, `role`, `status`) VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`), `role` = VALUES(`role`), `status` = VALUES(`status`)",
                        "13800138002", passwordEncoder.encode("123456"), "背包客小红", "USER", 1
                );
                jdbcTemplate.update(
                        "INSERT INTO `user` (`phone`, `password`, `nickname`, `role`, `status`) VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`), `role` = VALUES(`role`), `status` = VALUES(`status`)",
                        "13800138003", passwordEncoder.encode("123456"), "探险家小李", "USER", 1
                );
                jdbcTemplate.update(
                        "INSERT INTO `user` (`phone`, `password`, `nickname`, `role`, `status`) VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`), `role` = VALUES(`role`), `status` = VALUES(`status`)",
                        "13800138000", passwordEncoder.encode("123456"), "管理员", "ADMIN", 1
                );
                log.info("Default users checked/seeded.");
            }
        } catch (Exception e) {
            log.warn("Failed to seed default users: {}", e.getMessage());
        }

        // 4. Update tour_line tags for filtering support
        try {
            log.info("Updating tour_line tags for filtering support...");
            // 为没有有效标签的线路添加默认标签，确保可以通过前端筛选
            jdbcTemplate.update("UPDATE tour_line SET tags = '乐园,海岛,美食', category = '学生特惠' WHERE title LIKE '%珠海%' AND (tags IS NULL OR tags NOT REGEXP '古城|摄影|美食|海岛|自然|徒步|历史|研学')");
            jdbcTemplate.update("UPDATE tour_line SET tags = '自然,古城,摄影', category = '学生特惠' WHERE title LIKE '%杭州%' AND (tags IS NULL OR tags NOT REGEXP '古城|摄影|美食|海岛|自然|徒步|历史|研学')");
            jdbcTemplate.update("UPDATE tour_line SET tags = '徒步,自然,摄影', category = '学生特惠' WHERE title LIKE '%香格里拉%' AND (tags IS NULL OR tags NOT REGEXP '古城|摄影|美食|海岛|自然|徒步|历史|研学')");
            jdbcTemplate.update("UPDATE tour_line SET tags = '海岛,美食,自然', category = '学生特惠' WHERE title LIKE '%博鳌%' AND (tags IS NULL OR tags NOT REGEXP '古城|摄影|美食|海岛|自然|徒步|历史|研学')");
            
            // 补充更多数据到首页分类
            jdbcTemplate.update("UPDATE tour_line SET category = '学生特惠' WHERE id % 3 = 0 AND (category IS NULL OR category = '')");
            jdbcTemplate.update("UPDATE tour_line SET category = '周末游' WHERE id % 3 = 1 AND (category IS NULL OR category = '')");
            jdbcTemplate.update("UPDATE tour_line SET category = '海岛度假' WHERE id % 3 = 2 AND (category IS NULL OR category = '')");

            // 兜底：确保所有线路至少有一个玩法标签
            jdbcTemplate.update("UPDATE tour_line SET tags = CONCAT(IFNULL(tags, ''), ',自然') WHERE tags IS NULL OR tags = '' OR tags NOT REGEXP '古城|摄影|美食|海岛|自然|徒步|历史|研学'");
            log.info("Tour line tags updated.");
        } catch (Exception e) {
            log.warn("Failed to update tour_line tags: {}", e.getMessage());
        }

        // 5. Update review/user/hotel/guide/complaint schema for v4 features
        try {
            log.info("Checking v4 schema updates...");
            
            // 5.1 Add is_student to user table
            try {
                jdbcTemplate.execute("ALTER TABLE user ADD COLUMN is_student TINYINT DEFAULT 0 COMMENT '0-非学生 1-学生'");
            } catch (Exception ignored) {}

            // 5.2 Add hotel_id, guide_id, tags to review table
            try { jdbcTemplate.execute("ALTER TABLE review ADD COLUMN hotel_id BIGINT COMMENT '酒店ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review ADD COLUMN guide_id BIGINT COMMENT '导游ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review ADD COLUMN tags VARCHAR(255) COMMENT '评价标签'"); } catch (Exception ignored) {}

            // 5.3 Add review_count to hotel table
            try { jdbcTemplate.execute("ALTER TABLE hotel ADD COLUMN review_count INT DEFAULT 0 COMMENT '评论数'"); } catch (Exception ignored) {}

            // 5.4 Add rating, review_count to guide table
            try { jdbcTemplate.execute("ALTER TABLE guide ADD COLUMN rating DECIMAL(2,1) DEFAULT 0 COMMENT '评分'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE guide ADD COLUMN review_count INT DEFAULT 0 COMMENT '评论数'"); } catch (Exception ignored) {}

            // 5.5 Create complaint table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS complaint (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                    "type TINYINT DEFAULT 1 COMMENT '1-建议 2-投诉'," +
                    "content TEXT NOT NULL COMMENT '内容'," +
                    "contact VARCHAR(100) COMMENT '联系方式'," +
                    "reply TEXT COMMENT '回复内容'," +
                    "status TINYINT DEFAULT 0 COMMENT '0-待处理 1-已回复'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "reply_time DATETIME COMMENT '回复时间'," +
                    "deleted INT DEFAULT 0 COMMENT '逻辑删除'," +
                    "INDEX idx_user_id (user_id)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉建议表'");

            log.info("v4 schema updates checked.");
        } catch (Exception e) {
            log.warn("Failed to update v4 schema: {}", e.getMessage());
        }

        // 6. Update schema for v5 (Campus Companion features)
        try {
            log.info("Checking v5 schema updates (Campus Companion)...");

            // 6.1 Add school/major/grade to user table
            try { jdbcTemplate.execute("ALTER TABLE user ADD COLUMN school VARCHAR(100) COMMENT '学校名称'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE user ADD COLUMN major VARCHAR(100) COMMENT '专业'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE user ADD COLUMN grade VARCHAR(20) COMMENT '年级'"); } catch (Exception ignored) {}

            // 6.2 Add school/group_qrcode to companion table
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN school VARCHAR(100) COMMENT '限定学校'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN group_qrcode VARCHAR(500) COMMENT '群聊二维码'"); } catch (Exception ignored) {}

            // 6.3 Create campus_leader table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS campus_leader (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                    "school VARCHAR(100) COMMENT '学校'," +
                    "major VARCHAR(100) COMMENT '专业'," +
                    "grade VARCHAR(20) COMMENT '年级'," +
                    "description TEXT COMMENT '个人简介/优势'," +
                    "status TINYINT DEFAULT 0 COMMENT '0-待审核 1-已认证 2-已拒绝'," +
                    "commission_rate DECIMAL(5,2) DEFAULT 5.00 COMMENT '佣金比例(%)'," +
                    "total_commission DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计佣金'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "deleted TINYINT DEFAULT 0," +
                    "UNIQUE KEY uk_user_id (user_id)," +
                    "INDEX idx_school (school)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园领队表'");

            // 6.4 Create leader_commission table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS leader_commission (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "leader_id BIGINT NOT NULL COMMENT '领队ID'," +
                    "companion_id BIGINT NOT NULL COMMENT '结伴ID'," +
                    "order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额'," +
                    "commission_rate DECIMAL(5,2) NOT NULL COMMENT '佣金比例'," +
                    "commission_amount DECIMAL(10,2) NOT NULL COMMENT '佣金金额'," +
                    "status TINYINT DEFAULT 0 COMMENT '0-待结算 1-已结算'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_leader_id (leader_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领队佣金记录表'");

            log.info("v5 schema updates checked.");
        } catch (Exception e) {
            log.warn("Failed to update v5 schema: {}", e.getMessage());
        }

        // 7. Update schema for v7 (Student Authentication and Benefits)
        try {
            log.info("Checking v7 schema updates (Student Authentication)...");

            // 7.1 Add student_auth_status to user table
            try { jdbcTemplate.execute("ALTER TABLE user ADD COLUMN student_auth_status TINYINT DEFAULT 0 COMMENT '学生认证状态：0-未认证 1-待审核 2-已认证 3-已拒绝'"); } catch (Exception ignored) {}

            // 7.2 Add student-related columns to tour_line
            try { jdbcTemplate.execute("ALTER TABLE tour_line ADD COLUMN student_discount DECIMAL(5,2) DEFAULT 0 COMMENT '学生折扣比例(%)'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE tour_line ADD COLUMN student_price DECIMAL(10,2) COMMENT '学生价'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE tour_line ADD COLUMN student_friendly TINYINT DEFAULT 0 COMMENT '是否学生友好：0-否 1-是'"); } catch (Exception ignored) {}

            // 7.3 Add student-related columns to hotel
            try { jdbcTemplate.execute("ALTER TABLE hotel ADD COLUMN student_discount DECIMAL(5,2) DEFAULT 0 COMMENT '学生折扣比例(%)'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE hotel ADD COLUMN student_price DECIMAL(10,2) COMMENT '学生价'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE hotel ADD COLUMN student_friendly TINYINT DEFAULT 0 COMMENT '是否学生友好：0-否 1-是'"); } catch (Exception ignored) {}

            // 7.4 Add student_only to activity
            try { jdbcTemplate.execute("ALTER TABLE activity ADD COLUMN student_only TINYINT DEFAULT 0 COMMENT '是否仅学生可见：0-否 1-是'"); } catch (Exception ignored) {}

            // 7.5 Add student_discount to orders
            try { jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN student_discount DECIMAL(10,2) DEFAULT 0 COMMENT '学生优惠金额'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN final_amount DECIMAL(10,2) COMMENT '最终支付金额'"); } catch (Exception ignored) {}

            // 7.6 Add student_discount to hotel_order
            try { jdbcTemplate.execute("ALTER TABLE hotel_order ADD COLUMN student_discount DECIMAL(10,2) DEFAULT 0 COMMENT '学生优惠金额'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE hotel_order ADD COLUMN final_amount DECIMAL(10,2) COMMENT '最终支付金额'"); } catch (Exception ignored) {}

            // 7.7 Create student_auth_application table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS student_auth_application (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                    "user_name VARCHAR(100) COMMENT '用户姓名'," +
                    "user_phone VARCHAR(20) COMMENT '用户手机号'," +
                    "real_name VARCHAR(100) COMMENT '真实姓名'," +
                    "school_name VARCHAR(100) COMMENT '学校名称'," +
                    "major VARCHAR(100) COMMENT '专业'," +
                    "grade VARCHAR(20) COMMENT '年级'," +
                    "student_id VARCHAR(50) COMMENT '学号'," +
                    "id_card_front VARCHAR(500) COMMENT '身份证正面照片URL'," +
                    "id_card_back VARCHAR(500) COMMENT '身份证反面照片URL'," +
                    "student_card VARCHAR(500) COMMENT '学生证照片URL'," +
                    "chsi_screenshot VARCHAR(500) COMMENT '学信网截图URL'," +
                    "status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝'," +
                    "reject_reason VARCHAR(500) COMMENT '拒绝原因'," +
                    "reviewer_id BIGINT COMMENT '审核人ID'," +
                    "reviewer_name VARCHAR(100) COMMENT '审核人姓名'," +
                    "review_time DATETIME COMMENT '审核时间'," +
                    "valid_start_date DATE COMMENT '有效期开始'," +
                    "valid_end_date DATE COMMENT '有效期结束'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "deleted TINYINT DEFAULT 0," +
                    "INDEX idx_user_id (user_id)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生认证申请表'");
            
            // 7.7.1 Alter existing table to add missing columns (if table already exists with old schema)
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN real_name VARCHAR(100) COMMENT '真实姓名'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application CHANGE school school_name VARCHAR(100) COMMENT '学校名称'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN student_id VARCHAR(50) COMMENT '学号'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN id_card_front VARCHAR(500) COMMENT '身份证正面照片URL'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN id_card_back VARCHAR(500) COMMENT '身份证反面照片URL'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application CHANGE student_id_card student_card VARCHAR(500) COMMENT '学生证照片URL'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN chsi_screenshot VARCHAR(500) COMMENT '学信网截图URL'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application DROP COLUMN chsi_account"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application DROP COLUMN chsi_password"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN valid_start_date DATE COMMENT '有效期开始'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE student_auth_application ADD COLUMN valid_end_date DATE COMMENT '有效期结束'"); } catch (Exception ignored) {}

            // 7.8 Create student_benefit_config table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS student_benefit_config (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "benefit_type VARCHAR(50) NOT NULL COMMENT '权益类型：discount-折扣 label-标签 activity-活动'," +
                    "benefit_name VARCHAR(100) NOT NULL COMMENT '权益名称'," +
                    "benefit_value VARCHAR(200) COMMENT '权益值（如折扣比例、标签名称等）'," +
                    "description TEXT COMMENT '权益描述'," +
                    "status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用'," +
                    "sort_order INT DEFAULT 0 COMMENT '排序'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "deleted TINYINT DEFAULT 0," +
                    "INDEX idx_type (benefit_type)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生权益配置表'");

            log.info("v7 schema updates checked.");
        } catch (Exception e) {
            log.warn("Failed to update v7 schema: {}", e.getMessage());
        }

        // 9. Add login_log table for admin monitoring
        try {
            log.info("Checking login_log table...");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `login_log` (" +
                    "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "`user_id` BIGINT NOT NULL COMMENT '用户ID'," +
                    "`login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间'," +
                    "`ip` VARCHAR(50) COMMENT 'IP地址'," +
                    "`user_agent` VARCHAR(500) COMMENT '用户代理'," +
                    "`status` TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "INDEX `idx_user_id` (`user_id`)," +
                    "INDEX `idx_time` (`login_time`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表'");
            log.info("login_log table checked/created.");
        } catch (Exception e) {
            log.warn("Failed to create login_log table: {}", e.getMessage());
        }

        // 8. Update schema for content review (add review_status fields)
        try {
            log.info("Checking content review schema updates...");

            // 8.1 Add review_status to travel_note (0-待审核 1-已通过 2-已拒绝)
            try { jdbcTemplate.execute("ALTER TABLE travel_note ADD COLUMN review_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note ADD COLUMN review_time DATETIME COMMENT '审核时间'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note ADD COLUMN reviewer_id BIGINT COMMENT '审核人ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note ADD COLUMN reject_reason VARCHAR(500) COMMENT '拒绝原因'"); } catch (Exception ignored) {}

            // 8.2 Add review_status to companion
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN review_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN review_time DATETIME COMMENT '审核时间'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN reviewer_id BIGINT COMMENT '审核人ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE companion ADD COLUMN reject_reason VARCHAR(500) COMMENT '拒绝原因'"); } catch (Exception ignored) {}

            // 8.3 Add review_status to travel_note_comment
            try { jdbcTemplate.execute("ALTER TABLE travel_note_comment ADD COLUMN review_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note_comment ADD COLUMN review_time DATETIME COMMENT '审核时间'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note_comment ADD COLUMN reviewer_id BIGINT COMMENT '审核人ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE travel_note_comment ADD COLUMN reject_reason VARCHAR(500) COMMENT '拒绝原因'"); } catch (Exception ignored) {}

            // 8.4 Create review_record table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS review_record (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "content_type VARCHAR(50) NOT NULL COMMENT '内容类型'," +
                    "content_id BIGINT NOT NULL COMMENT '内容ID'," +
                    "content_title VARCHAR(255) COMMENT '内容标题'," +
                    "content_author_id BIGINT COMMENT '内容作者ID'," +
                    "old_status TINYINT COMMENT '审核前状态'," +
                    "new_status TINYINT NOT NULL COMMENT '审核后状态'," +
                    "review_type TINYINT DEFAULT 1 COMMENT '审核类型'," +
                    "reviewer_id BIGINT COMMENT '审核人ID'," +
                    "reviewer_name VARCHAR(100) COMMENT '审核人姓名'," +
                    "review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间'," +
                    "reject_reason VARCHAR(500) COMMENT '驳回原因'," +
                    "auto_review_result VARCHAR(50) COMMENT '自动审核结果'," +
                    "sensitive_words VARCHAR(500) COMMENT '检测到的敏感词'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "INDEX idx_content (content_type, content_id)," +
                    "INDEX idx_reviewer (reviewer_id)," +
                    "INDEX idx_status (new_status)," +
                    "INDEX idx_time (review_time)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容审核记录表'");

            // 8.4.1 Fix potential column mismatches
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN content_title VARCHAR(255) COMMENT '内容标题' AFTER content_id"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record CHANGE author_id content_author_id BIGINT COMMENT '内容作者ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN content_author_id BIGINT COMMENT '内容作者ID' AFTER content_title"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN old_status TINYINT COMMENT '审核前状态' AFTER content_author_id"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN new_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核后状态' AFTER old_status"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN review_type TINYINT DEFAULT 1 COMMENT '审核类型'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN reviewer_id BIGINT COMMENT '审核人ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN reviewer_name VARCHAR(100) COMMENT '审核人姓名'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN review_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN reject_reason VARCHAR(500) COMMENT '驳回原因'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN auto_review_result VARCHAR(50) COMMENT '自动审核结果'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN sensitive_words VARCHAR(500) COMMENT '检测到的敏感词'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE review_record ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"); } catch (Exception ignored) {}

            // 8.5 Create sensitive_word table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sensitive_word (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "word VARCHAR(100) NOT NULL COMMENT '敏感词'," +
                    "word_type TINYINT DEFAULT 1 COMMENT '类型：1-违禁词 2-敏感词 3-广告词'," +
                    "category VARCHAR(50) COMMENT '分类'," +
                    "match_mode TINYINT DEFAULT 1 COMMENT '匹配模式'," +
                    "replace_word VARCHAR(100) COMMENT '替换词'," +
                    "is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uk_word (word)," +
                    "INDEX idx_type (word_type)," +
                    "INDEX idx_is_active (is_active)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库表'");

            // 8.5.1 Fix potential column name mismatch (status -> is_active)
            try { jdbcTemplate.execute("ALTER TABLE sensitive_word CHANGE status is_active TINYINT DEFAULT 1 COMMENT '是否启用'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE sensitive_word ADD COLUMN category VARCHAR(50) COMMENT '分类'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE sensitive_word ADD COLUMN match_mode TINYINT DEFAULT 1 COMMENT '匹配模式'"); } catch (Exception ignored) {}

            // 8.6 Reset some content to pending status for testing (Optional)
            try {
                jdbcTemplate.execute("UPDATE travel_note SET review_status = 0 WHERE review_status IS NULL OR review_status = 1 LIMIT 5");
                jdbcTemplate.execute("UPDATE companion SET review_status = 0 WHERE review_status IS NULL OR review_status = 1 LIMIT 5");
                jdbcTemplate.execute("UPDATE travel_note_comment SET review_status = 0 WHERE review_status IS NULL OR review_status = 1 LIMIT 5");
            } catch (Exception ignored) {}

            log.info("Content review schema updates checked.");
        } catch (Exception e) {
            log.warn("Failed to update content review schema: {}", e.getMessage());
        }

        log.info("Database schema check completed.");
    }
}
