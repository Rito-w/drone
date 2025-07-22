package com.drone.delivery.order.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 订单查询DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class OrderQueryDTO {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 飞手ID
     */
    private Long pilotId;

    /**
     * 订单类型：1-即时配送，2-预约配送
     */
    private Integer orderType;

    /**
     * 订单状态：1-待接单，2-已接单，3-配送中，4-已完成，5-已取消
     */
    private Integer status;

    /**
     * 支付状态：1-未支付，2-已支付，3-已退款
     */
    private Integer payStatus;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

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