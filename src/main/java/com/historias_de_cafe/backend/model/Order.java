package com.historias_de_cafe.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private Long id;

    @Column(name = "state_order", nullable = false)
    private String stateOrder;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, String stateOrder, BigDecimal subtotal, BigDecimal total, LocalDateTime orderDate, User user, List<OrderDetail> details) {
        this.id = id;
        this.stateOrder = stateOrder;
        this.subtotal = subtotal;
        this.total = total;
        this.orderDate = orderDate;
        this.user = user;
        setDetails(details);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStateOrder() {
        return stateOrder;
    }

    public void setStateOrder(String stateOrder) {
        this.stateOrder = stateOrder;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details.clear();
        if (details != null) {
            details.forEach(this::addDetail);
        }
    }

    public void addDetail(OrderDetail detail) {
        details.add(detail);
        detail.setOrder(this);
    }
}
