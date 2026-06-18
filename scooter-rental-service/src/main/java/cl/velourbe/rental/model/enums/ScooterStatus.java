package cl.velourbe.rental.model.enums;

/**
 * Estados posibles de una patineta en el sistema.
 * <ul>
 *   <li>{@code AVAILABLE} — libre para arrendar</li>
 *   <li>{@code IN_USE} — actualmente arrendada por un usuario</li>
 *   <li>{@code MAINTENANCE} — fuera de servicio por mantenimiento</li>
 * </ul>
 */
public enum ScooterStatus { AVAILABLE, IN_USE, MAINTENANCE }
