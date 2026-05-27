package cl.velourbe.rental.model.enums;

/**
 * Estados posibles de un arriendo durante su ciclo de vida.
 * <ul>
 *   <li>{@code ACTIVE} — el arriendo está en curso</li>
 *   <li>{@code COMPLETED} — finalizado correctamente con duración calculada</li>
 *   <li>{@code CANCELLED} — cancelado antes de finalizar</li>
 * </ul>
 */
public enum RentalStatus { ACTIVE, COMPLETED, CANCELLED }
