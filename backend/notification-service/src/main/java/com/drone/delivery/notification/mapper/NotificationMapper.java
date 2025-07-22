package com.drone.delivery.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.delivery.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知Mapper接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}