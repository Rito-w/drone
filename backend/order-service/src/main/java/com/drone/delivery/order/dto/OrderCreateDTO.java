package com.drone.delivery.order.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建订单DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class OrderCreateDTO {

    /**
     * 订单类型：1-即时配送，2-预约配送
     */
    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型值错误")
    @Max(value = 2, message = "订单类型值错误")
    private Integer orderType;

    /**
     * 取货地址
     */
    @NotBlank(message = "取货地址不能为空")
    @Size(max = 200, message = "取货地址长度不能超过200字符")
    private String pickupAddress;

    /**
     * 取货详细地址
     */
    @Size(max = 500, message = "取货详细地址长度不能超过500字符")
    private String pickupDetailAddress;

    /**
     * 取货经度
     */
    @NotNull(message = "取货经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度值错误")
    @DecimalMax(value = "180.0", message = "经度值错误")
    private BigDecimal pickupLongitude;

    /**
     * 取货纬度
     */
    @NotNull(message = "取货纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度值错误")
    @DecimalMax(value = "90.0", message = "纬度值错误")
    private BigDecimal pickupLatitude;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址长度不能超过200字符")
    private String deliveryAddress;

    /**
     * 收货详细地址
     */
    @Size(max = 500, message = "收货详细地址长度不能超过500字符")
    private String deliveryDetailAddress;

    /**
     * 收货经度
     */
    @NotNull(message = "收货经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度值错误")
    @DecimalMax(value = "180.0", message = "经度值错误")
    private BigDecimal deliveryLongitude;

    /**
     * 收货纬度
     */
    @NotNull(message = "收货纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度值错误")
    @DecimalMax(value = "90.0", message = "纬度值错误")
    private BigDecimal deliveryLatitude;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "收货人电话格式错误")
    private String receiverPhone;

    /**
     * 货物描述
     */
    @NotBlank(message = "货物描述不能为空")
    @Size(max = 500, message = "货物描述长度不能超过500字符")
    private String goodsDescription;

    /**
     * 货物重量（kg）
     */
    @NotNull(message = "货物重量不能为空")
    @DecimalMin(value = "0.1", message = "货物重量不能小于0.1kg")
    @DecimalMax(value = "50.0", message = "货物重量不能超过50kg")
    private BigDecimal goodsWeight;

    /**
     * 预约配送时间（订单类型为预约配送时必填）
     */
    private LocalDateTime appointmentTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}