package com.drone.delivery.user.service;

import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.entity.User;

/**
 * 用户服务接口
 *
 * @author drone-delivery
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return JWT令牌
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);
}