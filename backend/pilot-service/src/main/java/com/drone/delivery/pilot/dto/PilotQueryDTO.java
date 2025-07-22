package com.drone.delivery.pilot.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 飞手查询DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PilotQueryDTO {

    /**
     * 飞手姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 执照类型：1-民用无人机驾驶员执照，2-商用无人机驾驶员执照
     */
    private Integer licenseType;

    /**
     * 认证状态：1-待认证，2-认证中，3-已认证，4-认证失败
     */
    private Integer certificationStatus;

    /**
     * 工作状态：1-空闲，2-忙碌，3-离线
     */
    private Integer workStatus;

    /**
     * 账户状态：1-正常，2-冻结，3-禁用
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer size = 10;
}