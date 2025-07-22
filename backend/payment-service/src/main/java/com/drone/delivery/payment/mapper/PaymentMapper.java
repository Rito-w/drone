package com.drone.delivery.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.delivery.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付Mapper接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

}