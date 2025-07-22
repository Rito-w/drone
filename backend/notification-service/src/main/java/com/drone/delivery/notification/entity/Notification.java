package com.drone.delivery.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知记录实体类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
@TableName("notification")
public class Notification {

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 通知类型：1-系统通知，2-订单通知，3-支付通知，4-配送通知，5-营销通知
     */
    @TableField("type")
    private Integer type;

    /**
     * 通知级别：1-普通，2-重要，3-紧急
     */
    @TableField("level")
    private Integer level;

    /**
     * 发送方式：1-站内信，2-短信，3-邮件，4-推送，5-微信，6-语音
     */
    @TableField("send_type")
    private Integer sendType;

    /**
     * 接收用户ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 接收用户类型：1-普通用户，2-飞手，3-管理员
     */
    @TableField("receiver_type")
    private Integer receiverType;

    /**
     * 接收地址（手机号、邮箱、设备ID等）
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 发送状态：1-待发送，2-发送中，3-发送成功，4-发送失败
     */
    @TableField("send_status")
    private Integer sendStatus;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @TableField("read_status")
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 失败原因
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    /**
     * 业务ID（订单ID、支付ID等）
     */
    @TableField("business_id")
    private String businessId;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 模板参数（JSON格式）
     */
    @TableField("template_params")
    private String templateParams;

    /**
     * 第三方消息ID
     */
    @TableField("third_party_msg_id")
    private String thirdPartyMsgId;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}