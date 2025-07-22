package com.drone.delivery.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 通知查询DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
@Schema(description = "通知查询DTO")
public class NotificationQueryDTO {

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知类型：1-系统通知，2-订单通知，3-支付通知，4-配送通知，5-营销通知")
    private Integer type;

    @Schema(description = "通知级别：1-普通，2-重要，3-紧急")
    private Integer level;

    @Schema(description = "发送方式：1-站内信，2-短信，3-邮件，4-推送，5-微信，6-语音")
    private Integer sendType;

    @Schema(description = "接收用户ID")
    private Long receiverId;

    @Schema(description = "接收用户类型：1-普通用户，2-飞手，3-管理员")
    private Integer receiverType;

    @Schema(description = "发送状态：1-待发送，2-发送中，3-发送成功，4-发送失败")
    private Integer sendStatus;

    @Schema(description = "阅读状态：0-未读，1-已读")
    private Integer readStatus;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "当前页码")
    @NotNull(message = "当前页码不能为空")
    @Min(value = 1, message = "当前页码必须大于0")
    private Long current = 1L;

    @Schema(description = "每页大小")
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Long size = 10L;
}