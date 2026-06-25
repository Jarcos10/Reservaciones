package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rooms")
@Schema(
        name = "Cuarto",
        description = "Entidad principal que representa una habitación física del hotel. Contiene su número, tipo, precio por noche, camas disponibles y estado de disponibilidad."
)
public class Cuarto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Schema(description = "Identificador interno de la habitación.", example = "1")
    private Long id;

    @Column(name = "tipo", length = 50, nullable = false)
    @Schema(description = "Categoría comercial de la habitación.", example = "Habitación doble")
    private String tipo;

    @Column(nullable = false, unique = true)
    @Schema(description = "Número visible de la habitación dentro del hotel.", example = "204")
    private int numero;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Precio por noche de la habitación.", example = "1450.00")
    private BigDecimal precio;

    @Column(name = "numeroCamas", nullable = false)
    @Schema(description = "Número de camas disponibles en la habitación.", example = "2")
    private int numeroCamas;

    @Column(name = "disponible", nullable = false)
    @Schema(description = "Disponibilidad administrativa del cuarto. Si es false, no debe ofrecerse para nuevas reservaciones.", example = "true")
    private boolean disponible;

    @JsonIgnore
    @OneToMany(mappedBy = "cuarto")
    @Schema(description = "Reservaciones asociadas al cuarto. Se oculta en respuestas para evitar ciclos de serialización.", hidden = true)
    private List<Reservacion> reservaciones = new ArrayList<>();
}
