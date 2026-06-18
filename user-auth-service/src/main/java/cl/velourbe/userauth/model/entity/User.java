package cl.velourbe.userauth.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario registrado en el sistema.
 * Se mapea a la tabla {@code users} de la base de datos {@code db_scooter_users}.
 * Los roles válidos son {@code CLIENT} y {@code ADMIN}.
 */
@Data
@Entity
@Table(name = "users")
public class User {

    /** Identificador único autoincremental. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dirección de correo electrónico, usada como clave de autenticación. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Contraseña almacenada como hash BCrypt (factor 10). Nunca en texto plano. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Nombre completo del usuario para identificación visual. */
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /** Rol de autorización: "CLIENT" para usuarios normales, "ADMIN" para operadores. */
    @Column(nullable = false, length = 20)
    private String role;

    /** Fecha y hora en que se creó el registro. Se asigna automáticamente al instanciar. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Indica si el usuario puede autenticarse. Por defecto {@code true}. */
    @Column(nullable = false)
    private Boolean active = true;
}
