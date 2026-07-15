package cl.velourbe.payment.controller;

import cl.velourbe.payment.model.dto.PaymentRequestDTO;
import cl.velourbe.payment.model.dto.PaymentResponseDTO;
import cl.velourbe.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for payment endpoints.
 * Provides payment processing, retrieval, and cancellation operations.
 * All endpoints require authentication via JWT token.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Processes a new payment for a rental.
     * 
     * @param dto payment request containing rental ID, amount, and payment method
     * @return created payment response with HATEOAS links
     */
    @PostMapping
    public ResponseEntity<EntityModel<PaymentResponseDTO>> processPayment(
            @Valid @RequestBody PaymentRequestDTO dto) {
        Long userId = getCurrentUserId();
        log.info("Solicitando procesamiento de pago para usuario={}", userId);
        
        PaymentResponseDTO response = paymentService.processPayment(userId, dto);
        EntityModel<PaymentResponseDTO> model = EntityModel.of(response,
            linkTo(methodOn(PaymentController.class).getPayment(response.id())).withSelfRel(),
            linkTo(methodOn(PaymentController.class).completePayment(response.id())).withRel("complete"),
            linkTo(methodOn(PaymentController.class).getUserHistory()).withRel("history"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
    
    /**
     * Retrieves a single payment by ID.
     * 
     * @param id the payment ID
     * @return payment response with HATEOAS links
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PaymentResponseDTO>> getPayment(@PathVariable Long id) {
        log.debug("Obteniendo pago id={}", id);
        PaymentResponseDTO response = paymentService.getPayment(id);
        EntityModel<PaymentResponseDTO> model = EntityModel.of(response,
            linkTo(methodOn(PaymentController.class).getPayment(id)).withSelfRel(),
            linkTo(methodOn(PaymentController.class).getUserHistory()).withRel("history"));
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Retrieves payment history for the authenticated user.
     * 
     * @return list of completed payments with HATEOAS links
     */
    @GetMapping("/history")
    public ResponseEntity<CollectionModel<EntityModel<PaymentResponseDTO>>> getUserHistory() {
        Long userId = getCurrentUserId();
        log.info("Solicitando historial de pagos para usuario={}", userId);
        
        List<EntityModel<PaymentResponseDTO>> models = paymentService.getUserPaymentHistory(userId)
            .stream()
            .map(p -> EntityModel.of(p,
                linkTo(methodOn(PaymentController.class).getPayment(p.id())).withSelfRel()))
            .toList();
        
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(PaymentController.class).getUserHistory()).withSelfRel()));
    }
    
    /**
     * Completes a payment transaction.
     * Called after successful verification from payment provider.
     * 
     * @param id the payment ID to complete
     * @return completed payment response with HATEOAS links
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<EntityModel<PaymentResponseDTO>> completePayment(@PathVariable Long id) {
        log.info("Completando pago id={}", id);
        PaymentResponseDTO response = paymentService.completePayment(id);
        EntityModel<PaymentResponseDTO> model = EntityModel.of(response,
            linkTo(methodOn(PaymentController.class).getPayment(id)).withSelfRel(),
            linkTo(methodOn(PaymentController.class).refundPayment(id)).withRel("refund"));
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Cancels a pending payment.
     * Cannot cancel already completed payments.
     * 
     * @param id the payment ID to cancel
     * @return cancelled payment response with HATEOAS links
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EntityModel<PaymentResponseDTO>> cancelPayment(@PathVariable Long id) {
        log.info("Cancelando pago id={}", id);
        PaymentResponseDTO response = paymentService.cancelPayment(id);
        EntityModel<PaymentResponseDTO> model = EntityModel.of(response,
            linkTo(methodOn(PaymentController.class).getPayment(id)).withSelfRel());
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Refunds a completed payment.
     * Only completed payments can be refunded.
     * 
     * @param id the payment ID to refund
     * @return refunded payment response with HATEOAS links
     */
    @PatchMapping("/{id}/refund")
    public ResponseEntity<EntityModel<PaymentResponseDTO>> refundPayment(@PathVariable Long id) {
        log.info("Reembolsando pago id={}", id);
        PaymentResponseDTO response = paymentService.refundPayment(id);
        EntityModel<PaymentResponseDTO> model = EntityModel.of(response,
            linkTo(methodOn(PaymentController.class).getPayment(id)).withSelfRel());
        
        return ResponseEntity.ok(model);
    }
    
    /**
     * Extracts the current user ID from the JWT token.
     * 
     * @return the authenticated user ID
     */
    private Long getCurrentUserId() {
        String userIdStr = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
        return Long.parseLong(userIdStr);
    }
}
