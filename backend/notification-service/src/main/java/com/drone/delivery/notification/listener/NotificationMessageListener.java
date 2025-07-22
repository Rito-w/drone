package com.drone.delivery.notification.listener;

import com.drone.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知消息监听器
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    private final NotificationService notificationService;

    /**
     * 监听通知发送队列
     */
    @RabbitListener(queues = "notification.send")
    public void handleNotificationSend(Long notificationId) {
        try {
            log.info("接收到通知发送任务，通知ID: {}", notificationId);
            notificationService.processNotificationTask(notificationId);
        } catch (Exception e) {
            log.error("处理通知发送任务失败，通知ID: {}", notificationId, e);
            throw e; // 重新抛出异常，触发重试机制
        }
    }

    /**
     * 监听通知重试队列
     */
    @RabbitListener(queues = "notification.retry")
    public void handleNotificationRetry(Long notificationId) {
        try {
            log.info("接收到通知重试任务，通知ID: {}", notificationId);
            notificationService.processNotificationTask(notificationId);
        } catch (Exception e) {
            log.error("处理通知重试任务失败，通知ID: {}", notificationId, e);
            throw e; // 重新抛出异常，触发重试机制
        }
    }

    /**
     * 监听死信队列
     */
    @RabbitListener(queues = "notification.dlq")
    public void handleDeadLetter(Long notificationId) {
        log.error("通知进入死信队列，通知ID: {}", notificationId);
        // 可以在这里记录死信消息，进行人工处理
    }
}