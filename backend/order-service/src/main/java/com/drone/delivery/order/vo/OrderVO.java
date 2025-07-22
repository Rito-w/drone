package com.drone.delivery.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class OrderVO {

    /**
     * 订单ID
     */
    private Long id;

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
     * 飞手姓名
     */
    private String pilotName;

    /**
     * 订单类型：1-即时配送，2-预约配送
     */
    private Integer orderType;

    /**
     * 订单类型描述
     */
    private String orderTypeDesc;

    /**
     * 订单状态：1-待接单，2-已接单，3-配送中，4-已完成，5-已取消
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 取货地址
     */
    private String pickupAddress;

    /**
     * 取货详细地址
     */
    private String pickupDetailAddress;

    /**
     * 取货经度
     */
    private BigDecimal pickupLongitude;

    /**
     * 取货纬度
     */
    private BigDecimal pickupLatitude;

    /**
     * 收货地址
     */
    private String deliveryAddress;

    /**
     * 收货详细地址
     */
    private String deliveryDetailAddress;

    /**
     * 收货经度
     */
    private BigDecimal deliveryLongitude;

    /**
     * 收货纬度
     */
    private BigDecimal deliveryLatitude;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 货物描述
     */
    private String goodsDescription;

    /**
     * 货物重量（kg）
     */
    private BigDecimal goodsWeight;

    /**
     * 配送距离（km）
     */
    private BigDecimal distance;

    /**
     * 预计配送时间（分钟）
     */
    private Integer estimatedTime;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付状态：1-未支付，2-已支付，3-已退款
     */
    private Integer payStatus;

    /**
     * 支付状态描述
     */
    private String payStatusDesc;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-余额支付
     */
    private Integer payType;

    /**
     * 支付方式描述
     */
    private String payTypeDesc;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 预约配送时间
     */
    private LocalDateTime appointmentTime;

    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;

    /**
     * 开始配送时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

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