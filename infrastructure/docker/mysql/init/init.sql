-- 创建Nacos数据库
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建无人机配送平台数据库
CREATE DATABASE IF NOT EXISTS drone_delivery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用无人机配送平台数据库
USE drone_delivery;

-- 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型：1-普通用户，2-飞手，3-管理员，4-商家',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 订单表
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pilot_id` bigint DEFAULT NULL COMMENT '飞手ID',
  `pickup_address` varchar(255) NOT NULL COMMENT '取货地址',
  `pickup_longitude` decimal(10,7) NOT NULL COMMENT '取货经度',
  `pickup_latitude` decimal(10,7) NOT NULL COMMENT '取货纬度',
  `delivery_address` varchar(255) NOT NULL COMMENT '配送地址',
  `delivery_longitude` decimal(10,7) NOT NULL COMMENT '配送经度',
  `delivery_latitude` decimal(10,7) NOT NULL COMMENT '配送纬度',
  `distance` decimal(8,2) NOT NULL COMMENT '配送距离(公里)',
  `weight` decimal(8,2) NOT NULL COMMENT '货物重量(公斤)',
  `goods_description` varchar(500) DEFAULT NULL COMMENT '货物描述',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `delivery_fee` decimal(10,2) NOT NULL COMMENT '配送费',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态：1-待支付，2-已支付，3-已接单，4-取货中，5-配送中，6-已送达，7-已取消，8-配送失败，9-已退款',
  `estimated_time` int DEFAULT NULL COMMENT '预计配送时间(分钟)',
  `actual_time` int DEFAULT NULL COMMENT '实际配送时间(分钟)',
  `pickup_time` datetime DEFAULT NULL COMMENT '取货时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pilot_id` (`pilot_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 飞手信息表
CREATE TABLE `pilot_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '飞手信息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `license_no` varchar(50) NOT NULL COMMENT '飞行执照号',
  `license_expire_date` date NOT NULL COMMENT '执照过期时间',
  `drone_model` varchar(100) NOT NULL COMMENT '无人机型号',
  `drone_no` varchar(50) NOT NULL COMMENT '无人机编号',
  `max_load` decimal(8,2) NOT NULL COMMENT '最大载重(公斤)',
  `max_distance` decimal(8,2) NOT NULL COMMENT '最大飞行距离(公里)',
  `current_longitude` decimal(10,7) DEFAULT NULL COMMENT '当前经度',
  `current_latitude` decimal(10,7) DEFAULT NULL COMMENT '当前纬度',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-离线，1-在线空闲，2-执行任务中',
  `rating` decimal(3,2) DEFAULT '5.00' COMMENT '评分',
  `total_orders` int DEFAULT '0' COMMENT '总订单数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_license_no` (`license_no`),
  UNIQUE KEY `uk_drone_no` (`drone_no`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`current_longitude`,`current_latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='飞手信息表';

-- 支付记录表
CREATE TABLE `payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `payment_no` varchar(32) NOT NULL COMMENT '支付流水号',
  `third_party_no` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `payment_type` tinyint NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-余额支付',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '支付状态：1-待支付，2-支付成功，3-支付失败，4-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `user_type`) VALUES
('admin', '$2a$10$7JB720yubVSOfvVMe6LevuELAhZyWGaQxsm3CKjjYdXN2gs4wHWvi', '管理员', '13800138000', 3),
('pilot001', '$2a$10$7JB720yubVSOfvVMe6LevuELAhZyWGaQxsm3CKjjYdXN2gs4wHWvi', '飞手001', '13800138001', 2),
('user001', '$2a$10$7JB720yubVSOfvVMe6LevuELAhZyWGaQxsm3CKjjYdXN2gs4wHWvi', '用户001', '13800138002', 1);