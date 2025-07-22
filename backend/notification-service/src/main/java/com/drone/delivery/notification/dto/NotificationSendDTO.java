package com.drone.delivery.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 发送通知请求DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
@Schema(description = "发送通知请求DTO")
public class NotificationSendDTO {

    @Schema(description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    @Schema(description = "通知内容")
    @NotBlank(message = "通知内容不能为空")
    private String content;

    @Schema(description = "通知类型：1-系统通知，2-订单通知，3-支付通知，4-配送通知，5-营销通知")
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    @Schema(description = "通知级别：1-普通，2-重要，3-紧急")
    private Integer level = 1;

    @Schema(description = "发送方式：1-站内信，2-短信，3-邮件，4-推送，5-微信，6-语音")
    @NotNull(message = "发送方式不能为空")
    private Integer sendType;

    @Schema(description = "接收用户ID")
    @NotNull(message = "接收用户ID不能为空")
    private Long receiverId;

    @Schema(description = "接收用户类型：1-普通用户，2-飞手，3-管理员")
    @NotNull(message = "接收用户类型不能为空")
    private Integer receiverType;

    @Schema(description = "接收地址（手机号、邮箱、设备ID等）")
    private String receiverAddress;

    @Schema(description = "业务ID（订单ID、支付ID等）")
    private String businessId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板参数")
    private Map<String, Object> templateParams;

    @Schema(description = "最大重试次数")
    private Integer maxRetryCount = 3;

    @Schema(description = "扩展数据")
    private Map<String, Object> extraData;

    @Schema(description = "备注")
    private String remark;
}