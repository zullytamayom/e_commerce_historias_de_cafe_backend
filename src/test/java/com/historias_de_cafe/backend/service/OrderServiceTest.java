package com.historias_de_cafe.backend.service;

import com.historias_de_cafe.backend.DTO.OrderDetailRequestDto;
import com.historias_de_cafe.backend.DTO.OrderRequestDto;
import com.historias_de_cafe.backend.DTO.OrderResponseDto;
import com.historias_de_cafe.backend.model.Order;
import com.historias_de_cafe.backend.model.OrderDetail;
import com.historias_de_cafe.backend.model.Product;
import com.historias_de_cafe.backend.model.Role;
import com.historias_de_cafe.backend.model.User;
import com.historias_de_cafe.backend.repository.OrderRepository;
import com.historias_de_cafe.backend.repository.ProductRepository;
import com.historias_de_cafe.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createBuildsOrderCalculatesTotalAndDiscountsStock() {
        User user = user();
        Product product = product();
        OrderRequestDto request = new OrderRequestDto(
                1L,
                "PENDING",
                List.of(new OrderDetailRequestDto(1L, 2))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(20L);
            long detailId = 1L;
            for (OrderDetail detail : order.getDetails()) {
                detail.setId(detailId++);
            }
            return order;
        });

        OrderResponseDto response = orderService.create(request);

        assertEquals(20L, response.id());
        assertEquals(1L, response.userId());
        assertEquals("En proceso", response.stateOrder());
        assertTrue(BigDecimal.valueOf(70000.0).compareTo(response.subtotal()) == 0);
        assertTrue(BigDecimal.valueOf(70000.0).compareTo(response.total()) == 0);
        assertEquals(8, product.getStock());
        assertEquals(1, response.details().size());
        assertEquals(1L, response.details().get(0).productId());
        assertEquals(2, response.details().get(0).quantityProducts());
    }

    @Test
    void createThrowsWhenOrderHasNoDetails() {
        OrderRequestDto request = new OrderRequestDto(1L, "En proceso", List.of());

        assertThrows(RuntimeException.class, () -> orderService.create(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createThrowsWhenStockIsNotEnough() {
        User user = user();
        Product product = product();
        OrderRequestDto request = new OrderRequestDto(
                1L,
                "En proceso",
                List.of(new OrderDetailRequestDto(1L, 99))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> orderService.create(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateStateAcceptsOnlyDatabaseAllowedStates() {
        Order order = existingOrder();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponseDto response = orderService.updateState(10L, "Entregado");

        assertEquals("Entregado", response.stateOrder());
    }

    @Test
    void updateStateThrowsForInvalidState() {
        Order order = existingOrder();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> orderService.updateState(10L, "PAGADO"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    private User user() {
        return new User(
                1L,
                "Carlos Medina",
                "carlos@example.com",
                "password123",
                Role.CLIENT,
                LocalDateTime.now(),
                true
        );
    }

    private Product product() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Cafe Huila Especial");
        product.setDescription("Cafe especial de Huila");
        product.setPrice(BigDecimal.valueOf(35000.0));
        product.setStock(10);
        return product;
    }

    private Order existingOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setUser(user());
        order.setStateOrder("En proceso");
        order.setSubtotal(BigDecimal.valueOf(35000.0));
        order.setTotal(BigDecimal.valueOf(35000.0));
        order.setOrderDate(LocalDateTime.now());

        OrderDetail detail = new OrderDetail();
        detail.setId(1L);
        detail.setProduct(product());
        detail.setQuantityProducts(1);
        order.addDetail(detail);

        return order;
    }
}
