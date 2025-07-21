package com.drone.delivery.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.dto.UserQueryDTO;
import com.drone.delivery.user.dto.UserRegisterDTO;
import com.drone.delivery.user.dto.UserUpdateDTO;
import com.drone.delivery.user.entity.User;
import com.drone.delivery.user.vo.UserVO;

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
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    Boolean register(UserRegisterDTO registerDTO);

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    Page<UserVO> pageQuery(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(Long id);

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    Boolean updateUser(UserUpdateDTO updateDTO);

    /**
     * 切换用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 操作结果
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    Boolean deleteUser(Long id);

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

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    Boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    Boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    Boolean existsByEmail(String email);
}