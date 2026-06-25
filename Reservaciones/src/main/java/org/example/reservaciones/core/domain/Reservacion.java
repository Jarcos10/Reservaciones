package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservaciones")
@Schema(
        name = "Reservacion",
        description = "Entidad principal que registra una estancia solicitada por un cliente, incluyendo fechas, datos de contacto, cuarto asignado, importes calculados y estado operativo."
)
public class Reservacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservacion")
    @Schema(description = "Identificador interno de la reservación.", example = "1")
    private Long idReservacion;

    @Column(name = "nombre_cliente", nullable = false, length = 120)
    @Schema(description = "Nombre completo del cliente responsable de la reservación.", example = "María Fernanda López")
    private String nombreCliente;

    @Column(name = "correo_cliente", length = 120)
    @Schema(description = "Correo electrónico del cliente.", example = "maria.lopez@example.com")
    private String correoCliente;

    @Column(name = "telefono_cliente", length = 30)
    @Schema(description = "Teléfono de contacto del cliente.", example = "5551234567")
    private String telefonoCliente;

    @Column(name = "fecha_entrada", nullable = false)
    @Schema(description = "Fecha de entrada al hotel.", example = "2026-07-10")
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    @Schema(description = "Fecha de salida del hotel.", example = "2026-07-13")
    private LocalDate fechaSalida;

    @Column(name = "numero_huespedes", nullable = false)
    @Schema(description = "Número de huéspedes de la estancia.", example = "2")
    private int numeroHuespedes;

    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Precio por noche tomado del cuarto al crear la reservación.", example = "1450.00")
    private BigDecimal precioNoche;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Total calculado de la reservación.", example = "4350.00")
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Schema(description = "Estado actual de la reservación.", example = "PENDIENTE", implementation = EstadoReservacion.class)
    private EstadoReservacion estado;

    @Column(name = "observaciones", length = 500)
    @Schema(description = "Observaciones o solicitudes especiales del cliente.", example = "Llegada aproximada a las 18:00 horas.")
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    @Schema(description = "Fecha y hora de creación del registro.", example = "2026-06-24T17:35:20")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @Schema(description = "Fecha y hora de la última actualización del registro.", example = "2026-06-24T18:10:00")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuarto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservaciones"})
    @Schema(description = "Cuarto asignado a la reservación.", implementation = Cuarto.class)
    private Cuarto cuarto;

    @JsonIgnore
    @OneToMany(mappedBy = "reservacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Archivos asociados a la reservación. Se oculta en respuestas para evitar exponer rutas internas.", hidden = true)
    private List<Archivo> archivos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();

        if (this.estado == null) {
            this.estado = EstadoReservacion.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
