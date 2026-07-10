package com.cafeteria.api.interno.metrica;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/** Clave compuesta de la vista de métricas (sucursal + fecha). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaSucursalId implements Serializable {

    private Long sucursalId;
    private LocalDate fecha;
}
