package org.example.reservaciones.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Cuarto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "numeroCamas", nullable = false)
    private int numeroCamas;

    @Column(name = "disponible", nullable = false)
    private boolean disponible;

    @JsonIgnore
    @OneToMany(mappedBy = "cuarto")
    private List<Reservacion> reservaciones = new ArrayList<>();
}
