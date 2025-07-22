package com.drone.delivery.payment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.payment.dto.PaymentQueryDTO;
import com.drone.delivery.payment.dto.PaymentRequestDTO;
import com.drone.delivery.payment.dto.RefundRequestDTO;
import com.drone.delivery.payment.vo.PaymentResponseVO;
import com.drone.delivery.payment.vo.PaymentVO;

/**
 * 支付服务接口
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
public interface PaymentService {

    /**
     * 创建支付订单
     * 
     * @param requestDTO 支付请求参数
     * @return 支付响应信息
     */
    PaymentResponseVO createPayment(PaymentRequestDTO requestDTO);

    /**
     * 分页查询支付记录
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PaymentVO> page(PaymentQueryDTO queryDTO);

    /**
     * 根据ID查询支付详情
     * 
     * @param id 支付ID
     * @return 支付详情
     */
    PaymentVO getById(Long id);

    /**
     * 根据支付流水号查询支付详情
     * 
     * @param paymentNo 支付流水号
     * @return 支付详情
     */
    PaymentVO getByPaymentNo(String paymentNo);

    /**
     * 根据订单号查询支付详情
     * 
     * @param orderNo 订单号
     * @return 支付详情
     */
    PaymentVO getByOrderNo(String orderNo);

    /**
     * 查询支付状态
     * 
     * @param paymentNo 支付流水号
     * @return 支付状态
     */
    Integer queryPaymentStatus(String paymentNo);

    /**
     * 支付成功回调处理
     * 
     * @param paymentNo 支付流水号
     * @param thirdPartyTransactionId 第三方交易号
     * @return 是否处理成功
     */
    Boolean paymentSuccess(String paymentNo, String thirdPartyTransactionId);

    /**
     * 支付失败回调处理
     * 
     * @param paymentNo 支付流水号
     * @param failReason 失败原因
     * @return 是否处理成功
     */
    Boolean paymentFailed(String paymentNo, String failReason);

    /**
     * 申请退款
     * 
     * @param requestDTO 退款请求参数
     * @return 是否申请成功
     */
    Boolean refund(RefundRequestDTO requestDTO);

    /**
     * 退款成功回调处理
     * 
     * @param paymentNo 支付流水号
     * @return 是否处理成功
     */
    Boolean refundSuccess(String paymentNo);

    /**
     * 取消支付
     * 
     * @param paymentNo 支付流水号
     * @return 是否取消成功
     */
    Boolean cancelPayment(String paymentNo);

    /**
     * 同步第三方支付状态
     * 
     * @param paymentNo 支付流水号
     * @return 是否同步成功
     */
    Boolean syncPaymentStatus(String paymentNo);

    /**
     * 重新发送支付通知
     * 
     * @param paymentNo 支付流水号
     * @return 是否发送成功
     */
    Boolean resendNotification(String paymentNo);
}