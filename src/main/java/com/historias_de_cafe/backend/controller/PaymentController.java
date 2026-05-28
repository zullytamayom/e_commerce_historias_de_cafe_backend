package com.historias_de_cafe.backend.controller;

import com.historias_de_cafe.backend.DTO.PaymentRequestDto;
import com.historias_de_cafe.backend.DTO.PaymentResponseDto;
import com.historias_de_cafe.backend.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<PaymentResponseDto> createPaymentPreference(@RequestBody PaymentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentPreference(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDto> updateStatus(@PathVariable Long id,
                                                           @RequestParam String status,
                                                           @RequestParam(required = false) String transactionNumber) {
        return ResponseEntity.ok(paymentService.updateStatus(id, status, transactionNumber));
    }
}
