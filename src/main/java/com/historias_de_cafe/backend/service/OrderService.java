package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.OrderDetailRequestDto;
import com.historias_de_cafe.backend.DTO.OrderDetailResponseDto;
import com.historias_de_cafe.backend.DTO.OrderRequestDto;
import com.historias_de_cafe.backend.DTO.OrderResponseDto;
import com.historias_de_cafe.backend.model.Order;
import com.historias_de_cafe.backend.model.OrderDetail;
import com.historias_de_cafe.backend.model.Product;
import com.historias_de_cafe.backend.model.User;
import com.historias_de_cafe.backend.repository.OrderRepository;
import com.historias_de_cafe.backend.repository.ProductRepository;
import com.historias_de_cafe.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class OrderService {

    private static final String DEFAULT_STATE = "En proceso";
    private static final Set<String> ALLOWED_STATES = Set.of("En proceso", "Pendiente Entrega", "Entregado", "Cancelado");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public OrderResponseDto create(OrderRequestDto dto) {
        if (dto.details() == null || dto.details().isEmpty()) {
            throw new RuntimeException("Order must have at least one detail");
        }

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.userId()));

        Order order = new Order();
        order.setUser(user);
        order.setStateOrder(normalizeState(dto.stateOrder()));
        order.setOrderDate(LocalDateTime.now());

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailRequestDto item : dto.details()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.productId()));

            if (item.quantityProducts() == null || item.quantityProducts() <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            if (product.getStock() < item.quantityProducts()) {
                throw new RuntimeException("Not enough stock for product id: " + item.productId());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.quantityProducts()));
            subtotal = subtotal.add(lineTotal);
            product.setStock(product.getStock() - item.quantityProducts());

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantityProducts(item.quantityProducts());

            details.add(detail);
        }

        order.setDetails(details);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal);

        Order saved = orderRepository.save(order);

        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return toResponseDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public OrderResponseDto updateState(Long id, String state) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStateOrder(normalizeState(state));
        return toResponseDto(orderRepository.save(order));
    }

    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        orderRepository.delete(order);
    }

    private OrderResponseDto toResponseDto(Order order) {
        List<OrderDetailResponseDto> detailDtos = order.getDetails().stream()
                .map(detail -> new OrderDetailResponseDto(
                        detail.getId(),
                        detail.getProduct().getId(),
                        detail.getProduct().getName(),
                        detail.getQuantityProducts()
                ))
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getUser().getId(),
                order.getStateOrder(),
                order.getSubtotal(),
                order.getTotal(),
                order.getOrderDate(),
                detailDtos
        );
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return DEFAULT_STATE;
        }

        if ("PENDING".equalsIgnoreCase(state)) {
            return DEFAULT_STATE;
        }

        if (!ALLOWED_STATES.contains(state)) {
            throw new RuntimeException("Invalid order state. Allowed values: " + ALLOWED_STATES);
        }

        return state;
    }
}
