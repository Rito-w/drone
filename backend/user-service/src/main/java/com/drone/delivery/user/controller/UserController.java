package com.drone.delivery.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.vo.Result;
import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.dto.UserQueryDTO;
import com.drone.delivery.user.dto.UserRegisterDTO;
import com.drone.delivery.user.dto.UserUpdateDTO;
import com.drone.delivery.user.service.UserService;
import com.drone.delivery.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 *
 * @author drone-delivery
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求，用户名：{}", loginDTO.getUsername());
        String token = userService.login(loginDTO);
        return Result.success(token, "登录成功");
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Boolean> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册请求，用户名：{}", registerDTO.getUsername());
        Boolean result = userService.register(registerDTO);
        return Result.success(result, "注册成功");
    }

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户分页数据
     */
    @GetMapping("/page")
    public Result<Page<UserVO>> pageQuery(UserQueryDTO queryDTO) {
        log.info("分页查询用户列表，查询条件：{}", queryDTO);
        Page<UserVO> page = userService.pageQuery(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        log.info("查询用户详情，用户ID：{}", id);
        UserVO userVO = userService.getUserDetail(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @PutMapping
    public Result<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("更新用户信息，用户ID：{}", updateDTO.getId());
        Boolean result = userService.updateUser(updateDTO);
        return Result.success(result, "更新成功");
    }

    /**
     * 切换用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("切换用户状态，用户ID：{}，状态：{}", id, status);
        Boolean result = userService.updateStatus(id, status);
        return Result.success(result, "状态更新成功");
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        log.info("删除用户，用户ID：{}", id);
        Boolean result = userService.deleteUser(id);
        return Result.success(result, "删除成功");
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        Boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    @GetMapping("/check/phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        Boolean exists = userService.existsByPhone(phone);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @GetMapping("/check/email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        Boolean exists = userService.existsByEmail(email);
        return Result.success(exists);
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常");
    }
}