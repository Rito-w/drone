package com.drone.delivery.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.result.Result;
import com.drone.delivery.order.dto.OrderCreateDTO;
import com.drone.delivery.order.dto.OrderQueryDTO;
import com.drone.delivery.order.service.OrderService;
import com.drone.delivery.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 订单控制器
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * 
     * @param orderCreateDTO 订单创建DTO
     * @return 订单ID
     */
    @PostMapping("/create")
    public Result<Long> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        // TODO: 从JWT token中获取用户ID
        Long userId = 1L; // 临时写死，实际应该从token中获取
        Long orderId = orderService.createOrder(orderCreateDTO, userId);
        return Result.success(orderId);
    }

    /**
     * 分页查询订单
     * 
     * @param queryDTO 查询条件
     * @return 订单分页数据
     */
    @PostMapping("/page")
    public Result<Page<OrderVO>> pageOrders(@RequestBody OrderQueryDTO queryDTO) {
        Page<OrderVO> page = orderService.pageOrders(queryDTO);
        return Result.success(page);
    }

    /**
     * 根据ID查询订单详情
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    /**
     * 根据订单号查询订单详情
     * 
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/orderNo/{orderNo}")
    public Result<OrderVO> getOrderByOrderNo(@PathVariable String orderNo) {
        OrderVO orderVO = orderService.getOrderByOrderNo(orderNo);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * 
     * @param orderId 订单ID
     * @return 是否成功
     */
    @PostMapping("/{orderId}/accept")
    public Result<Boolean> acceptOrder(@PathVariable Long orderId) {
        // TODO: 从JWT token中获取飞手ID
        Long pilotId = 1L; // 临时写死，实际应该从token中获取
        Boolean result = orderService.acceptOrder(orderId, pilotId);
        return Result.success(result);
    }

    /**
     * 开始配送
     * 
     * @param orderId 订单ID
     * @return 是否成功
     */
    @PostMapping("/{orderId}/start")
    public Result<Boolean> startDelivery(@PathVariable Long orderId) {
        // TODO: 从JWT token中获取飞手ID
        Long pilotId = 1L; // 临时写死，实际应该从token中获取
        Boolean result = orderService.startDelivery(orderId, pilotId);
        return Result.success(result);
    }

    /**
     * 完成订单
     * 
     * @param orderId 订单ID
     * @return 是否成功
     */
    @PostMapping("/{orderId}/finish")
    public Result<Boolean> finishOrder(@PathVariable Long orderId) {
        // TODO: 从JWT token中获取飞手ID
        Long pilotId = 1L; // 临时写死，实际应该从token中获取
        Boolean result = orderService.finishOrder(orderId, pilotId);
        return Result.success(result);
    }

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long orderId, @RequestParam String cancelReason) {
        // TODO: 从JWT token中获取用户ID
        Long userId = 1L; // 临时写死，实际应该从token中获取
        Boolean result = orderService.cancelOrder(orderId, userId, cancelReason);
        return Result.success(result);
    }

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param payType 支付方式
     * @return 是否成功
     */
    @PostMapping("/{orderId}/pay")
    public Result<Boolean> payOrder(@PathVariable Long orderId, @RequestParam Integer payType) {
        Boolean result = orderService.payOrder(orderId, payType);
        return Result.success(result);
    }

    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Order Service is running");
    }
}