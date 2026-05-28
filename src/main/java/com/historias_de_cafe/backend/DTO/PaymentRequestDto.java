package com.historias_de_cafe.backend.DTO;

import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotNull(message = "El ID de la orden es obligatorio")
        Long orderId
) {}
