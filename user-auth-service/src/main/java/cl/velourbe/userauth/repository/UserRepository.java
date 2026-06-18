package cl.velourbe.userauth.repository;

import cl.velourbe.userauth.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 * Extiende {@link JpaRepository} para operaciones CRUD estándar y define
 * métodos derivados y consultas JPQL personalizadas.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email dado.
     *
     * @param email dirección de correo a verificar
     * @return true si el email está en uso, false si está disponible
     */
    boolean existsByEmail(String email);

    /**
     * Consulta personalizada JPQL: retorna todos los usuarios activos con el rol indicado.
     * Útil para auditorías y paneles de administración segmentados por rol.
     *
     * @param role nombre del rol a filtrar (ej. "CLIENT" o "ADMIN")
     * @return lista de usuarios activos que coinciden con el rol
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveByRole(@Param("role") String role);
}
