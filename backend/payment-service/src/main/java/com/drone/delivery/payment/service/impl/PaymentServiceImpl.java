package com.drone.delivery.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.delivery.common.exception.BusinessException;
import com.drone.delivery.payment.dto.PaymentQueryDTO;
import com.drone.delivery.payment.dto.PaymentRequestDTO;
import com.drone.delivery.payment.dto.RefundRequestDTO;
import com.drone.delivery.payment.entity.Payment;
import com.drone.delivery.payment.mapper.PaymentMapper;
import com.drone.delivery.payment.service.PaymentService;
import com.drone.delivery.payment.vo.PaymentResponseVO;
import com.drone.delivery.payment.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponseVO createPayment(PaymentRequestDTO requestDTO) {
        // 检查订单是否已有支付记录
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderNo, requestDTO.getOrderNo())
               .in(Payment::getStatus, 1, 2, 3); // 待支付、支付中、支付成功
        Payment existPayment = paymentMapper.selectOne(wrapper);
        if (existPayment != null) {
            if (existPayment.getStatus() == 3) {
                throw new BusinessException("订单已支付成功");
            }
            if (existPayment.getStatus() == 1 || existPayment.getStatus() == 2) {
                // 返回已有的支付信息
                return buildPaymentResponse(existPayment);
            }
        }

        // 创建支付记录
        Payment payment = new Payment();
        BeanUtil.copyProperties(requestDTO, payment);
        payment.setPaymentNo(generatePaymentNo());
        payment.setStatus(1); // 待支付
        payment.setNotifyStatus(1); // 未通知
        payment.setNotifyCount(0);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        paymentMapper.insert(payment);

        // 调用第三方支付接口（模拟）
        PaymentResponseVO response = callThirdPartyPayment(payment);
        
        // 更新支付状态为支付中
        payment.setStatus(2);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        response.setPaymentId(payment.getId());
        response.setPaymentNo(payment.getPaymentNo());
        
        log.info("创建支付订单成功，支付流水号: {}", payment.getPaymentNo());
        return response;
    }

    @Override
    public IPage<PaymentVO> page(PaymentQueryDTO queryDTO) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        wrapper.like(StrUtil.isNotBlank(queryDTO.getPaymentNo()), Payment::getPaymentNo, queryDTO.getPaymentNo())
               .eq(queryDTO.getOrderId() != null, Payment::getOrderId, queryDTO.getOrderId())
               .like(StrUtil.isNotBlank(queryDTO.getOrderNo()), Payment::getOrderNo, queryDTO.getOrderNo())
               .eq(queryDTO.getUserId() != null, Payment::getUserId, queryDTO.getUserId())
               .eq(queryDTO.getPaymentMethod() != null, Payment::getPaymentMethod, queryDTO.getPaymentMethod())
               .eq(queryDTO.getStatus() != null, Payment::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getChannel() != null, Payment::getChannel, queryDTO.getChannel())
               .ge(queryDTO.getMinAmount() != null, Payment::getAmount, queryDTO.getMinAmount())
               .le(queryDTO.getMaxAmount() != null, Payment::getAmount, queryDTO.getMaxAmount())
               .ge(queryDTO.getStartTime() != null, Payment::getCreateTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, Payment::getCreateTime, queryDTO.getEndTime())
               .orderByDesc(Payment::getCreateTime);

        Page<Payment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<Payment> paymentPage = paymentMapper.selectPage(page, wrapper);

        // 转换为VO
        IPage<PaymentVO> voPage = new Page<>();
        BeanUtil.copyProperties(paymentPage, voPage);
        voPage.setRecords(paymentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public PaymentVO getById(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return convertToVO(payment);
    }

    @Override
    public PaymentVO getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return convertToVO(payment);
    }

    @Override
    public PaymentVO getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderNo, orderNo)
               .orderByDesc(Payment::getCreateTime)
               .last("LIMIT 1");
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return convertToVO(payment);
    }

    @Override
    public Integer queryPaymentStatus(String paymentNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return payment.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean paymentSuccess(String paymentNo, String thirdPartyTransactionId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (payment.getStatus() == 3) {
            log.warn("支付已成功，无需重复处理，支付流水号: {}", paymentNo);
            return true;
        }

        payment.setStatus(3); // 支付成功
        payment.setThirdPartyTransactionId(thirdPartyTransactionId);
        payment.setCompletedTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateById(payment);
        if (result > 0) {
            log.info("支付成功回调处理完成，支付流水号: {}", paymentNo);
            // TODO: 发送支付成功通知给订单服务
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean paymentFailed(String paymentNo, String failReason) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        payment.setStatus(4); // 支付失败
        payment.setFailReason(failReason);
        payment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateById(payment);
        if (result > 0) {
            log.info("支付失败回调处理完成，支付流水号: {}, 失败原因: {}", paymentNo, failReason);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refund(RefundRequestDTO requestDTO) {
        Payment payment = paymentMapper.selectById(requestDTO.getPaymentId());
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (payment.getStatus() != 3) {
            throw new BusinessException("只有支付成功的订单才能退款");
        }

        if (payment.getStatus() == 5) {
            throw new BusinessException("订单已退款");
        }

        if (requestDTO.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("退款金额不能大于支付金额");
        }

        // 调用第三方退款接口（模拟）
        boolean refundResult = callThirdPartyRefund(payment, requestDTO.getRefundAmount());
        if (!refundResult) {
            throw new BusinessException("退款申请失败");
        }

        payment.setStatus(5); // 已退款
        payment.setRefundAmount(requestDTO.getRefundAmount());
        payment.setRefundReason(requestDTO.getRefundReason());
        payment.setRefundTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateById(payment);
        if (result > 0) {
            log.info("退款申请成功，支付流水号: {}, 退款金额: {}", payment.getPaymentNo(), requestDTO.getRefundAmount());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refundSuccess(String paymentNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        payment.setStatus(5); // 已退款
        payment.setRefundTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateById(payment);
        if (result > 0) {
            log.info("退款成功回调处理完成，支付流水号: {}", paymentNo);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelPayment(String paymentNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (payment.getStatus() == 3) {
            throw new BusinessException("支付已成功，无法取消");
        }

        if (payment.getStatus() == 5) {
            throw new BusinessException("支付已退款，无法取消");
        }

        payment.setStatus(4); // 支付失败（取消）
        payment.setFailReason("用户取消支付");
        payment.setUpdateTime(LocalDateTime.now());

        int result = paymentMapper.updateById(payment);
        if (result > 0) {
            log.info("取消支付成功，支付流水号: {}", paymentNo);
            return true;
        }
        return false;
    }

    @Override
    public Boolean syncPaymentStatus(String paymentNo) {
        // 调用第三方接口查询支付状态（模拟）
        Integer thirdPartyStatus = queryThirdPartyPaymentStatus(paymentNo);
        
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        if (!payment.getStatus().equals(thirdPartyStatus)) {
            payment.setStatus(thirdPartyStatus);
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            log.info("同步支付状态成功，支付流水号: {}, 状态: {}", paymentNo, thirdPartyStatus);
        }

        return true;
    }

    @Override
    public Boolean resendNotification(String paymentNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 发送通知（模拟）
        boolean notifyResult = sendPaymentNotification(payment);
        
        payment.setNotifyCount(payment.getNotifyCount() + 1);
        payment.setLastNotifyTime(LocalDateTime.now());
        payment.setNotifyStatus(notifyResult ? 2 : 3); // 2-通知成功，3-通知失败
        payment.setUpdateTime(LocalDateTime.now());
        
        paymentMapper.updateById(payment);
        
        log.info("重新发送支付通知，支付流水号: {}, 结果: {}", paymentNo, notifyResult ? "成功" : "失败");
        return notifyResult;
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 8).toUpperCase();
    }

    /**
     * 调用第三方支付接口（模拟）
     */
    private PaymentResponseVO callThirdPartyPayment(Payment payment) {
        PaymentResponseVO response = new PaymentResponseVO();
        response.setStatus(2); // 支付中
        response.setStatusDesc("支付中");
        
        // 根据支付方式和渠道生成不同的支付参数
        switch (payment.getPaymentMethod()) {
            case 1: // 微信支付
                if (payment.getChannel() == 1) { // APP
                    response.setPaymentParams("微信APP支付参数");
                } else if (payment.getChannel() == 2) { // H5
                    response.setPaymentUrl("https://pay.weixin.qq.com/h5/pay");
                } else if (payment.getChannel() == 3) { // 小程序
                    response.setPaymentParams("微信小程序支付参数");
                } else { // PC
                    response.setQrCode("微信扫码支付二维码");
                }
                break;
            case 2: // 支付宝
                if (payment.getChannel() == 1) { // APP
                    response.setPaymentParams("支付宝APP支付参数");
                } else if (payment.getChannel() == 2) { // H5
                    response.setPaymentUrl("https://openapi.alipay.com/gateway.do");
                } else { // PC
                    response.setQrCode("支付宝扫码支付二维码");
                }
                break;
            case 3: // 银行卡
                response.setPaymentUrl("银行卡支付页面");
                break;
            case 4: // 余额支付
                // 余额支付直接成功（模拟）
                response.setStatus(3);
                response.setStatusDesc("支付成功");
                break;
        }
        
        return response;
    }

    /**
     * 调用第三方退款接口（模拟）
     */
    private boolean callThirdPartyRefund(Payment payment, BigDecimal refundAmount) {
        // 模拟第三方退款接口调用
        log.info("调用第三方退款接口，支付流水号: {}, 退款金额: {}", payment.getPaymentNo(), refundAmount);
        return true; // 模拟退款成功
    }

    /**
     * 查询第三方支付状态（模拟）
     */
    private Integer queryThirdPartyPaymentStatus(String paymentNo) {
        // 模拟查询第三方支付状态
        return 3; // 模拟支付成功
    }

    /**
     * 发送支付通知（模拟）
     */
    private boolean sendPaymentNotification(Payment payment) {
        // 模拟发送支付通知
        log.info("发送支付通知，支付流水号: {}", payment.getPaymentNo());
        return true; // 模拟通知成功
    }

    /**
     * 构建支付响应
     */
    private PaymentResponseVO buildPaymentResponse(Payment payment) {
        PaymentResponseVO response = new PaymentResponseVO();
        response.setPaymentId(payment.getId());
        response.setPaymentNo(payment.getPaymentNo());
        response.setStatus(payment.getStatus());
        response.setStatusDesc(getStatusDesc(payment.getStatus()));
        return response;
    }

    /**
     * 转换为VO对象
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtil.copyProperties(payment, vo);
        
        // 设置描述字段
        vo.setPaymentMethodDesc(getPaymentMethodDesc(payment.getPaymentMethod()));
        vo.setStatusDesc(getStatusDesc(payment.getStatus()));
        vo.setChannelDesc(getChannelDesc(payment.getChannel()));
        vo.setNotifyStatusDesc(getNotifyStatusDesc(payment.getNotifyStatus()));
        
        return vo;
    }

    /**
     * 获取支付方式描述
     */
    private String getPaymentMethodDesc(Integer paymentMethod) {
        if (paymentMethod == null) return "";
        switch (paymentMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "银行卡";
            case 4: return "余额支付";
            default: return "未知";
        }
    }

    /**
     * 获取支付状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "待支付";
            case 2: return "支付中";
            case 3: return "支付成功";
            case 4: return "支付失败";
            case 5: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 获取支付渠道描述
     */
    private String getChannelDesc(Integer channel) {
        if (channel == null) return "";
        switch (channel) {
            case 1: return "APP";
            case 2: return "H5";
            case 3: return "小程序";
            case 4: return "PC";
            default: return "未知";
        }
    }

    /**
     * 获取通知状态描述
     */
    private String getNotifyStatusDesc(Integer notifyStatus) {
        if (notifyStatus == null) return "";
        switch (notifyStatus) {
            case 1: return "未通知";
            case 2: return "通知成功";
            case 3: return "通知失败";
            default: return "未知";
        }
    }
}