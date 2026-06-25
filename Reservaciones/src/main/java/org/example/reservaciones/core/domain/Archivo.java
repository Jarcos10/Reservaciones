package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "archivos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_archivo_reservacion_tipo_documento",
                        columnNames = {"id_reservacion", "tipo_documento"}
                )
        }
)
@Schema(
        name = "Archivo",
        description = "Entidad que almacena los metadatos de un archivo relacionado con una reservación. El contenido del archivo se guarda en el servidor y no como bytes dentro de la base de datos."
)
public class Archivo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idArchivo")
    @Schema(description = "Identificador interno del archivo.", example = "1")
    private Long idArchivo;

    @Column(name = "nombreArchivo", nullable = false)
    @Schema(description = "Nombre original del archivo cargado por el usuario.", example = "identificacion_cliente.pdf")
    private String nombreArchivo;

    @Column(name = "nombre_guardado")
    @Schema(description = "Nombre físico asignado por el sistema para guardar el archivo en el servidor.", example = "identificacion-1-550e8400-e29b-41d4-a716-446655440000.pdf")
    private String nombreGuardado;

    @Column(name = "tipoArchivo", nullable = false)
    @Schema(description = "Tipo MIME del archivo.", example = "application/pdf")
    private String tipoArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    @Schema(description = "Clasificación funcional del documento.", example = "IDENTIFICACION", implementation = TipoDocumento.class)
    private TipoDocumento tipoDocumento;

    @Column(name = "ruta_archivo", length = 700)
    @Schema(description = "Ruta interna del archivo dentro del servidor. No debe exponerse a usuarios finales.", example = "uploads/identificaciones/reservacion-1/identificacion-1.pdf")
    private String rutaArchivo;

    @Column(name = "tamano_bytes")
    @Schema(description = "Tamaño del archivo en bytes.", example = "245760")
    private Long tamanoBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reservacion")
    @JsonIgnore
    @Schema(description = "Reservación a la que pertenece el archivo. Se oculta para evitar ciclos de serialización.", hidden = true)
    private Reservacion reservacion;
}
