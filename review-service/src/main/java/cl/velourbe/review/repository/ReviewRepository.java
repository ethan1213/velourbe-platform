package cl.velourbe.review.repository;

import cl.velourbe.review.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review operations.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds all reviews written by a user, most recent first.
     */
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all reviews for a scooter, most recent first.
     */
    List<Review> findByScooterIdOrderByCreatedAtDesc(Long scooterId);

    /**
     * Finds the review a user wrote for a specific rental (one review per rental).
     */
    Optional<Review> findByUserIdAndRentalId(Long userId, Long rentalId);

    /**
     * Returns all reviews, most recent first.
     */
    List<Review> findAllByOrderByCreatedAtDesc();
}
