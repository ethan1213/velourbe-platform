package cl.velourbe.rental.repository;

import cl.velourbe.rental.model.entity.Rental;
import cl.velourbe.rental.model.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Rental}.
 * Extiende {@link JpaRepository} para operaciones CRUD estándar y define
 * consultas JPQL personalizadas para análisis de uso de la flota.
 */
public interface RentalRepository extends JpaRepository<Rental, Long> {

    /**
     * Método derivado: retorna todos los arriendos asociados a un usuario.
     *
     * @param userId identificador del usuario
     * @return lista de arriendos del usuario ordenados por ID
     */
    List<Rental> findByUserId(Long userId);

    /**
     * Método derivado: busca el arriendo activo de un usuario dado.
     *
     * @param userId identificador del usuario
     * @param status estado del arriendo a buscar
     * @return Optional con el arriendo si existe, vacío si no
     */
    Optional<Rental> findByUserIdAndStatus(Long userId, RentalStatus status);

    /**
     * Consulta personalizada JPQL: retorna arriendos completados cuya duración
     * es igual o mayor al mínimo indicado en minutos.
     * Permite identificar usuarios con sesiones largas para análisis de negocio.
     *
     * @param minMinutes duración mínima en minutos (inclusivo)
     * @return lista de arriendos completados con larga duración
     */
    @Query("SELECT r FROM Rental r WHERE r.status = 'COMPLETED' AND r.totalMinutes >= :minMinutes")
    List<Rental> findCompletedWithMinDuration(@Param("minMinutes") int minMinutes);
}
