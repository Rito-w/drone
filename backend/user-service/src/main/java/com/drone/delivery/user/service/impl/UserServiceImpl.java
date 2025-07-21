package com.drone.delivery.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.common.utils.JwtUtils;
import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.dto.UserQueryDTO;
import com.drone.delivery.user.dto.UserRegisterDTO;
import com.drone.delivery.user.dto.UserUpdateDTO;
import com.drone.delivery.user.entity.User;
import com.drone.delivery.user.mapper.UserMapper;
import com.drone.delivery.user.service.UserService;
import com.drone.delivery.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        
        // 更新最后登录时间
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                    .set(User::getLastLoginTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);
        
        // 生成JWT令牌
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getUserType());
        log.info("用户登录成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
        
        return token;
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @Override
    public Boolean register(UserRegisterDTO registerDTO) {
        log.info("用户注册，用户名：{}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        if (existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (StringUtils.hasText(registerDTO.getPhone()) && existsByPhone(registerDTO.getPhone())) {
            throw new BusinessException(400, "手机号已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerDTO.getEmail()) && existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException(400, "邮箱已存在");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.insert(user);
        log.info("用户注册成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
        
        return result > 0;
    }

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    @Override
    public Page<UserVO> pageQuery(UserQueryDTO queryDTO) {
        log.info("分页查询用户列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDeleted, 0);
        
        if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getNickname())) {
            queryWrapper.like(User::getNickname, queryDTO.getNickname());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like(User::getPhone, queryDTO.getPhone());
        }
        if (StringUtils.hasText(queryDTO.getEmail())) {
            queryWrapper.like(User::getEmail, queryDTO.getEmail());
        }
        if (queryDTO.getUserType() != null) {
            queryWrapper.eq(User::getUserType, queryDTO.getUserType());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge(User::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le(User::getCreateTime, queryDTO.getEndTime());
        }
        
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 分页查询
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(userPage, voPage);
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    public UserVO getUserDetail(Long id) {
        log.info("查询用户详情，用户ID：{}", id);
        
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        return convertToVO(user);
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @Override
    public Boolean updateUser(UserUpdateDTO updateDTO) {
        log.info("更新用户信息，用户ID：{}", updateDTO.getId());
        
        User existUser = getById(updateDTO.getId());
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getPhone()) && !updateDTO.getPhone().equals(existUser.getPhone())) {
            if (existsByPhone(updateDTO.getPhone())) {
                throw new BusinessException(400, "手机号已被其他用户使用");
            }
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getEmail()) && !updateDTO.getEmail().equals(existUser.getEmail())) {
            if (existsByEmail(updateDTO.getEmail())) {
                throw new BusinessException(400, "邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息
        User user = new User();
        BeanUtils.copyProperties(updateDTO, user);
        user.setUpdateTime(LocalDateTime.now());
        
        int result = userMapper.updateById(user);
        log.info("用户信息更新成功，用户ID：{}", updateDTO.getId());
        
        return result > 0;
    }

    /**
     * 切换用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 操作结果
     */
    @Override
    public Boolean updateStatus(Long id, Integer status) {
        log.info("切换用户状态，用户ID：{}，状态：{}", id, status);
        
        User existUser = getById(id);
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                    .set(User::getStatus, status)
                    .set(User::getUpdateTime, LocalDateTime.now());
        
        int result = userMapper.update(null, updateWrapper);
        log.info("用户状态更新成功，用户ID：{}，状态：{}", id, status);
        
        return result > 0;
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Override
    public Boolean deleteUser(Long id) {
        log.info("删除用户，用户ID：{}", id);
        
        User existUser = getById(id);
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 逻辑删除
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id)
                    .set(User::getIsDeleted, 1)
                    .set(User::getUpdateTime, LocalDateTime.now());
        
        int result = userMapper.update(null, updateWrapper);
        log.info("用户删除成功，用户ID：{}", id);
        
        return result > 0;
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    public Boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                    .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    @Override
    public Boolean existsByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone)
                    .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Override
    public Boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email)
                    .eq(User::getIsDeleted, 0);
        return userMapper.selectCount(queryWrapper) > 0;
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

    /**
     * 转换为VO对象
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 设置用户类型描述
        switch (user.getUserType()) {
            case 1:
                userVO.setUserTypeDesc("管理员");
                break;
            case 2:
                userVO.setUserTypeDesc("飞手");
                break;
            case 3:
                userVO.setUserTypeDesc("普通用户");
                break;
            default:
                userVO.setUserTypeDesc("未知");
                break;
        }
        
        // 设置状态描述
        userVO.setStatusDesc(user.getStatus() == 1 ? "启用" : "禁用");
        
        return userVO;
    }
}