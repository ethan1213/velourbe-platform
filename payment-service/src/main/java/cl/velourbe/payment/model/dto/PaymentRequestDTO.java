package cl.velourbe.payment.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for payment creation/processing requests.
 */
public record PaymentRequestDTO(
    @NotNull(message = "rentalId es requerido")
    Long rentalId,
    
    @NotNull(message = "amount es requerido")
    @Positive(message = "amount debe ser > 0")
    BigDecimal amount,
    
    @NotBlank(message = "paymentMethod es requerido")
    String paymentMethod,
    
    String notes
) {}
