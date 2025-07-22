package com.drone.delivery.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.order.dto.OrderCreateDTO;
import com.drone.delivery.order.dto.OrderQueryDTO;
import com.drone.delivery.order.entity.Order;
import com.drone.delivery.order.vo.OrderVO;

/**
 * 订单服务接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
public interface OrderService {

    /**
     * 创建订单
     * 
     * @param orderCreateDTO 订单创建DTO
     * @param userId 用户ID
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO orderCreateDTO, Long userId);

    /**
     * 分页查询订单
     * 
     * @param queryDTO 查询条件
     * @return 订单分页数据
     */
    Page<OrderVO> pageOrders(OrderQueryDTO queryDTO);

    /**
     * 根据ID查询订单详情
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    OrderVO getOrderById(Long id);

    /**
     * 根据订单号查询订单详情
     * 
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 接单
     * 
     * @param orderId 订单ID
     * @param pilotId 飞手ID
     * @return 是否成功
     */
    Boolean acceptOrder(Long orderId, Long pilotId);

    /**
     * 开始配送
     * 
     * @param orderId 订单ID
     * @param pilotId 飞手ID
     * @return 是否成功
     */
    Boolean startDelivery(Long orderId, Long pilotId);

    /**
     * 完成订单
     * 
     * @param orderId 订单ID
     * @param pilotId 飞手ID
     * @return 是否成功
     */
    Boolean finishOrder(Long orderId, Long pilotId);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId, Long userId, String cancelReason);

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param payType 支付方式
     * @return 是否成功
     */
    Boolean payOrder(Long orderId, Integer payType);

    /**
     * 计算订单金额
     * 
     * @param distance 配送距离
     * @param weight 货物重量
     * @param orderType 订单类型
     * @return 订单金额
     */
    java.math.BigDecimal calculateAmount(java.math.BigDecimal distance, java.math.BigDecimal weight, Integer orderType);

    /**
     * 计算配送距离
     * 
     * @param pickupLongitude 取货经度
     * @param pickupLatitude 取货纬度
     * @param deliveryLongitude 收货经度
     * @param deliveryLatitude 收货纬度
     * @return 配送距离（km）
     */
    java.math.BigDecimal calculateDistance(java.math.BigDecimal pickupLongitude, java.math.BigDecimal pickupLatitude,
                                          java.math.BigDecimal deliveryLongitude, java.math.BigDecimal deliveryLatitude);

    /**
     * 计算预计配送时间
     * 
     * @param distance 配送距离
     * @return 预计配送时间（分钟）
     */
    Integer calculateEstimatedTime(java.math.BigDecimal distance);
}