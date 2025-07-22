package com.drone.delivery.payment.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 支付请求DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PaymentRequestDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0.01")
    private BigDecimal amount;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-银行卡，4-余额支付
     */
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式值错误")
    @Max(value = 4, message = "支付方式值错误")
    private Integer paymentMethod;

    /**
     * 支付渠道：1-APP，2-H5，3-小程序，4-PC
     */
    @NotNull(message = "支付渠道不能为空")
    @Min(value = 1, message = "支付渠道值错误")
    @Max(value = 4, message = "支付渠道值错误")
    private Integer channel;

    /**
     * 客户端IP
     */
    @NotBlank(message = "客户端IP不能为空")
    private String clientIp;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}