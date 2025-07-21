package com.drone.delivery.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.drone.delivery.common.entity.BaseEntity;
import com.drone.delivery.common.enums.UserType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * @author drone-delivery
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}