package com.drone.delivery.payment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付视图对象
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PaymentVO {

    /**
     * 支付ID
     */
    private Long id;

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
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-银行卡，4-余额支付
     */
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败，5-已退款
     */
    private Integer status;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 第三方支付平台交易号
     */
    private String thirdPartyTransactionId;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 支付完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 支付失败原因
     */
    private String failReason;

    /**
     * 支付渠道：1-APP，2-H5，3-小程序，4-PC
     */
    private Integer channel;

    /**
     * 支付渠道描述
     */
    private String channelDesc;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 支付回调通知状态：1-未通知，2-通知成功，3-通知失败
     */
    private Integer notifyStatus;

    /**
     * 通知状态描述
     */
    private String notifyStatusDesc;

    /**
     * 通知次数
     */
    private Integer notifyCount;

    /**
     * 最后通知时间
     */
    private LocalDateTime lastNotifyTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}