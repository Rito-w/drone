package com.drone.delivery.user.controller;

import com.drone.delivery.common.vo.Result;
import com.drone.delivery.user.dto.UserLoginDTO;
import com.drone.delivery.user.service.UserService;
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
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常");
    }
}