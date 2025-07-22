package com.drone.delivery.pilot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞手视图对象
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PilotVO {

    /**
     * 飞手ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 飞手姓名
     */
    private String name;

    /**
     * 身份证号（脱敏）
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：1-男，2-女
     */
    private Integer gender;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 地址
     */
    private String address;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 飞行执照号
     */
    private String licenseNo;

    /**
     * 执照类型：1-民用无人机驾驶员执照，2-商用无人机驾驶员执照
     */
    private Integer licenseType;

    /**
     * 执照类型描述
     */
    private String licenseTypeDesc;

    /**
     * 执照颁发日期
     */
    private LocalDateTime licenseIssueDate;

    /**
     * 执照到期日期
     */
    private LocalDateTime licenseExpireDate;

    /**
     * 执照照片
     */
    private String licensePhoto;

    /**
     * 飞行经验（小时）
     */
    private Integer flightHours;

    /**
     * 认证状态：1-待认证，2-认证中，3-已认证，4-认证失败
     */
    private Integer certificationStatus;

    /**
     * 认证状态描述
     */
    private String certificationStatusDesc;

    /**
     * 认证时间
     */
    private LocalDateTime certificationTime;

    /**
     * 认证失败原因
     */
    private String certificationFailReason;

    /**
     * 工作状态：1-空闲，2-忙碌，3-离线
     */
    private Integer workStatus;

    /**
     * 工作状态描述
     */
    private String workStatusDesc;

    /**
     * 当前位置经度
     */
    private BigDecimal longitude;

    /**
     * 当前位置纬度
     */
    private BigDecimal latitude;

    /**
     * 服务范围（km）
     */
    private Integer serviceRange;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 完成订单数
     */
    private Integer completedOrders;

    /**
     * 总收入
     */
    private BigDecimal totalIncome;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 账户状态：1-正常，2-冻结，3-禁用
     */
    private Integer status;

    /**
     * 账户状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}