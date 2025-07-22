package com.drone.delivery.pilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.delivery.pilot.entity.Pilot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 飞手Mapper接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Mapper
public interface PilotMapper extends BaseMapper<Pilot> {

}