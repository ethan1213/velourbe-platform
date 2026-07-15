package cl.velourbe.payment.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment responses.
 */
public record PaymentResponseDTO(
    Long id,
    Long rentalId,
    Long userId,
    BigDecimal amount,
    String currency,
    String status,
    String transactionId,
    String paymentMethod,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
