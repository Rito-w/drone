package com.drone.delivery.notification.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.notification.dto.NotificationBatchSendDTO;
import com.drone.delivery.notification.dto.NotificationQueryDTO;
import com.drone.delivery.notification.dto.NotificationSendDTO;
import com.drone.delivery.notification.entity.Notification;
import com.drone.delivery.notification.mapper.NotificationMapper;
import com.drone.delivery.notification.service.NotificationService;
import com.drone.delivery.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    private static final String NOTIFICATION_QUEUE = "notification.send";
    private static final String NOTIFICATION_RETRY_QUEUE = "notification.retry";
    private static final String UNREAD_COUNT_KEY = "notification:unread:count:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendNotification(NotificationSendDTO sendDTO) {
        // 创建通知记录
        Notification notification = new Notification();
        BeanUtil.copyProperties(sendDTO, notification);
        
        // 设置默认值
        notification.setSendStatus(1); // 待发送
        notification.setReadStatus(0); // 未读
        notification.setRetryCount(0);
        notification.setMaxRetryCount(sendDTO.getMaxRetryCount());
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        
        // 处理模板参数和扩展数据
        if (sendDTO.getTemplateParams() != null) {
            notification.setTemplateParams(JSON.toJSONString(sendDTO.getTemplateParams()));
        }
        if (sendDTO.getExtraData() != null) {
            notification.setExtraData(JSON.toJSONString(sendDTO.getExtraData()));
        }

        notificationMapper.insert(notification);

        // 发送到消息队列进行异步处理
        rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification.getId());

        // 更新未读计数
        updateUnreadCount(notification.getReceiverId(), notification.getReceiverType(), 1);

        log.info("创建通知成功，通知ID: {}, 接收用户: {}", notification.getId(), notification.getReceiverId());
        return notification.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchSendNotification(NotificationBatchSendDTO batchSendDTO) {
        List<Long> notificationIds = new ArrayList<>();
        
        for (Long receiverId : batchSendDTO.getReceiverIds()) {
            NotificationSendDTO sendDTO = new NotificationSendDTO();
            BeanUtil.copyProperties(batchSendDTO, sendDTO);
            sendDTO.setReceiverId(receiverId);
            
            Long notificationId = sendNotification(sendDTO);
            notificationIds.add(notificationId);
        }
        
        log.info("批量发送通知成功，通知数量: {}", notificationIds.size());
        return notificationIds;
    }

    @Override
    public IPage<NotificationVO> page(NotificationQueryDTO queryDTO) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        wrapper.like(StrUtil.isNotBlank(queryDTO.getTitle()), Notification::getTitle, queryDTO.getTitle())
               .eq(queryDTO.getType() != null, Notification::getType, queryDTO.getType())
               .eq(queryDTO.getLevel() != null, Notification::getLevel, queryDTO.getLevel())
               .eq(queryDTO.getSendType() != null, Notification::getSendType, queryDTO.getSendType())
               .eq(queryDTO.getReceiverId() != null, Notification::getReceiverId, queryDTO.getReceiverId())
               .eq(queryDTO.getReceiverType() != null, Notification::getReceiverType, queryDTO.getReceiverType())
               .eq(queryDTO.getSendStatus() != null, Notification::getSendStatus, queryDTO.getSendStatus())
               .eq(queryDTO.getReadStatus() != null, Notification::getReadStatus, queryDTO.getReadStatus())
               .eq(StrUtil.isNotBlank(queryDTO.getBusinessId()), Notification::getBusinessId, queryDTO.getBusinessId())
               .eq(StrUtil.isNotBlank(queryDTO.getBusinessType()), Notification::getBusinessType, queryDTO.getBusinessType())
               .ge(queryDTO.getStartTime() != null, Notification::getCreateTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, Notification::getCreateTime, queryDTO.getEndTime())
               .orderByDesc(Notification::getCreateTime);

        Page<Notification> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<Notification> notificationPage = notificationMapper.selectPage(page, wrapper);

        // 转换为VO
        IPage<NotificationVO> voPage = new Page<>();
        BeanUtil.copyProperties(notificationPage, voPage);
        voPage.setRecords(notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public NotificationVO getById(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        return convertToVO(notification);
    }

    @Override
    public Long getUnreadCount(Long receiverId, Integer receiverType) {
        String key = UNREAD_COUNT_KEY + receiverType + ":" + receiverId;
        Object count = redisTemplate.opsForValue().get(key);
        if (count != null) {
            return Long.valueOf(count.toString());
        }
        
        // 从数据库查询并缓存
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
               .eq(Notification::getReceiverType, receiverType)
               .eq(Notification::getReadStatus, 0)
               .eq(Notification::getSendStatus, 3); // 发送成功的通知
        
        Long unreadCount = notificationMapper.selectCount(wrapper);
        redisTemplate.opsForValue().set(key, unreadCount, 1, TimeUnit.HOURS);
        
        return unreadCount;
    }

    @Override
    public List<NotificationVO> getUnreadList(Long receiverId, Integer receiverType, Integer limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
               .eq(Notification::getReceiverType, receiverType)
               .eq(Notification::getReadStatus, 0)
               .eq(Notification::getSendStatus, 3) // 发送成功的通知
               .orderByDesc(Notification::getCreateTime)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        List<Notification> notifications = notificationMapper.selectList(wrapper);
        return notifications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        if (notification.getReadStatus() == 1) {
            return true; // 已经是已读状态
        }
        
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .set(Notification::getReadStatus, 1)
               .set(Notification::getReadTime, LocalDateTime.now())
               .set(Notification::getUpdateTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        if (result > 0) {
            // 更新未读计数
            updateUnreadCount(notification.getReceiverId(), notification.getReceiverType(), -1);
            log.info("标记通知为已读成功，通知ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchMarkAsRead(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        
        // 查询通知信息
        List<Notification> notifications = notificationMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(notifications)) {
            return true;
        }
        
        // 过滤未读通知
        List<Long> unreadIds = notifications.stream()
                .filter(n -> n.getReadStatus() == 0)
                .map(Notification::getId)
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(unreadIds)) {
            return true;
        }
        
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Notification::getId, unreadIds)
               .set(Notification::getReadStatus, 1)
               .set(Notification::getReadTime, LocalDateTime.now())
               .set(Notification::getUpdateTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        if (result > 0) {
            // 按用户分组更新未读计数
            notifications.stream()
                    .filter(n -> unreadIds.contains(n.getId()))
                    .collect(Collectors.groupingBy(n -> n.getReceiverType() + ":" + n.getReceiverId()))
                    .forEach((key, list) -> {
                        String[] parts = key.split(":");
                        Integer receiverType = Integer.valueOf(parts[0]);
                        Long receiverId = Long.valueOf(parts[1]);
                        updateUnreadCount(receiverId, receiverType, -list.size());
                    });
            
            log.info("批量标记通知为已读成功，数量: {}", result);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAllAsRead(Long receiverId, Integer receiverType) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
               .eq(Notification::getReceiverType, receiverType)
               .eq(Notification::getReadStatus, 0)
               .set(Notification::getReadStatus, 1)
               .set(Notification::getReadTime, LocalDateTime.now())
               .set(Notification::getUpdateTime, LocalDateTime.now());
        
        int result = notificationMapper.update(null, wrapper);
        if (result > 0) {
            // 清空未读计数
            String key = UNREAD_COUNT_KEY + receiverType + ":" + receiverId;
            redisTemplate.delete(key);
            log.info("标记用户所有通知为已读成功，用户: {}, 数量: {}", receiverId, result);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteNotification(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return true;
        }
        
        int result = notificationMapper.deleteById(id);
        if (result > 0) {
            // 如果是未读通知，更新未读计数
            if (notification.getReadStatus() == 0) {
                updateUnreadCount(notification.getReceiverId(), notification.getReceiverType(), -1);
            }
            log.info("删除通知成功，通知ID: {}", id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteNotification(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        
        // 查询通知信息
        List<Notification> notifications = notificationMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(notifications)) {
            return true;
        }
        
        int result = notificationMapper.deleteBatchIds(ids);
        if (result > 0) {
            // 按用户分组更新未读计数
            notifications.stream()
                    .filter(n -> n.getReadStatus() == 0)
                    .collect(Collectors.groupingBy(n -> n.getReceiverType() + ":" + n.getReceiverId()))
                    .forEach((key, list) -> {
                        String[] parts = key.split(":");
                        Integer receiverType = Integer.valueOf(parts[0]);
                        Long receiverId = Long.valueOf(parts[1]);
                        updateUnreadCount(receiverId, receiverType, -list.size());
                    });
            
            log.info("批量删除通知成功，数量: {}", result);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resendNotification(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        if (notification.getSendStatus() == 3) {
            throw new BusinessException("通知已发送成功，无需重发");
        }
        
        // 重置发送状态
        notification.setSendStatus(1); // 待发送
        notification.setRetryCount(0);
        notification.setFailReason(null);
        notification.setNextRetryTime(null);
        notification.setUpdateTime(LocalDateTime.now());
        
        notificationMapper.updateById(notification);
        
        // 发送到消息队列
        rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification.getId());
        
        log.info("重新发送通知，通知ID: {}", id);
        return true;
    }

    @Override
    public void processNotificationTask(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            log.warn("通知不存在，通知ID: {}", notificationId);
            return;
        }
        
        if (notification.getSendStatus() != 1) {
            log.warn("通知状态不是待发送，通知ID: {}, 状态: {}", notificationId, notification.getSendStatus());
            return;
        }
        
        try {
            // 更新为发送中
            updateNotificationStatus(notificationId, 2, null, null);
            
            // 根据发送方式处理
            boolean success = false;
            switch (notification.getSendType()) {
                case 1: // 站内信
                    success = sendInAppMessage(notification);
                    break;
                case 2: // 短信
                    success = sendSms(notification);
                    break;
                case 3: // 邮件
                    success = sendEmail(notification);
                    break;
                case 4: // 推送
                    success = sendPush(notification);
                    break;
                case 5: // 微信
                    success = sendWechat(notification);
                    break;
                case 6: // 语音
                    success = sendVoice(notification);
                    break;
                default:
                    log.warn("不支持的发送方式: {}", notification.getSendType());
                    success = false;
            }
            
            if (success) {
                // 发送成功
                updateNotificationStatus(notificationId, 3, LocalDateTime.now(), null);
                log.info("通知发送成功，通知ID: {}", notificationId);
            } else {
                // 发送失败，安排重试
                scheduleRetry(notification);
            }
            
        } catch (Exception e) {
            log.error("处理通知发送任务异常，通知ID: {}", notificationId, e);
            scheduleRetry(notification);
        }
    }

    @Override
    public void processRetryTasks() {
        // 查询需要重试的通知
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getSendStatus, 4) // 发送失败
               .le(Notification::getNextRetryTime, LocalDateTime.now())
               .lt(Notification::getRetryCount, Notification::getMaxRetryCount);
        
        List<Notification> retryNotifications = notificationMapper.selectList(wrapper);
        
        for (Notification notification : retryNotifications) {
            // 发送到重试队列
            rabbitTemplate.convertAndSend(NOTIFICATION_RETRY_QUEUE, notification.getId());
        }
        
        if (!retryNotifications.isEmpty()) {
            log.info("处理重试任务，数量: {}", retryNotifications.size());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer cleanExpiredNotifications(Integer days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Notification::getCreateTime, expireTime);
        
        int count = notificationMapper.delete(wrapper);
        log.info("清理过期通知完成，清理数量: {}, 保留天数: {}", count, days);
        
        return count;
    }

    /**
     * 更新通知状态
     */
    private void updateNotificationStatus(Long id, Integer status, LocalDateTime sendTime, String failReason) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
               .set(Notification::getSendStatus, status)
               .set(Notification::getUpdateTime, LocalDateTime.now());
        
        if (sendTime != null) {
            wrapper.set(Notification::getSendTime, sendTime);
        }
        if (StrUtil.isNotBlank(failReason)) {
            wrapper.set(Notification::getFailReason, failReason);
        }
        
        notificationMapper.update(null, wrapper);
    }

    /**
     * 安排重试
     */
    private void scheduleRetry(Notification notification) {
        int retryCount = notification.getRetryCount() + 1;
        
        if (retryCount >= notification.getMaxRetryCount()) {
            // 超过最大重试次数，标记为最终失败
            updateNotificationStatus(notification.getId(), 4, null, "超过最大重试次数");
            log.warn("通知发送最终失败，通知ID: {}", notification.getId());
        } else {
            // 计算下次重试时间（指数退避）
            long delayMinutes = (long) Math.pow(2, retryCount - 1) * 5; // 5分钟、10分钟、20分钟...
            LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(delayMinutes);
            
            LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Notification::getId, notification.getId())
                   .set(Notification::getSendStatus, 4) // 发送失败
                   .set(Notification::getRetryCount, retryCount)
                   .set(Notification::getNextRetryTime, nextRetryTime)
                   .set(Notification::getUpdateTime, LocalDateTime.now());
            
            notificationMapper.update(null, wrapper);
            log.info("安排通知重试，通知ID: {}, 重试次数: {}, 下次重试时间: {}", 
                    notification.getId(), retryCount, nextRetryTime);
        }
    }

    /**
     * 发送站内信
     */
    private boolean sendInAppMessage(Notification notification) {
        // 站内信直接标记为成功（已存储在数据库）
        return true;
    }

    /**
     * 发送短信
     */
    private boolean sendSms(Notification notification) {
        // TODO: 集成短信服务商API
        log.info("发送短信通知，接收地址: {}, 内容: {}", notification.getReceiverAddress(), notification.getContent());
        return true; // 模拟发送成功
    }

    /**
     * 发送邮件
     */
    private boolean sendEmail(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getReceiverAddress());
            message.setSubject(notification.getTitle());
            message.setText(notification.getContent());
            
            mailSender.send(message);
            log.info("发送邮件通知成功，接收地址: {}", notification.getReceiverAddress());
            return true;
        } catch (Exception e) {
            log.error("发送邮件通知失败，接收地址: {}", notification.getReceiverAddress(), e);
            return false;
        }
    }

    /**
     * 发送推送
     */
    private boolean sendPush(Notification notification) {
        // TODO: 集成推送服务
        log.info("发送推送通知，设备ID: {}, 内容: {}", notification.getReceiverAddress(), notification.getContent());
        return true; // 模拟发送成功
    }

    /**
     * 发送微信通知
     */
    private boolean sendWechat(Notification notification) {
        // TODO: 集成微信服务号模板消息
        log.info("发送微信通知，OpenID: {}, 内容: {}", notification.getReceiverAddress(), notification.getContent());
        return true; // 模拟发送成功
    }

    /**
     * 发送语音通知
     */
    private boolean sendVoice(Notification notification) {
        // TODO: 集成语音通话服务
        log.info("发送语音通知，手机号: {}, 内容: {}", notification.getReceiverAddress(), notification.getContent());
        return true; // 模拟发送成功
    }

    /**
     * 更新未读计数
     */
    private void updateUnreadCount(Long receiverId, Integer receiverType, int delta) {
        String key = UNREAD_COUNT_KEY + receiverType + ":" + receiverId;
        redisTemplate.opsForValue().increment(key, delta);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    /**
     * 转换为VO对象
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtil.copyProperties(notification, vo);
        
        // 设置描述字段
        vo.setTypeDesc(getTypeDesc(notification.getType()));
        vo.setLevelDesc(getLevelDesc(notification.getLevel()));
        vo.setSendTypeDesc(getSendTypeDesc(notification.getSendType()));
        vo.setReceiverTypeDesc(getReceiverTypeDesc(notification.getReceiverType()));
        vo.setSendStatusDesc(getSendStatusDesc(notification.getSendStatus()));
        vo.setReadStatusDesc(getReadStatusDesc(notification.getReadStatus()));
        
        return vo;
    }

    /**
     * 获取通知类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "系统通知";
            case 2: return "订单通知";
            case 3: return "支付通知";
            case 4: return "配送通知";
            case 5: return "营销通知";
            default: return "未知";
        }
    }

    /**
     * 获取通知级别描述
     */
    private String getLevelDesc(Integer level) {
        if (level == null) return "";
        switch (level) {
            case 1: return "普通";
            case 2: return "重要";
            case 3: return "紧急";
            default: return "未知";
        }
    }

    /**
     * 获取发送方式描述
     */
    private String getSendTypeDesc(Integer sendType) {
        if (sendType == null) return "";
        switch (sendType) {
            case 1: return "站内信";
            case 2: return "短信";
            case 3: return "邮件";
            case 4: return "推送";
            case 5: return "微信";
            case 6: return "语音";
            default: return "未知";
        }
    }

    /**
     * 获取接收用户类型描述
     */
    private String getReceiverTypeDesc(Integer receiverType) {
        if (receiverType == null) return "";
        switch (receiverType) {
            case 1: return "普通用户";
            case 2: return "飞手";
            case 3: return "管理员";
            default: return "未知";
        }
    }

    /**
     * 获取发送状态描述
     */
    private String getSendStatusDesc(Integer sendStatus) {
        if (sendStatus == null) return "";
        switch (sendStatus) {
            case 1: return "待发送";
            case 2: return "发送中";
            case 3: return "发送成功";
            case 4: return "发送失败";
            default: return "未知";
        }
    }

    /**
     * 获取阅读状态描述
     */
    private String getReadStatusDesc(Integer readStatus) {
        if (readStatus == null) return "";
        switch (readStatus) {
            case 0: return "未读";
            case 1: return "已读";
            default: return "未知";
        }
    }
}