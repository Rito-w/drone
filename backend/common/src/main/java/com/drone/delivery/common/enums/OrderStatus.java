package com.drone.delivery.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 * 定义订单在整个配送流程中的各种状态
 * 
 * @author Drone Delivery Team
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),
    
    /**
     * 已支付，待接单
     */
    PAID(1, "已支付"),
    
    /**
     * 已接单，待取货
     */
    ACCEPTED(2, "已接单"),
    
    /**
     * 取货中
     */
    PICKING_UP(3, "取货中"),
    
    /**
     * 配送中
     */
    DELIVERING(4, "配送中"),
    
    /**
     * 已送达
     */
    DELIVERED(5, "已送达"),
    
    /**
     * 已取消
     */
    CANCELLED(6, "已取消"),
    
    /**
     * 配送失败
     */
    FAILED(7, "配送失败"),
    
    /**
     * 已退款
     */
    REFUNDED(8, "已退款");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态码: " + code);
    }
    
    /**
     * 判断是否为终态
     * 终态包括：已送达、已取消、配送失败、已退款
     * 
     * @return true: 终态, false: 非终态
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED || this == FAILED || this == REFUNDED;
    }
    
    /**
     * 判断是否可以取消
     * 只有待支付、已支付、已接单状态可以取消
     * 
     * @return true: 可取消, false: 不可取消
     */
    public boolean canCancel() {
        return this == PENDING_PAYMENT || this == PAID || this == ACCEPTED;
    }
}