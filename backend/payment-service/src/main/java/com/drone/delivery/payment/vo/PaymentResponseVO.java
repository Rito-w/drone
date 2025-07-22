package com.drone.delivery.payment.vo;

import lombok.Data;

/**
 * 支付响应VO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PaymentResponseVO {

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败，5-已退款
     */
    private Integer status;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 支付跳转URL（用于H5支付）
     */
    private String paymentUrl;

    /**
     * 支付二维码（用于扫码支付）
     */
    private String qrCode;

    /**
     * 支付参数（用于APP调起支付）
     */
    private String paymentParams;

    /**
     * 错误信息
     */
    private String errorMessage;
}