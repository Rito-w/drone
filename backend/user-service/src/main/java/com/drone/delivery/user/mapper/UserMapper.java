package com.drone.delivery.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.delivery.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author drone-delivery
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}