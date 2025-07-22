package com.drone.delivery.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.common.result.Result;
import com.drone.delivery.notification.dto.NotificationBatchSendDTO;
import com.drone.delivery.notification.dto.NotificationQueryDTO;
import com.drone.delivery.notification.dto.NotificationSendDTO;
import com.drone.delivery.notification.service.NotificationService;
import com.drone.delivery.notification.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 通知控制器
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Validated
@Tag(name = "通知管理", description = "通知发送、查询、状态管理等接口")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送单个通知")
    public Result<Long> sendNotification(@Valid @RequestBody NotificationSendDTO sendDTO) {
        Long notificationId = notificationService.sendNotification(sendDTO);
        return Result.success(notificationId);
    }

    @PostMapping("/batch-send")
    @Operation(summary = "批量发送通知", description = "批量发送通知给多个用户")
    public Result<List<Long>> batchSendNotification(@Valid @RequestBody NotificationBatchSendDTO batchSendDTO) {
        List<Long> notificationIds = notificationService.batchSendNotification(batchSendDTO);
        return Result.success(notificationIds);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询通知", description = "根据条件分页查询通知列表")
    public Result<IPage<NotificationVO>> page(@Valid @RequestBody NotificationQueryDTO queryDTO) {
        IPage<NotificationVO> page = notificationService.page(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询通知详情", description = "根据ID查询通知详情")
    public Result<NotificationVO> getById(@Parameter(description = "通知ID") @PathVariable Long id) {
        NotificationVO notification = notificationService.getById(id);
        return Result.success(notification);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "查询未读通知数量", description = "查询指定用户的未读通知数量")
    public Result<Long> getUnreadCount(
            @Parameter(description = "接收用户ID") @RequestParam @NotNull Long receiverId,
            @Parameter(description = "接收用户类型") @RequestParam @NotNull Integer receiverType) {
        Long count = notificationService.getUnreadCount(receiverId, receiverType);
        return Result.success(count);
    }

    @GetMapping("/unread/list")
    @Operation(summary = "查询未读通知列表", description = "查询指定用户的未读通知列表")
    public Result<List<NotificationVO>> getUnreadList(
            @Parameter(description = "接收用户ID") @RequestParam @NotNull Long receiverId,
            @Parameter(description = "接收用户类型") @RequestParam @NotNull Integer receiverType,
            @Parameter(description = "限制数量") @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<NotificationVO> notifications = notificationService.getUnreadList(receiverId, receiverType, limit);
        return Result.success(notifications);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读状态")
    public Result<Boolean> markAsRead(@Parameter(description = "通知ID") @PathVariable Long id) {
        Boolean result = notificationService.markAsRead(id);
        return Result.success(result);
    }

    @PutMapping("/batch-read")
    @Operation(summary = "批量标记为已读", description = "批量标记多个通知为已读状态")
    public Result<Boolean> batchMarkAsRead(@RequestBody List<Long> ids) {
        Boolean result = notificationService.batchMarkAsRead(ids);
        return Result.success(result);
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记全部为已读", description = "标记指定用户的所有通知为已读状态")
    public Result<Boolean> markAllAsRead(
            @Parameter(description = "接收用户ID") @RequestParam @NotNull Long receiverId,
            @Parameter(description = "接收用户类型") @RequestParam @NotNull Integer receiverType) {
        Boolean result = notificationService.markAllAsRead(receiverId, receiverType);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<Boolean> deleteNotification(@Parameter(description = "通知ID") @PathVariable Long id) {
        Boolean result = notificationService.deleteNotification(id);
        return Result.success(result);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知", description = "批量删除多个通知")
    public Result<Boolean> batchDeleteNotification(@RequestBody List<Long> ids) {
        Boolean result = notificationService.batchDeleteNotification(ids);
        return Result.success(result);
    }

    @PostMapping("/{id}/resend")
    @Operation(summary = "重新发送通知", description = "重新发送失败的通知")
    public Result<Boolean> resendNotification(@Parameter(description = "通知ID") @PathVariable Long id) {
        Boolean result = notificationService.resendNotification(id);
        return Result.success(result);
    }

    @PostMapping("/clean-expired")
    @Operation(summary = "清理过期通知", description = "清理指定天数之前的过期通知")
    public Result<Integer> cleanExpiredNotifications(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") Integer days) {
        Integer count = notificationService.cleanExpiredNotifications(days);
        return Result.success(count);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查通知服务健康状态")
    public Result<String> health() {
        return Result.success("Notification Service is running");
    }
}