package com.drone.delivery.pilot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.common.result.Result;
import com.drone.delivery.pilot.dto.PilotQueryDTO;
import com.drone.delivery.pilot.dto.PilotRegisterDTO;
import com.drone.delivery.pilot.service.PilotService;
import com.drone.delivery.pilot.vo.PilotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 飞手控制器
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/pilot")
@RequiredArgsConstructor
@Validated
public class PilotController {

    private final PilotService pilotService;

    /**
     * 飞手注册
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody PilotRegisterDTO registerDTO) {
        Long pilotId = pilotService.register(registerDTO);
        return Result.success(pilotId);
    }

    /**
     * 分页查询飞手
     */
    @PostMapping("/page")
    public Result<IPage<PilotVO>> page(@Valid @RequestBody PilotQueryDTO queryDTO) {
        IPage<PilotVO> page = pilotService.page(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据ID查询飞手详情
     */
    @GetMapping("/{id}")
    public Result<PilotVO> getById(@PathVariable Long id) {
        PilotVO pilot = pilotService.getById(id);
        return Result.success(pilot);
    }

    /**
     * 根据用户ID查询飞手详情
     */
    @GetMapping("/user/{userId}")
    public Result<PilotVO> getByUserId(@PathVariable Long userId) {
        PilotVO pilot = pilotService.getByUserId(userId);
        return Result.success(pilot);
    }

    /**
     * 更新飞手位置
     */
    @PutMapping("/{id}/location")
    public Result<Boolean> updateLocation(@PathVariable Long id,
                                        @RequestParam @NotNull BigDecimal longitude,
                                        @RequestParam @NotNull BigDecimal latitude) {
        Boolean success = pilotService.updateLocation(id, longitude, latitude);
        return Result.success(success);
    }

    /**
     * 更新工作状态
     */
    @PutMapping("/{id}/work-status")
    public Result<Boolean> updateWorkStatus(@PathVariable Long id,
                                          @RequestParam @NotNull Integer workStatus) {
        Boolean success = pilotService.updateWorkStatus(id, workStatus);
        return Result.success(success);
    }

    /**
     * 认证飞手
     */
    @PutMapping("/{id}/certify")
    public Result<Boolean> certify(@PathVariable Long id,
                                 @RequestParam @NotNull Integer status,
                                 @RequestParam(required = false) String reason) {
        Boolean success = pilotService.certify(id, status, reason);
        return Result.success(success);
    }

    /**
     * 更新账户状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id,
                                      @RequestParam @NotNull Integer status) {
        Boolean success = pilotService.updateStatus(id, status);
        return Result.success(success);
    }

    /**
     * 查找附近的空闲飞手
     */
    @GetMapping("/nearby")
    public Result<List<PilotVO>> findNearbyPilots(@RequestParam @NotNull BigDecimal longitude,
                                                 @RequestParam @NotNull BigDecimal latitude,
                                                 @RequestParam(defaultValue = "10") Integer radius) {
        List<PilotVO> pilots = pilotService.findNearbyPilots(longitude, latitude, radius);
        return Result.success(pilots);
    }

    /**
     * 更新飞手评分
     */
    @PutMapping("/{id}/rating")
    public Result<Boolean> updateRating(@PathVariable Long id,
                                      @RequestParam @NotNull BigDecimal rating) {
        Boolean success = pilotService.updateRating(id, rating);
        return Result.success(success);
    }

    /**
     * 增加完成订单数
     */
    @PutMapping("/{id}/increment-orders")
    public Result<Boolean> incrementCompletedOrders(@PathVariable Long id) {
        Boolean success = pilotService.incrementCompletedOrders(id);
        return Result.success(success);
    }

    /**
     * 增加收入
     */
    @PutMapping("/{id}/add-income")
    public Result<Boolean> addIncome(@PathVariable Long id,
                                   @RequestParam @NotNull BigDecimal amount) {
        Boolean success = pilotService.addIncome(id, amount);
        return Result.success(success);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Pilot Service is running");
    }
}