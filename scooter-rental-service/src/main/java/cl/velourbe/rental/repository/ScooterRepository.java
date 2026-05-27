package cl.velourbe.rental.repository;

import cl.velourbe.rental.model.entity.Scooter;
import cl.velourbe.rental.model.enums.ScooterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Scooter}.
 * Extiende {@link JpaRepository} para operaciones CRUD estándar y define
 * consultas JPQL personalizadas para casos de uso operativos.
 */
public interface ScooterRepository extends JpaRepository<Scooter, Long> {

    /**
     * Método derivado: retorna patinetas filtradas por estado.
     *
     * @param status estado de la patineta (AVAILABLE, IN_USE o MAINTENANCE)
     * @return lista de patinetas con el estado indicado
     */
    List<Scooter> findByStatus(ScooterStatus status);

    /**
     * Consulta personalizada JPQL: retorna patinetas con nivel de batería por debajo
     * del umbral indicado, ordenadas de menor a mayor batería.
     * Permite al operador identificar patinetas que necesitan recarga urgente.
     *
     * @param threshold porcentaje máximo de batería (exclusivo)
     * @return lista de patinetas con batería crítica, ordenadas ascendentemente
     */
    @Query("SELECT s FROM Scooter s WHERE s.battery < :threshold ORDER BY s.battery ASC")
    List<Scooter> findByBatteryBelow(@Param("threshold") int threshold);

    /**
     * Consulta personalizada JPQL: busca patinetas cuya ubicación contenga
     * el texto dado, sin distinguir mayúsculas de minúsculas.
     * Facilita la búsqueda de flotas por zona geográfica.
     *
     * @param location texto parcial de ubicación a buscar
     * @return lista de patinetas cuya ubicación coincide con el patrón
     */
    @Query("SELECT s FROM Scooter s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Scooter> findByLocationContaining(@Param("location") String location);
}
