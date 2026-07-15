package cl.velourbe.payment.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import cl.velourbe.payment.exception.InvalidPaymentException;
import cl.velourbe.payment.exception.PaymentNotFoundException;
import cl.velourbe.payment.model.dto.PaymentRequestDTO;
import cl.velourbe.payment.model.dto.PaymentResponseDTO;
import cl.velourbe.payment.model.entity.Payment;
import cl.velourbe.payment.model.enums.PaymentStatus;
import cl.velourbe.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for PaymentService with Mockito.
 * Tests business logic without Spring context.
 * Target coverage: 40%+
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    private PaymentService paymentService;
    
    @BeforeEach
    public void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }
    
    @Test
    public void testProcessPayment_Success() {
        // Given
        Long userId = 1L;
        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("10000"), "CARD", "Test payment");
        
        // When
        when(paymentRepository.findByRentalId(1L)).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        
        PaymentResponseDTO response = paymentService.processPayment(userId, dto);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("10000"), response.amount());
        assertEquals("PROCESSING", response.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
    
    @Test
    public void testProcessPayment_InvalidAmount() {
        // Given
        Long userId = 1L;
        PaymentRequestDTO dto = new PaymentRequestDTO(1L, BigDecimal.ZERO, "CARD", "Invalid");
        
        // When & Then
        assertThrows(InvalidPaymentException.class, () -> {
            paymentService.processPayment(userId, dto);
        });
    }
    
    @Test
    public void testProcessPayment_NegativeAmount() {
        // Given
        Long userId = 1L;
        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("-5000"), "CARD", "Invalid");
        
        // When & Then
        assertThrows(InvalidPaymentException.class, () -> {
            paymentService.processPayment(userId, dto);
        });
    }
    
    @Test
    public void testCompletePayment_Success() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setAmount(new BigDecimal("10000"));
        payment.setUserId(1L);
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        
        PaymentResponseDTO response = paymentService.completePayment(paymentId);
        
        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED.name(), response.status());
        verify(paymentRepository, times(1)).save(payment);
    }
    
    @Test
    public void testCompletePayment_NotFound() {
        // Given
        Long paymentId = 999L;
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // Then
        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.completePayment(paymentId);
        });
    }
    
    @Test
    public void testCompletePayment_AlreadyCompleted() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        
        // Then
        assertThrows(InvalidPaymentException.class, () -> {
            paymentService.completePayment(paymentId);
        });
    }
    
    @Test
    public void testCancelPayment_Success() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        
        PaymentResponseDTO response = paymentService.cancelPayment(paymentId);
        
        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.CANCELLED.name(), response.status());
    }
    
    @Test
    public void testCancelPayment_CompletedPayment() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        
        // Then
        assertThrows(InvalidPaymentException.class, () -> {
            paymentService.cancelPayment(paymentId);
        });
    }
    
    @Test
    public void testGetUserPaymentHistory_Success() {
        // Given
        Long userId = 1L;
        Payment p1 = new Payment();
        p1.setId(1L);
        p1.setUserId(userId);
        p1.setStatus(PaymentStatus.COMPLETED);
        p1.setAmount(new BigDecimal("10000"));
        
        // When
        when(paymentRepository.findCompletedPaymentsByUser(userId))
            .thenReturn(List.of(p1));
        
        List<PaymentResponseDTO> history = paymentService.getUserPaymentHistory(userId);
        
        // Then
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(1L, history.get(0).id());
    }
    
    @Test
    public void testGetPayment_Success() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setAmount(new BigDecimal("5000"));
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        
        PaymentResponseDTO response = paymentService.getPayment(paymentId);
        
        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.id());
    }
    
    @Test
    public void testGetPayment_NotFound() {
        // Given
        Long paymentId = 999L;
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        // Then
        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.getPayment(paymentId);
        });
    }
    
    @Test
    public void testRefundPayment_Success() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("10000"));
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        
        PaymentResponseDTO response = paymentService.refundPayment(paymentId);
        
        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED.name(), response.status());
    }
    
    @Test
    public void testRefundPayment_NotCompleted() {
        // Given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.PENDING);
        
        // When
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        
        // Then
        assertThrows(InvalidPaymentException.class, () -> {
            paymentService.refundPayment(paymentId);
        });
    }
}
