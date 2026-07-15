package cl.velourbe.review.model.dto;

/**
 * DTO with the aggregated rating of a scooter.
 */
public record ScooterRatingDTO(
        Long scooterId,
        Double averageRating,
        Long totalReviews
) {}
