package com.drone.delivery.payment.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付查询DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PaymentQueryDTO {

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-银行卡，4-余额支付
     */
    private Integer paymentMethod;

    /**
     * 支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败，5-已退款
     */
    private Integer status;

    /**
     * 支付渠道：1-APP，2-H5，3-小程序，4-PC
     */
    private Integer channel;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

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