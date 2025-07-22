package com.drone.delivery.order.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.common.result.ResultCode;
import com.drone.delivery.order.dto.OrderCreateDTO;
import com.drone.delivery.order.dto.OrderQueryDTO;
import com.drone.delivery.order.entity.Order;
import com.drone.delivery.order.mapper.OrderMapper;
import com.drone.delivery.order.service.OrderService;
import com.drone.delivery.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO orderCreateDTO, Long userId) {
        // 计算配送距离
        BigDecimal distance = calculateDistance(
                orderCreateDTO.getPickupLongitude(),
                orderCreateDTO.getPickupLatitude(),
                orderCreateDTO.getDeliveryLongitude(),
                orderCreateDTO.getDeliveryLatitude()
        );

        // 计算订单金额
        BigDecimal amount = calculateAmount(distance, orderCreateDTO.getGoodsWeight(), orderCreateDTO.getOrderType());

        // 计算预计配送时间
        Integer estimatedTime = calculateEstimatedTime(distance);

        // 创建订单对象
        Order order = new Order();
        BeanUtils.copyProperties(orderCreateDTO, order);
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(1); // 待接单
        order.setDistance(distance);
        order.setEstimatedTime(estimatedTime);
        order.setAmount(amount);
        order.setPayAmount(amount);
        order.setPayStatus(1); // 未支付

        // 保存订单
        orderMapper.insert(order);
        
        log.info("创建订单成功，订单号：{}，用户ID：{}", order.getOrderNo(), userId);
        return order.getId();
    }

    /**
     * 分页查询订单
     */
    @Override
    public Page<OrderVO> pageOrders(OrderQueryDTO queryDTO) {
        Page<Order> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(queryDTO.getOrderNo()), Order::getOrderNo, queryDTO.getOrderNo())
                .eq(queryDTO.getUserId() != null, Order::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getPilotId() != null, Order::getPilotId, queryDTO.getPilotId())
                .eq(queryDTO.getOrderType() != null, Order::getOrderType, queryDTO.getOrderType())
                .eq(queryDTO.getStatus() != null, Order::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getPayStatus() != null, Order::getPayStatus, queryDTO.getPayStatus())
                .like(StrUtil.isNotBlank(queryDTO.getReceiverName()), Order::getReceiverName, queryDTO.getReceiverName())
                .like(StrUtil.isNotBlank(queryDTO.getReceiverPhone()), Order::getReceiverPhone, queryDTO.getReceiverPhone())
                .ge(queryDTO.getStartTime() != null, Order::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, Order::getCreateTime, queryDTO.getEndTime())
                .orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);
        
        // 转换为VO
        List<OrderVO> orderVOList = orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<OrderVO> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(orderVOList);
        return result;
    }

    /**
     * 根据ID查询订单详情
     */
    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        return convertToVO(order);
    }

    /**
     * 根据订单号查询订单详情
     */
    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        return convertToVO(order);
    }

    /**
     * 接单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean acceptOrder(Long orderId, Long pilotId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getStatus().equals(1)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态不允许接单");
        }

        order.setPilotId(pilotId);
        order.setStatus(2); // 已接单
        order.setAcceptTime(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("飞手接单，订单ID：{}，飞手ID：{}", orderId, pilotId);
        return result > 0;
    }

    /**
     * 开始配送
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startDelivery(Long orderId, Long pilotId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getStatus().equals(2)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态不允许开始配送");
        }
        if (!order.getPilotId().equals(pilotId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有接单飞手才能开始配送");
        }

        order.setStatus(3); // 配送中
        order.setStartTime(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("开始配送，订单ID：{}，飞手ID：{}", orderId, pilotId);
        return result > 0;
    }

    /**
     * 完成订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishOrder(Long orderId, Long pilotId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getStatus().equals(3)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单状态不允许完成");
        }
        if (!order.getPilotId().equals(pilotId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有配送飞手才能完成订单");
        }

        order.setStatus(4); // 已完成
        order.setFinishTime(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("完成订单，订单ID：{}，飞手ID：{}", orderId, pilotId);
        return result > 0;
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId, Long userId, String cancelReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能取消自己的订单");
        }
        if (order.getStatus() >= 3) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单已开始配送，无法取消");
        }

        order.setStatus(5); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(cancelReason);
        
        int result = orderMapper.updateById(order);
        log.info("取消订单，订单ID：{}，用户ID：{}，取消原因：{}", orderId, userId, cancelReason);
        return result > 0;
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(Long orderId, Integer payType) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getPayStatus().equals(1)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "订单已支付或已退款");
        }

        order.setPayStatus(2); // 已支付
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        log.info("支付订单，订单ID：{}，支付方式：{}", orderId, payType);
        return result > 0;
    }

    /**
     * 计算订单金额
     */
    @Override
    public BigDecimal calculateAmount(BigDecimal distance, BigDecimal weight, Integer orderType) {
        // 基础价格：起步价10元
        BigDecimal basePrice = new BigDecimal("10.00");
        
        // 距离费用：每公里2元
        BigDecimal distancePrice = distance.multiply(new BigDecimal("2.00"));
        
        // 重量费用：超过5kg的部分，每公斤1元
        BigDecimal weightPrice = BigDecimal.ZERO;
        if (weight.compareTo(new BigDecimal("5.0")) > 0) {
            weightPrice = weight.subtract(new BigDecimal("5.0")).multiply(new BigDecimal("1.00"));
        }
        
        // 预约配送加收20%
        BigDecimal totalPrice = basePrice.add(distancePrice).add(weightPrice);
        if (orderType.equals(2)) {
            totalPrice = totalPrice.multiply(new BigDecimal("1.2"));
        }
        
        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算配送距离（使用简单的直线距离计算）
     */
    @Override
    public BigDecimal calculateDistance(BigDecimal pickupLongitude, BigDecimal pickupLatitude,
                                       BigDecimal deliveryLongitude, BigDecimal deliveryLatitude) {
        // 使用简单的直线距离计算（实际项目中应该调用地图API）
        double lat1 = pickupLatitude.doubleValue();
        double lon1 = pickupLongitude.doubleValue();
        double lat2 = deliveryLatitude.doubleValue();
        double lon2 = deliveryLongitude.doubleValue();
        
        double earthRadius = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        
        return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算预计配送时间
     */
    @Override
    public Integer calculateEstimatedTime(BigDecimal distance) {
        // 假设无人机平均速度为30km/h，加上起降和装卸时间
        double speed = 30.0; // km/h
        double timeInHours = distance.doubleValue() / speed;
        int timeInMinutes = (int) Math.ceil(timeInHours * 60);
        
        // 最少15分钟，加上10分钟的起降和装卸时间
        return Math.max(timeInMinutes + 10, 15);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "DD" + System.currentTimeMillis() + IdUtil.randomNumbers(4);
    }

    /**
     * 转换为VO对象
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置描述字段
        vo.setOrderTypeDesc(getOrderTypeDesc(order.getOrderType()));
        vo.setStatusDesc(getStatusDesc(order.getStatus()));
        vo.setPayStatusDesc(getPayStatusDesc(order.getPayStatus()));
        vo.setPayTypeDesc(getPayTypeDesc(order.getPayType()));
        
        return vo;
    }

    /**
     * 获取订单类型描述
     */
    private String getOrderTypeDesc(Integer orderType) {
        if (orderType == null) return "";
        switch (orderType) {
            case 1: return "即时配送";
            case 2: return "预约配送";
            default: return "未知";
        }
    }

    /**
     * 获取订单状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "待接单";
            case 2: return "已接单";
            case 3: return "配送中";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 获取支付状态描述
     */
    private String getPayStatusDesc(Integer payStatus) {
        if (payStatus == null) return "";
        switch (payStatus) {
            case 1: return "未支付";
            case 2: return "已支付";
            case 3: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 获取支付方式描述
     */
    private String getPayTypeDesc(Integer payType) {
        if (payType == null) return "";
        switch (payType) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "余额支付";
            default: return "未知";
        }
    }
}