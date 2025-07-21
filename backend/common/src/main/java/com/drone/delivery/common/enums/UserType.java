package com.drone.delivery.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 * 定义系统中不同类型的用户角色
 * 
 * @author Drone Delivery Team
 */
@Getter
@AllArgsConstructor
public enum UserType {
    
    /**
     * 普通用户（下单用户）
     */
    CUSTOMER(0, "普通用户"),
    
    /**
     * 飞手（配送员）
     */
    PILOT(1, "飞手"),
    
    /**
     * 管理员
     */
    ADMIN(2, "管理员"),
    
    /**
     * 商家
     */
    MERCHANT(3, "商家");
    
    /**
     * 用户类型码
     */
    private final Integer code;
    
    /**
     * 用户类型描述
     */
    private final String description;
    
    /**
     * 根据类型码获取枚举
     * 
     * @param code 类型码
     * @return 对应的枚举值
     */
    public static UserType getByCode(Integer code) {
        for (UserType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的用户类型码: " + code);
    }
}