package cl.velourbe.review.service;

import cl.velourbe.review.exception.InvalidReviewException;
import cl.velourbe.review.exception.ReviewNotFoundException;
import cl.velourbe.review.model.dto.CreateReviewRequestDTO;
import cl.velourbe.review.model.dto.ReviewResponseDTO;
import cl.velourbe.review.model.dto.ScooterRatingDTO;
import cl.velourbe.review.model.entity.Review;
import cl.velourbe.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository repository;

    @InjectMocks
    private ReviewService service;

    private Review review;

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .id(1L)
                .userId(3L)
                .rentalId(10L)
                .scooterId(5L)
                .rating(4)
                .comment("Buena patineta")
                .build();
    }

    @Test
    void create_deberiaGuardarResenaNueva() {
        when(repository.findByUserIdAndRentalId(3L, 10L)).thenReturn(Optional.empty());
        when(repository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponseDTO dto = service.create(3L,
                new CreateReviewRequestDTO(10L, 5L, 4, "Buena patineta"));

        assertThat(dto.rating()).isEqualTo(4);
        assertThat(dto.userId()).isEqualTo(3L);
    }

    @Test
    void create_deberiaFallarSiYaExisteResenaDelArriendo() {
        when(repository.findByUserIdAndRentalId(3L, 10L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.create(3L,
                new CreateReviewRequestDTO(10L, 5L, 5, "Otra reseña")))
                .isInstanceOf(InvalidReviewException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getById_deberiaLanzarSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void getScooterRating_deberiaPromediarCalificaciones() {
        Review otra = Review.builder()
                .id(2L).userId(4L).rentalId(11L).scooterId(5L).rating(5).build();
        when(repository.findByScooterIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(review, otra));

        ScooterRatingDTO rating = service.getScooterRating(5L);

        assertThat(rating.averageRating()).isEqualTo(4.5);
        assertThat(rating.totalReviews()).isEqualTo(2L);
    }

    @Test
    void getScooterRating_deberiaRetornarCeroSinResenas() {
        when(repository.findByScooterIdOrderByCreatedAtDesc(7L)).thenReturn(List.of());

        ScooterRatingDTO rating = service.getScooterRating(7L);

        assertThat(rating.averageRating()).isZero();
        assertThat(rating.totalReviews()).isZero();
    }

    @Test
    void delete_deberiaPermitirAlAutor() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));

        service.delete(1L, 3L, false);

        verify(repository).delete(review);
    }

    @Test
    void delete_deberiaFallarSiNoEsAutorNiAdmin() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> service.delete(1L, 8L, false))
                .isInstanceOf(InvalidReviewException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_deberiaPermitirAlAdmin() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));

        service.delete(1L, 8L, true);

        verify(repository).delete(review);
    }
}
