# 数据库说明

本目录只保留一份**已脱敏的演示数据库**：`lvyou_full.sql`。

## 包含内容

| 数据 | 数量 | 说明 |
|---|---:|---|
| 景点 `scenic_spot` | 1600 | 公开景点基础数据 |
| 景点照片索引 `scenic_photos` | 1583 | 与仓库根目录 `scenic_photos/` 文件名对应 |
| 酒店评分 `hote_grade` | 325 | 酒店兜底数据源 |
| 旅游线路 `tour_line` | 15 | 演示线路 |
| 用户 `user` | 8 | 演示账号，密码为 BCrypt 哈希 |
| 游记 `travel_note` | 4 | 演示内容 |
| 酒店订单 / 线路订单 | 2 / 1 | 联系方式已替换为演示数据 |

## 已做的脱敏处理

- 删除早期测试账号与明文密码列 `plain_password`；
- 清空用户头像、邮箱、学生证/身份证等个人信息；
- 清空 `traveler` 表中的姓名、身份证号、手机号；
- 清空投诉、登录日志、浏览记录、钱包等个人/交易记录；
- 将酒店/线路订单联系人和导游联系方式替换为演示数据；
- 所有用户密码均为 BCrypt 哈希，演示账号密码统一为 `123456`。

> 如果你需要完整原始数据，请使用本地留存的原始压缩包，不要把它提交到公开仓库。

## 导入方式

确保 MySQL 8.x 已启动，然后执行：

```powershell
mysql -u root -p --default-character-set=utf8mb4 < database/lvyou_full.sql
```

或使用 Navicat / SQLyog / MySQL Workbench 运行该 SQL 文件。文件内已包含
`CREATE DATABASE lvyou`，无需手动建库。

导入后在 `travel-backend/.env` 中配置：

```env
DB_URL=jdbc:mysql://localhost:3306/lvyou?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=你的密码
```

## 演示账号

| 角色 | 手机号 | 密码 |
|---|---|---|
| 管理员 | `13800138000` | `123456` |
| 普通用户 | `13800138001` | `123456` |
| 普通用户 | `13800138002` | `123456` |

首次启动时 `DatabaseInitializer` 会补齐表结构并确保这些演示账号存在。
生产环境可设置 `DEMO_DATA_ENABLED=false` 关闭该行为。
