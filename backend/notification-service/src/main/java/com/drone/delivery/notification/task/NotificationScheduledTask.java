package com.drone.delivery.notification.task;

import com.drone.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知定时任务
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduledTask {

    private final NotificationService notificationService;

    /**
     * 处理重试任务
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void processRetryTasks() {
        try {
            notificationService.processRetryTasks();
        } catch (Exception e) {
            log.error("处理重试任务异常", e);
        }
    }

    /**
     * 清理过期通知
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredNotifications() {
        try {
            Integer count = notificationService.cleanExpiredNotifications(30);
            log.info("清理过期通知完成，清理数量: {}", count);
        } catch (Exception e) {
            log.error("清理过期通知异常", e);
        }
    }
}