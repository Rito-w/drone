package com.drone.delivery.pilot.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞手注册DTO
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Data
public class PilotRegisterDTO {

    /**
     * 飞手姓名
     */
    @NotBlank(message = "飞手姓名不能为空")
    @Size(max = 50, message = "飞手姓名长度不能超过50字符")
    private String name;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
             message = "身份证号格式错误")
    private String idCard;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    private String email;

    /**
     * 性别：1-男，2-女
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值错误")
    @Max(value = 2, message = "性别值错误")
    private Integer gender;

    /**
     * 年龄
     */
    @NotNull(message = "年龄不能为空")
    @Min(value = 18, message = "年龄不能小于18岁")
    @Max(value = 65, message = "年龄不能大于65岁")
    private Integer age;

    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址长度不能超过200字符")
    private String address;

    /**
     * 紧急联系人
     */
    @NotBlank(message = "紧急联系人不能为空")
    @Size(max = 50, message = "紧急联系人姓名长度不能超过50字符")
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @NotBlank(message = "紧急联系人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系人电话格式错误")
    private String emergencyPhone;

    /**
     * 飞行执照号
     */
    @NotBlank(message = "飞行执照号不能为空")
    @Size(max = 50, message = "飞行执照号长度不能超过50字符")
    private String licenseNo;

    /**
     * 执照类型：1-民用无人机驾驶员执照，2-商用无人机驾驶员执照
     */
    @NotNull(message = "执照类型不能为空")
    @Min(value = 1, message = "执照类型值错误")
    @Max(value = 2, message = "执照类型值错误")
    private Integer licenseType;

    /**
     * 执照颁发日期
     */
    @NotNull(message = "执照颁发日期不能为空")
    private LocalDateTime licenseIssueDate;

    /**
     * 执照到期日期
     */
    @NotNull(message = "执照到期日期不能为空")
    private LocalDateTime licenseExpireDate;

    /**
     * 执照照片
     */
    @NotBlank(message = "执照照片不能为空")
    private String licensePhoto;

    /**
     * 飞行经验（小时）
     */
    @NotNull(message = "飞行经验不能为空")
    @Min(value = 0, message = "飞行经验不能小于0小时")
    private Integer flightHours;

    /**
     * 服务范围（km）
     */
    @NotNull(message = "服务范围不能为空")
    @Min(value = 1, message = "服务范围不能小于1公里")
    @Max(value = 50, message = "服务范围不能大于50公里")
    private Integer serviceRange;
}