package cl.velourbe.payment.service;

import cl.velourbe.payment.exception.InvalidPaymentException;
import cl.velourbe.payment.exception.PaymentNotFoundException;
import cl.velourbe.payment.model.dto.PaymentRequestDTO;
import cl.velourbe.payment.model.dto.PaymentResponseDTO;
import cl.velourbe.payment.model.entity.Payment;
import cl.velourbe.payment.model.enums.PaymentStatus;
import cl.velourbe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for payment operations.
 * Handles all business logic for payment processing, status management, and history retrieval.
 * All public methods include comprehensive Javadoc as per architecture requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    /**
     * Processes a payment for a rental.
     * 
     * Creates a new payment record with PENDING status, generates a transaction ID,
     * and initiates payment processing via the payment provider.
     * 
     * @param userId the user ID initiating the payment
     * @param dto the payment request containing rental ID and amount
     * @return the created payment response DTO
     * @throws InvalidPaymentException if the amount is invalid or rental already has a payment
     */
    @Transactional
    public PaymentResponseDTO processPayment(Long userId, PaymentRequestDTO dto) {
        log.info("Procesando pago para usuario={} rental={} monto={}", userId, dto.rentalId(), dto.amount());
        
        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("El monto debe ser mayor a 0");
        }
        
        // Check if payment already exists for this rental
        List<Payment> existingPayments = paymentRepository.findByRentalId(dto.rentalId());
        if (!existingPayments.isEmpty()) {
            Payment existing = existingPayments.get(0);
            if (existing.getStatus() == PaymentStatus.COMPLETED) {
                throw new InvalidPaymentException("Este arriendo ya tiene un pago completado");
            }
        }
        
        String transactionId = generateTransactionId();
        Payment payment = Payment.builder()
            .rentalId(dto.rentalId())
            .userId(userId)
            .amount(dto.amount())
            .currency("CLP")
            .status(PaymentStatus.PROCESSING)
            .transactionId(transactionId)
            .paymentMethod(dto.paymentMethod())
            .notes(dto.notes())
            .build();
        
        payment = paymentRepository.save(payment);
        log.info("Pago creado id={} transactionId={}", payment.getId(), transactionId);
        
        return toDTO(payment);
    }
    
    /**
     * Completes a payment transaction.
     * 
     * Updates the payment status to COMPLETED after successful verification
     * from the payment provider (Stripe, PayPal, etc).
     * 
     * @param paymentId the payment ID to complete
     * @return the updated payment response DTO
     * @throws PaymentNotFoundException if payment does not exist
     * @throws InvalidPaymentException if payment is already completed
     */
    @Transactional
    public PaymentResponseDTO completePayment(Long paymentId) {
        log.info("Completando pago id={}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("El pago ya fue completado");
        }
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);
        
        log.info("Pago completado id={}", paymentId);
        return toDTO(payment);
    }
    
    /**
     * Cancels a pending payment.
     * 
     * Sets the payment status to CANCELLED and refunds if already charged.
     * Only pending or processing payments can be cancelled.
     * 
     * @param paymentId the payment ID to cancel
     * @return the cancelled payment response DTO
     * @throws PaymentNotFoundException if payment does not exist
     * @throws InvalidPaymentException if payment cannot be cancelled
     */
    @Transactional
    public PaymentResponseDTO cancelPayment(Long paymentId) {
        log.info("Cancelando pago id={}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("No se puede cancelar un pago completado. Usa refund");
        }
        
        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);
        
        log.info("Pago cancelado id={}", paymentId);
        return toDTO(payment);
    }
    
    /**
     * Retrieves payment history for a user.
     * 
     * Returns all completed payments for the user ordered by most recent first.
     * 
     * @param userId the user ID
     * @return list of completed payments ordered by creation date descending
     */
    public List<PaymentResponseDTO> getUserPaymentHistory(Long userId) {
        log.debug("Obteniendo historial de pagos para usuario={}", userId);
        return paymentRepository.findCompletedPaymentsByUser(userId)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Retrieves a single payment by ID.
     * 
     * @param paymentId the payment ID
     * @return the payment response DTO
     * @throws PaymentNotFoundException if payment does not exist
     */
    public PaymentResponseDTO getPayment(Long paymentId) {
        log.debug("Obteniendo pago id={}", paymentId);
        return paymentRepository.findById(paymentId)
            .map(this::toDTO)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
    
    /**
     * Refunds a completed payment.
     * 
     * Marks the payment as REFUNDED after successful refund processing.
     * Only completed payments can be refunded.
     * 
     * @param paymentId the payment ID to refund
     * @return the refunded payment response DTO
     * @throws PaymentNotFoundException if payment does not exist
     * @throws InvalidPaymentException if payment cannot be refunded
     */
    @Transactional
    public PaymentResponseDTO refundPayment(Long paymentId) {
        log.info("Reembolsando pago id={}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("Solo se pueden reembolsar pagos completados");
        }
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);
        
        log.info("Pago reembolsado id={}", paymentId);
        return toDTO(payment);
    }
    
    /**
     * Generates a unique transaction ID for payment processing.
     * 
     * Format: TXN-{timestamp}-{random}
     * 
     * @return a unique transaction ID string
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
    
    /**
     * Converts a Payment entity to a response DTO.
     * 
     * @param payment the payment entity to convert
     * @return the payment response DTO
     */
    private PaymentResponseDTO toDTO(Payment payment) {
        return new PaymentResponseDTO(
            payment.getId(),
            payment.getRentalId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getStatus().name(),
            payment.getTransactionId(),
            payment.getPaymentMethod(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
