package com.drone.delivery.payment.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class RefundRequestDTO {

    /**
     * 支付ID
     */
    @NotNull(message = "支付ID不能为空")
    private Long paymentId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0.01")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 200, message = "退款原因长度不能超过200字符")
    private String refundReason;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}