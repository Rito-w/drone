package com.drone.delivery.notification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知视图对象
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
@Schema(description = "通知视图对象")
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知类型")
    private Integer type;

    @Schema(description = "通知类型描述")
    private String typeDesc;

    @Schema(description = "通知级别")
    private Integer level;

    @Schema(description = "通知级别描述")
    private String levelDesc;

    @Schema(description = "发送方式")
    private Integer sendType;

    @Schema(description = "发送方式描述")
    private String sendTypeDesc;

    @Schema(description = "接收用户ID")
    private Long receiverId;

    @Schema(description = "接收用户类型")
    private Integer receiverType;

    @Schema(description = "接收用户类型描述")
    private String receiverTypeDesc;

    @Schema(description = "接收地址")
    private String receiverAddress;

    @Schema(description = "发送状态")
    private Integer sendStatus;

    @Schema(description = "发送状态描述")
    private String sendStatusDesc;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "阅读状态")
    private Integer readStatus;

    @Schema(description = "阅读状态描述")
    private String readStatusDesc;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最大重试次数")
    private Integer maxRetryCount;

    @Schema(description = "下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "第三方消息ID")
    private String thirdPartyMsgId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}