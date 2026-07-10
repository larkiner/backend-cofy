package com.cafeteria.api.interno.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoEmpleadoVistaRepository extends JpaRepository<PedidoEmpleadoVista, Long> {

    List<PedidoEmpleadoVista> findAllByOrderByFechaPedidoDesc();

    List<PedidoEmpleadoVista> findByEstadoOrderByFechaPedidoAsc(String estado);

    List<PedidoEmpleadoVista> findBySucursalIdOrderByFechaPedidoDesc(Long sucursalId);

    List<PedidoEmpleadoVista> findBySucursalIdAndEstadoOrderByFechaPedidoAsc(Long sucursalId, String estado);
}
