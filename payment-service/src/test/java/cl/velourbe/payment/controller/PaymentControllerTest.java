package cl.velourbe.payment.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.velourbe.payment.exception.PaymentNotFoundException;
import cl.velourbe.payment.model.dto.PaymentRequestDTO;
import cl.velourbe.payment.model.dto.PaymentResponseDTO;
import cl.velourbe.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

/**
 * Integration tests for PaymentController using MockMvc.
 * Tests REST endpoints with Spring Security and error handling.
 * Target coverage: 40%+
 */
@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc
public class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
        // Setup can be extended if needed
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testProcessPayment_Success() throws Exception {
        // Given
        PaymentRequestDTO request = new PaymentRequestDTO(
            1L, new BigDecimal("10000"), "CARD", "Test payment"
        );
        PaymentResponseDTO response = new PaymentResponseDTO(
            1L, 1L, 1L, new BigDecimal("10000"), "CLP", "PROCESSING", "TXN-001", null, null, null
        );
        
        // When
        when(paymentService.processPayment(1L, request)).thenReturn(response);
        
        // Then
        mockMvc.perform(post("/api/payments")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("PROCESSING"));
        
        verify(paymentService, times(1)).processPayment(1L, request);
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testProcessPayment_InvalidData() throws Exception {
        // Given
        PaymentRequestDTO request = new PaymentRequestDTO(
            1L, BigDecimal.ZERO, "CARD", "Invalid"
        );
        
        // When & Then
        mockMvc.perform(post("/api/payments")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetPayment_Success() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentResponseDTO response = new PaymentResponseDTO(
            paymentId, 1L, 1L, new BigDecimal("10000"), "CLP", "COMPLETED", 
            "TXN-001", null, null, null
        );
        
        // When
        when(paymentService.getPayment(paymentId)).thenReturn(response);
        
        // Then
        mockMvc.perform(get("/api/payments/{id}", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(paymentId))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        verify(paymentService, times(1)).getPayment(paymentId);
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetPayment_NotFound() throws Exception {
        // Given
        Long paymentId = 999L;
        
        // When
        when(paymentService.getPayment(paymentId))
            .thenThrow(new PaymentNotFoundException("Payment not found"));
        
        // Then
        mockMvc.perform(get("/api/payments/{id}", paymentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"));
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testGetUserPaymentHistory_Success() throws Exception {
        // Given
        PaymentResponseDTO p1 = new PaymentResponseDTO(
            1L, 1L, 1L, new BigDecimal("5000"), "CLP", "COMPLETED", 
            "TXN-001", null, null, null
        );
        PaymentResponseDTO p2 = new PaymentResponseDTO(
            2L, 1L, 2L, new BigDecimal("8000"), "CLP", "COMPLETED", 
            "TXN-002", null, null, null
        );
        
        // When
        when(paymentService.getUserPaymentHistory(1L))
            .thenReturn(List.of(p1, p2));
        
        // Then
        mockMvc.perform(get("/api/payments/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].id").value(2L));
        
        verify(paymentService, times(1)).getUserPaymentHistory(1L);
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCompletePayment_Success() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentResponseDTO response = new PaymentResponseDTO(
            paymentId, 1L, 1L, new BigDecimal("10000"), "CLP", "COMPLETED", 
            "TXN-001", null, null, null
        );
        
        // When
        when(paymentService.completePayment(paymentId)).thenReturn(response);
        
        // Then
        mockMvc.perform(patch("/api/payments/{id}/complete", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        verify(paymentService, times(1)).completePayment(paymentId);
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testCancelPayment_Success() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentResponseDTO response = new PaymentResponseDTO(
            paymentId, 1L, 1L, new BigDecimal("10000"), "CLP", "CANCELLED", 
            "TXN-001", null, null, null
        );
        
        // When
        when(paymentService.cancelPayment(paymentId)).thenReturn(response);
        
        // Then
        mockMvc.perform(patch("/api/payments/{id}/cancel", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        verify(paymentService, times(1)).cancelPayment(paymentId);
    }
    
    @Test
    @WithMockUser(username = "1", roles = "USER")
    public void testRefundPayment_Success() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentResponseDTO response = new PaymentResponseDTO(
            paymentId, 1L, 1L, new BigDecimal("10000"), "CLP", "REFUNDED", 
            "TXN-001", null, null, null
        );
        
        // When
        when(paymentService.refundPayment(paymentId)).thenReturn(response);
        
        // Then
        mockMvc.perform(patch("/api/payments/{id}/refund", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REFUNDED"));
        
        verify(paymentService, times(1)).refundPayment(paymentId);
    }
    
    @Test
    public void testProcessPayment_Unauthorized() throws Exception {
        // Given
        PaymentRequestDTO request = new PaymentRequestDTO(
            1L, new BigDecimal("10000"), "CARD", "Test"
        );
        
        // Then - no @WithMockUser means unauthenticated request
        mockMvc.perform(post("/api/payments")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
