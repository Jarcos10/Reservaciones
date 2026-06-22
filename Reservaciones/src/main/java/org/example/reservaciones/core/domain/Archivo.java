package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Archivo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idArchivo")
    private Long idArchivo;

    @Column(name = "nombreArchivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "nombre_guardado")
    private String nombreGuardado;

    @Column(name = "tipoArchivo", nullable = false)
    private String tipoArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    private TipoDocumento tipoDocumento;

    @Column(name = "ruta_archivo", length = 700)
    private String rutaArchivo;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reservacion")
    @JsonIgnore
    private Reservacion reservacion;
}
