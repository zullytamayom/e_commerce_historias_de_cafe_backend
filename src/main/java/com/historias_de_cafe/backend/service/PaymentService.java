package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.PaymentRequestDto;
import com.historias_de_cafe.backend.DTO.PaymentResponseDto;
import com.historias_de_cafe.backend.model.Order;
import com.historias_de_cafe.backend.model.OrderDetail;
import com.historias_de_cafe.backend.model.Payment;
import com.historias_de_cafe.backend.repository.OrderRepository;
import com.historias_de_cafe.backend.repository.PaymentRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private static final String ORDER_STATUS_PENDING_DELIVERY = "Pendiente Entrega";
    private static final String ORDER_STATUS_IN_PROCESS = "En proceso";
    private static final String ORDER_STATUS_CANCELLED = "Cancelado";
    private static final String PAYMENT_STATUS_PENDING = "En proceso";
    private static final String PAYMENT_STATUS_APPROVED = "Aprobado";
    private static final String PAYMENT_STATUS_CANCELLED = "Cancelado";

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final String accessToken;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          @Value("${mercadopago.access-token}") String accessToken) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.accessToken = accessToken;
    }

    public PaymentResponseDto createPaymentPreference(PaymentRequestDto dto) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("Mercado Pago access token is not configured");
        }

        Order order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + dto.orderId()));

        paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
            throw new RuntimeException("Payment already exists for order id: " + order.getId());
        });

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            List<PreferenceItemRequest> items = order.getDetails().stream()
                    .map(this::toPreferenceItemRequest)
                    .toList();

            // Nota: En producción, estas URLs deberían apuntar a tu dominio de Render o al Frontend
            String baseUrl = "https://e-commerce-historias-de-cafe-1.onrender.com"; // URL de tu frontend
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(baseUrl + "/pages/cart/success.html")
                    .pending(baseUrl + "/pages/cart/pending.html")
                    .failure(baseUrl + "/pages/cart/failure.html")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(order.getId().toString())
                    .backUrls(backUrls)
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setTransactionNumber(preference.getId());
            payment.setTransactionDate(LocalDateTime.now());
            payment.setTransactionStatus(PAYMENT_STATUS_PENDING);

            order.setStateOrder(ORDER_STATUS_IN_PROCESS);

            Payment saved = paymentRepository.save(payment);
            return toResponseDto(saved, preference.getInitPoint());
        } catch (Exception exception) {
            throw new RuntimeException("Error creating Mercado Pago preference: " + exception.getMessage(), exception);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return toResponseDto(payment, null);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order id: " + orderId));
        return toResponseDto(payment, null);
    }

    public PaymentResponseDto updateStatus(Long id, String status, String transactionNumber) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        payment.setTransactionDate(LocalDateTime.now());

        if (transactionNumber != null && !transactionNumber.isBlank()) {
            payment.setTransactionNumber(transactionNumber);
        }

        if ("APPROVED".equalsIgnoreCase(status)) {
            payment.setTransactionStatus(PAYMENT_STATUS_APPROVED);
            payment.getOrder().setStateOrder(ORDER_STATUS_PENDING_DELIVERY);
        } else if ("REJECTED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            payment.setTransactionStatus(PAYMENT_STATUS_CANCELLED);
            payment.getOrder().setStateOrder(ORDER_STATUS_CANCELLED);
        } else {
            payment.setTransactionStatus(PAYMENT_STATUS_PENDING);
            payment.getOrder().setStateOrder(ORDER_STATUS_IN_PROCESS);
        }

        return toResponseDto(paymentRepository.save(payment), null);
    }

    private PreferenceItemRequest toPreferenceItemRequest(OrderDetail detail) {
        BigDecimal unitPrice = detail.getProduct().getPrice();

        return PreferenceItemRequest.builder()
                .title(detail.getProduct().getName())
                .quantity(detail.getQuantityProducts())
                .unitPrice(unitPrice)
                .build();
    }

    private PaymentResponseDto toResponseDto(Payment payment, String paymentUrl) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getTransactionNumber(),
                payment.getTransactionDate(),
                payment.getTransactionStatus(),
                paymentUrl
        );
    }
}
