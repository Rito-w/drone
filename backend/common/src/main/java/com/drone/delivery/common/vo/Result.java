package com.drone.delivery.common.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果封装类
 * 用于封装所有API接口的返回结果，提供统一的响应格式
 * 
 * @param <T> 响应数据的类型
 * @author Drone Delivery Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端错误
     * 500: 服务器错误
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求时间戳
     */
    private Long timestamp;
    
    /**
     * 构造成功响应结果
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }
    
    /**
     * 构造成功响应结果（无数据）
     * 
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 构造成功响应结果（自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }
    
    /**
     * 构造失败响应结果
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }
    
    /**
     * 构造失败响应结果（默认500错误码）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
    
    /**
     * 构造客户端错误响应结果（400错误码）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 客户端错误响应结果
     */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }
    
    /**
     * 判断响应是否成功
     * 
     * @return true: 成功, false: 失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}