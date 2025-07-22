package com.drone.delivery.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.notification.dto.NotificationBatchSendDTO;
import com.drone.delivery.notification.dto.NotificationQueryDTO;
import com.drone.delivery.notification.dto.NotificationSendDTO;
import com.drone.delivery.notification.vo.NotificationVO;

import java.util.List;

/**
 * 通知服务接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
public interface NotificationService {

    /**
     * 发送通知
     * 
     * @param sendDTO 发送通知请求
     * @return 通知ID
     */
    Long sendNotification(NotificationSendDTO sendDTO);

    /**
     * 批量发送通知
     * 
     * @param batchSendDTO 批量发送通知请求
     * @return 通知ID列表
     */
    List<Long> batchSendNotification(NotificationBatchSendDTO batchSendDTO);

    /**
     * 分页查询通知
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<NotificationVO> page(NotificationQueryDTO queryDTO);

    /**
     * 根据ID查询通知详情
     * 
     * @param id 通知ID
     * @return 通知详情
     */
    NotificationVO getById(Long id);

    /**
     * 查询用户未读通知数量
     * 
     * @param receiverId 接收用户ID
     * @param receiverType 接收用户类型
     * @return 未读通知数量
     */
    Long getUnreadCount(Long receiverId, Integer receiverType);

    /**
     * 查询用户未读通知列表
     * 
     * @param receiverId 接收用户ID
     * @param receiverType 接收用户类型
     * @param limit 限制数量
     * @return 未读通知列表
     */
    List<NotificationVO> getUnreadList(Long receiverId, Integer receiverType, Integer limit);

    /**
     * 标记通知为已读
     * 
     * @param id 通知ID
     * @return 是否成功
     */
    Boolean markAsRead(Long id);

    /**
     * 批量标记通知为已读
     * 
     * @param ids 通知ID列表
     * @return 是否成功
     */
    Boolean batchMarkAsRead(List<Long> ids);

    /**
     * 标记用户所有通知为已读
     * 
     * @param receiverId 接收用户ID
     * @param receiverType 接收用户类型
     * @return 是否成功
     */
    Boolean markAllAsRead(Long receiverId, Integer receiverType);

    /**
     * 删除通知
     * 
     * @param id 通知ID
     * @return 是否成功
     */
    Boolean deleteNotification(Long id);

    /**
     * 批量删除通知
     * 
     * @param ids 通知ID列表
     * @return 是否成功
     */
    Boolean batchDeleteNotification(List<Long> ids);

    /**
     * 重新发送失败的通知
     * 
     * @param id 通知ID
     * @return 是否成功
     */
    Boolean resendNotification(Long id);

    /**
     * 处理通知发送任务
     * 
     * @param notificationId 通知ID
     */
    void processNotificationTask(Long notificationId);

    /**
     * 处理通知重试任务
     */
    void processRetryTasks();

    /**
     * 清理过期通知
     * 
     * @param days 保留天数
     * @return 清理数量
     */
    Integer cleanExpiredNotifications(Integer days);
}