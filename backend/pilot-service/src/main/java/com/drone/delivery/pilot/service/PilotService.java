package com.drone.delivery.pilot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.pilot.dto.PilotQueryDTO;
import com.drone.delivery.pilot.dto.PilotRegisterDTO;
import com.drone.delivery.pilot.vo.PilotVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 飞手服务接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
public interface PilotService {

    /**
     * 飞手注册
     * 
     * @param registerDTO 注册信息
     * @return 飞手ID
     */
    Long register(PilotRegisterDTO registerDTO);

    /**
     * 分页查询飞手
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PilotVO> page(PilotQueryDTO queryDTO);

    /**
     * 根据ID查询飞手详情
     * 
     * @param id 飞手ID
     * @return 飞手详情
     */
    PilotVO getById(Long id);

    /**
     * 根据用户ID查询飞手详情
     * 
     * @param userId 用户ID
     * @return 飞手详情
     */
    PilotVO getByUserId(Long userId);

    /**
     * 更新飞手位置
     * 
     * @param id 飞手ID
     * @param longitude 经度
     * @param latitude 纬度
     * @return 是否成功
     */
    Boolean updateLocation(Long id, BigDecimal longitude, BigDecimal latitude);

    /**
     * 更新工作状态
     * 
     * @param id 飞手ID
     * @param workStatus 工作状态：1-空闲，2-忙碌，3-离线
     * @return 是否成功
     */
    Boolean updateWorkStatus(Long id, Integer workStatus);

    /**
     * 认证飞手
     * 
     * @param id 飞手ID
     * @param status 认证状态：3-已认证，4-认证失败
     * @param reason 失败原因（认证失败时必填）
     * @return 是否成功
     */
    Boolean certify(Long id, Integer status, String reason);

    /**
     * 冻结/解冻飞手
     * 
     * @param id 飞手ID
     * @param status 账户状态：1-正常，2-冻结，3-禁用
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 根据位置查找附近的空闲飞手
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 搜索半径（km）
     * @return 飞手列表
     */
    List<PilotVO> findNearbyPilots(BigDecimal longitude, BigDecimal latitude, Integer radius);

    /**
     * 更新飞手评分
     * 
     * @param id 飞手ID
     * @param rating 评分
     * @return 是否成功
     */
    Boolean updateRating(Long id, BigDecimal rating);

    /**
     * 增加完成订单数
     * 
     * @param id 飞手ID
     * @return 是否成功
     */
    Boolean incrementCompletedOrders(Long id);

    /**
     * 增加收入
     * 
     * @param id 飞手ID
     * @param amount 收入金额
     * @return 是否成功
     */
    Boolean addIncome(Long id, BigDecimal amount);
}