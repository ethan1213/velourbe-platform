package cl.velourbe.payment.repository;

import cl.velourbe.payment.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity operations.
 * Handles data access layer for payments using Spring Data JPA.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Finds all payments for a specific user.
     * 
     * @param userId the user ID
     * @return list of payments for the user
     */
    List<Payment> findByUserId(Long userId);
    
    /**
     * Finds all payments for a specific rental.
     * 
     * @param rentalId the rental ID
     * @return list of payments for the rental
     */
    List<Payment> findByRentalId(Long rentalId);
    
    /**
     * Finds a payment by transaction ID.
     * 
     * @param transactionId the transaction ID
     * @return optional containing the payment if found
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Finds all completed payments for a user, ordered by creation date descending.
     * 
     * @param userId the user ID
     * @return list of completed payments ordered by most recent first
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED' ORDER BY p.createdAt DESC")
    List<Payment> findCompletedPaymentsByUser(Long userId);
}
