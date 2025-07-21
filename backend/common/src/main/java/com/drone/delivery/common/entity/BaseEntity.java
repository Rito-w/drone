package com.drone.delivery.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体类的公共字段，如ID、创建时间、更新时间等
 * 
 * @author Drone Delivery Team
 */
@Data
public class BaseEntity {
    
    /**
     * 主键ID
     * 使用雪花算法生成唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 创建时间
     * 插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID
     * 插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    /**
     * 更新人ID
     * 插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    /**
     * 逻辑删除标志
     * 0: 未删除, 1: 已删除
     */
    @TableField("is_deleted")
    private Integer deleted;
    
    /**
     * 版本号
     * 用于乐观锁控制
     */
    @TableField("version")
    private Integer version;
}