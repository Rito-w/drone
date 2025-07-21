package com.drone.delivery.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.common.utils.JwtUtils;
import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.entity.User;
import com.drone.delivery.user.mapper.UserMapper;
import com.drone.delivery.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author drone-delivery
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return JWT令牌
     */
    @Override
    public String login(UserLoginDTO loginDTO) {
        log.info("用户登录，用户名：{}", loginDTO.getUsername());
        
        // 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(400, "账户已被禁用");
        }
        
        // 生成JWT令牌
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getUserType());
        log.info("用户登录成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
        
        return token;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getIsDeleted, 0);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public User getById(Long id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id);
        queryWrapper.eq(User::getIsDeleted, 0);
        return userMapper.selectOne(queryWrapper);
    }
}