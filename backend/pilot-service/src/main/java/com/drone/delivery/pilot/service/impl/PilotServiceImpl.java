package com.drone.delivery.pilot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.pilot.dto.PilotQueryDTO;
import com.drone.delivery.pilot.dto.PilotRegisterDTO;
import com.drone.delivery.pilot.entity.Pilot;
import com.drone.delivery.pilot.mapper.PilotMapper;
import com.drone.delivery.pilot.service.PilotService;
import com.drone.delivery.pilot.vo.PilotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 飞手服务实现类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PilotServiceImpl implements PilotService {

    private final PilotMapper pilotMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(PilotRegisterDTO registerDTO) {
        // 检查身份证号是否已存在
        LambdaQueryWrapper<Pilot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pilot::getIdCard, registerDTO.getIdCard());
        if (pilotMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("身份证号已存在");
        }

        // 检查手机号是否已存在
        wrapper.clear();
        wrapper.eq(Pilot::getPhone, registerDTO.getPhone());
        if (pilotMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("手机号已存在");
        }

        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(registerDTO.getEmail())) {
            wrapper.clear();
            wrapper.eq(Pilot::getEmail, registerDTO.getEmail());
            if (pilotMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 检查执照号是否已存在
        wrapper.clear();
        wrapper.eq(Pilot::getLicenseNo, registerDTO.getLicenseNo());
        if (pilotMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("飞行执照号已存在");
        }

        // 创建飞手
        Pilot pilot = new Pilot();
        BeanUtil.copyProperties(registerDTO, pilot);
        pilot.setCertificationStatus(1); // 待认证
        pilot.setWorkStatus(3); // 离线
        pilot.setRating(BigDecimal.valueOf(5.0)); // 默认评分
        pilot.setCompletedOrders(0);
        pilot.setTotalIncome(BigDecimal.ZERO);
        pilot.setStatus(1); // 正常
        pilot.setCreateTime(LocalDateTime.now());
        pilot.setUpdateTime(LocalDateTime.now());

        pilotMapper.insert(pilot);
        log.info("飞手注册成功，ID: {}", pilot.getId());
        return pilot.getId();
    }

    @Override
    public IPage<PilotVO> page(PilotQueryDTO queryDTO) {
        LambdaQueryWrapper<Pilot> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        wrapper.like(StrUtil.isNotBlank(queryDTO.getName()), Pilot::getName, queryDTO.getName())
               .like(StrUtil.isNotBlank(queryDTO.getPhone()), Pilot::getPhone, queryDTO.getPhone())
               .like(StrUtil.isNotBlank(queryDTO.getEmail()), Pilot::getEmail, queryDTO.getEmail())
               .like(StrUtil.isNotBlank(queryDTO.getIdCard()), Pilot::getIdCard, queryDTO.getIdCard())
               .eq(queryDTO.getLicenseType() != null, Pilot::getLicenseType, queryDTO.getLicenseType())
               .eq(queryDTO.getCertificationStatus() != null, Pilot::getCertificationStatus, queryDTO.getCertificationStatus())
               .eq(queryDTO.getWorkStatus() != null, Pilot::getWorkStatus, queryDTO.getWorkStatus())
               .eq(queryDTO.getStatus() != null, Pilot::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getStartTime() != null, Pilot::getCreateTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, Pilot::getCreateTime, queryDTO.getEndTime())
               .orderByDesc(Pilot::getCreateTime);

        Page<Pilot> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<Pilot> pilotPage = pilotMapper.selectPage(page, wrapper);

        // 转换为VO
        IPage<PilotVO> voPage = new Page<>();
        BeanUtil.copyProperties(pilotPage, voPage);
        voPage.setRecords(pilotPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public PilotVO getById(Long id) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }
        return convertToVO(pilot);
    }

    @Override
    public PilotVO getByUserId(Long userId) {
        LambdaQueryWrapper<Pilot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pilot::getUserId, userId);
        Pilot pilot = pilotMapper.selectOne(wrapper);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }
        return convertToVO(pilot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLocation(Long id, BigDecimal longitude, BigDecimal latitude) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setLongitude(longitude);
        pilot.setLatitude(latitude);
        pilot.setLastOnlineTime(LocalDateTime.now());
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWorkStatus(Long id, Integer workStatus) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setWorkStatus(workStatus);
        if (workStatus != 3) { // 非离线状态更新在线时间
            pilot.setLastOnlineTime(LocalDateTime.now());
        }
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean certify(Long id, Integer status, String reason) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        if (status != 3 && status != 4) {
            throw new BusinessException("认证状态参数错误");
        }

        pilot.setCertificationStatus(status);
        pilot.setCertificationTime(LocalDateTime.now());
        if (status == 4 && StrUtil.isNotBlank(reason)) {
            pilot.setCertificationFailReason(reason);
        }
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setStatus(status);
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    public List<PilotVO> findNearbyPilots(BigDecimal longitude, BigDecimal latitude, Integer radius) {
        // 简化实现，实际应该使用地理位置计算
        LambdaQueryWrapper<Pilot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pilot::getWorkStatus, 1) // 空闲状态
               .eq(Pilot::getCertificationStatus, 3) // 已认证
               .eq(Pilot::getStatus, 1) // 正常状态
               .isNotNull(Pilot::getLongitude)
               .isNotNull(Pilot::getLatitude);

        List<Pilot> pilots = pilotMapper.selectList(wrapper);
        
        // 过滤距离范围内的飞手（简化计算）
        return pilots.stream()
                .filter(pilot -> calculateDistance(longitude, latitude, 
                        pilot.getLongitude(), pilot.getLatitude()) <= radius)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRating(Long id, BigDecimal rating) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setRating(rating);
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementCompletedOrders(Long id) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setCompletedOrders(pilot.getCompletedOrders() + 1);
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addIncome(Long id, BigDecimal amount) {
        Pilot pilot = pilotMapper.selectById(id);
        if (pilot == null) {
            throw new BusinessException("飞手不存在");
        }

        pilot.setTotalIncome(pilot.getTotalIncome().add(amount));
        pilot.setUpdateTime(LocalDateTime.now());

        return pilotMapper.updateById(pilot) > 0;
    }

    /**
     * 转换为VO对象
     */
    private PilotVO convertToVO(Pilot pilot) {
        PilotVO vo = new PilotVO();
        BeanUtil.copyProperties(pilot, vo);
        
        // 身份证号脱敏
        if (StrUtil.isNotBlank(pilot.getIdCard())) {
            vo.setIdCard(pilot.getIdCard().replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2"));
        }
        
        // 设置描述字段
        vo.setGenderDesc(getGenderDesc(pilot.getGender()));
        vo.setLicenseTypeDesc(getLicenseTypeDesc(pilot.getLicenseType()));
        vo.setCertificationStatusDesc(getCertificationStatusDesc(pilot.getCertificationStatus()));
        vo.setWorkStatusDesc(getWorkStatusDesc(pilot.getWorkStatus()));
        vo.setStatusDesc(getStatusDesc(pilot.getStatus()));
        
        return vo;
    }

    /**
     * 获取性别描述
     */
    private String getGenderDesc(Integer gender) {
        if (gender == null) return "";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 获取执照类型描述
     */
    private String getLicenseTypeDesc(Integer licenseType) {
        if (licenseType == null) return "";
        switch (licenseType) {
            case 1: return "民用无人机驾驶员执照";
            case 2: return "商用无人机驾驶员执照";
            default: return "未知";
        }
    }

    /**
     * 获取认证状态描述
     */
    private String getCertificationStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "待认证";
            case 2: return "认证中";
            case 3: return "已认证";
            case 4: return "认证失败";
            default: return "未知";
        }
    }

    /**
     * 获取工作状态描述
     */
    private String getWorkStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "空闲";
            case 2: return "忙碌";
            case 3: return "离线";
            default: return "未知";
        }
    }

    /**
     * 获取账户状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "正常";
            case 2: return "冻结";
            case 3: return "禁用";
            default: return "未知";
        }
    }

    /**
     * 计算两点间距离（简化实现，实际应使用更精确的地理计算）
     */
    private double calculateDistance(BigDecimal lon1, BigDecimal lat1, BigDecimal lon2, BigDecimal lat2) {
        double deltaLon = Math.abs(lon1.doubleValue() - lon2.doubleValue());
        double deltaLat = Math.abs(lat1.doubleValue() - lat2.doubleValue());
        // 简化计算，1度约等于111km
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat) * 111;
    }
}