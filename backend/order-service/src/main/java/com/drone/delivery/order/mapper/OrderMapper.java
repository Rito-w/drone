package com.drone.delivery.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.delivery.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}