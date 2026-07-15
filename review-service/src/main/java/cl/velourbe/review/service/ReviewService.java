package cl.velourbe.review.service;

import cl.velourbe.review.exception.InvalidReviewException;
import cl.velourbe.review.exception.ReviewNotFoundException;
import cl.velourbe.review.model.dto.CreateReviewRequestDTO;
import cl.velourbe.review.model.dto.ReviewResponseDTO;
import cl.velourbe.review.model.dto.ScooterRatingDTO;
import cl.velourbe.review.model.entity.Review;
import cl.velourbe.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for review operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;

    /**
     * Creates a new review for a rental. A user can only review each rental once.
     */
    @Transactional
    public ReviewResponseDTO create(Long userId, CreateReviewRequestDTO dto) {
        log.info("Creando reseña userId={} rentalId={} rating={}", userId, dto.rentalId(), dto.rating());
        repository.findByUserIdAndRentalId(userId, dto.rentalId()).ifPresent(r -> {
            throw new InvalidReviewException("Ya existe una reseña para este arriendo");
        });
        Review review = Review.builder()
                .userId(userId)
                .rentalId(dto.rentalId())
                .scooterId(dto.scooterId())
                .rating(dto.rating())
                .comment(dto.comment())
                .build();
        return toDTO(repository.save(review));
    }

    /**
     * Returns a review by id, or throws if not found.
     */
    public ReviewResponseDTO getById(Long id) {
        return toDTO(mustFind(id));
    }

    /**
     * Returns all reviews written by the given user, most recent first.
     */
    public List<ReviewResponseDTO> getMyReviews(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Returns all reviews for a scooter, most recent first.
     */
    public List<ReviewResponseDTO> getByScooter(Long scooterId) {
        return repository.findByScooterIdOrderByCreatedAtDesc(scooterId)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Returns the aggregated rating (average + total) of a scooter.
     */
    public ScooterRatingDTO getScooterRating(Long scooterId) {
        List<Review> reviews = repository.findByScooterIdOrderByCreatedAtDesc(scooterId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return new ScooterRatingDTO(scooterId,
                Math.round(average * 10.0) / 10.0,
                (long) reviews.size());
    }

    /**
     * Returns all reviews in the system, most recent first.
     */
    public List<ReviewResponseDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    /**
     * Deletes a review. Only the author or an admin may delete it.
     */
    @Transactional
    public void delete(Long id, Long userId, boolean isAdmin) {
        Review review = mustFind(id);
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new InvalidReviewException("Solo el autor o un admin puede eliminar la reseña");
        }
        repository.delete(review);
        log.info("Reseña {} eliminada por userId={}", id, userId);
    }

    private Review mustFind(Long id) {
        return repository.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
    }

    private ReviewResponseDTO toDTO(Review r) {
        return new ReviewResponseDTO(
                r.getId(), r.getUserId(), r.getRentalId(), r.getScooterId(),
                r.getRating(), r.getComment(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
