package com.drone.delivery.payment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.delivery.common.result.Result;
import com.drone.delivery.payment.dto.PaymentQueryDTO;
import com.drone.delivery.payment.dto.PaymentRequestDTO;
import com.drone.delivery.payment.dto.RefundRequestDTO;
import com.drone.delivery.payment.service.PaymentService;
import com.drone.delivery.payment.vo.PaymentResponseVO;
import com.drone.delivery.payment.vo.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 支付控制器
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Validated
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "创建支付订单", description = "发起支付请求，创建支付订单")
    public Result<PaymentResponseVO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        PaymentResponseVO response = paymentService.createPayment(requestDTO);
        return Result.success(response);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询支付记录", description = "根据条件分页查询支付记录")
    public Result<IPage<PaymentVO>> page(@Valid @RequestBody PaymentQueryDTO queryDTO) {
        IPage<PaymentVO> page = paymentService.page(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询支付详情", description = "根据支付ID查询支付详情")
    public Result<PaymentVO> getById(@Parameter(description = "支付ID") @PathVariable @NotNull Long id) {
        PaymentVO payment = paymentService.getById(id);
        return Result.success(payment);
    }

    @GetMapping("/payment-no/{paymentNo}")
    @Operation(summary = "根据支付流水号查询支付详情", description = "根据支付流水号查询支付详情")
    public Result<PaymentVO> getByPaymentNo(@Parameter(description = "支付流水号") @PathVariable @NotBlank String paymentNo) {
        PaymentVO payment = paymentService.getByPaymentNo(paymentNo);
        return Result.success(payment);
    }

    @GetMapping("/order-no/{orderNo}")
    @Operation(summary = "根据订单号查询支付详情", description = "根据订单号查询最新的支付详情")
    public Result<PaymentVO> getByOrderNo(@Parameter(description = "订单号") @PathVariable @NotBlank String orderNo) {
        PaymentVO payment = paymentService.getByOrderNo(orderNo);
        return Result.success(payment);
    }

    @GetMapping("/status/{paymentNo}")
    @Operation(summary = "查询支付状态", description = "根据支付流水号查询支付状态")
    public Result<Integer> queryPaymentStatus(@Parameter(description = "支付流水号") @PathVariable @NotBlank String paymentNo) {
        Integer status = paymentService.queryPaymentStatus(paymentNo);
        return Result.success(status);
    }

    @PostMapping("/callback/success")
    @Operation(summary = "支付成功回调", description = "第三方支付成功回调接口")
    public Result<Boolean> paymentSuccess(
            @Parameter(description = "支付流水号") @RequestParam @NotBlank String paymentNo,
            @Parameter(description = "第三方交易号") @RequestParam @NotBlank String thirdPartyTransactionId) {
        Boolean result = paymentService.paymentSuccess(paymentNo, thirdPartyTransactionId);
        return Result.success(result);
    }

    @PostMapping("/callback/failed")
    @Operation(summary = "支付失败回调", description = "第三方支付失败回调接口")
    public Result<Boolean> paymentFailed(
            @Parameter(description = "支付流水号") @RequestParam @NotBlank String paymentNo,
            @Parameter(description = "失败原因") @RequestParam @NotBlank String failReason) {
        Boolean result = paymentService.paymentFailed(paymentNo, failReason);
        return Result.success(result);
    }

    @PostMapping("/refund")
    @Operation(summary = "申请退款", description = "申请退款")
    public Result<Boolean> refund(@Valid @RequestBody RefundRequestDTO requestDTO) {
        Boolean result = paymentService.refund(requestDTO);
        return Result.success(result);
    }

    @PostMapping("/refund/callback/success")
    @Operation(summary = "退款成功回调", description = "第三方退款成功回调接口")
    public Result<Boolean> refundSuccess(@Parameter(description = "支付流水号") @RequestParam @NotBlank String paymentNo) {
        Boolean result = paymentService.refundSuccess(paymentNo);
        return Result.success(result);
    }

    @PostMapping("/cancel/{paymentNo}")
    @Operation(summary = "取消支付", description = "取消支付订单")
    public Result<Boolean> cancelPayment(@Parameter(description = "支付流水号") @PathVariable @NotBlank String paymentNo) {
        Boolean result = paymentService.cancelPayment(paymentNo);
        return Result.success(result);
    }

    @PostMapping("/sync/{paymentNo}")
    @Operation(summary = "同步支付状态", description = "从第三方同步支付状态")
    public Result<Boolean> syncPaymentStatus(@Parameter(description = "支付流水号") @PathVariable @NotBlank String paymentNo) {
        Boolean result = paymentService.syncPaymentStatus(paymentNo);
        return Result.success(result);
    }

    @PostMapping("/notify/resend/{paymentNo}")
    @Operation(summary = "重新发送支付通知", description = "重新发送支付通知")
    public Result<Boolean> resendNotification(@Parameter(description = "支付流水号") @PathVariable @NotBlank String paymentNo) {
        Boolean result = paymentService.resendNotification(paymentNo);
        return Result.success(result);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "支付服务健康检查")
    public Result<String> health() {
        return Result.success("Payment Service is running");
    }
}