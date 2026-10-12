package com.cafeteria.api.interno.pedido;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PendientesPagoSchedulerTests {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 12, 10, 0);

    @Test
    void cancelaLosPendientesMasAntiguosQueElLimiteConfigurado() {
        PedidoInternoRepository repo = mock(PedidoInternoRepository.class);
        when(repo.cancelarPendientesSinPago(AHORA.minusMinutes(30))).thenReturn(4);
        var scheduler = new PendientesPagoScheduler(repo, 30);

        int cancelados = scheduler.cancelarPendientesAnterioresA(AHORA);

        assertThat(cancelados).isEqualTo(4);
        verify(repo).cancelarPendientesSinPago(AHORA.minusMinutes(30));
    }

    @Test
    void conMinutosEnCeroElBarridoEstaDesactivado() {
        PedidoInternoRepository repo = mock(PedidoInternoRepository.class);
        var scheduler = new PendientesPagoScheduler(repo, 0);

        assertThat(scheduler.cancelarPendientesAnterioresA(AHORA)).isZero();
        verifyNoInteractions(repo);
    }

    @Test
    void conMinutosNegativosElBarridoEstaDesactivado() {
        PedidoInternoRepository repo = mock(PedidoInternoRepository.class);
        var scheduler = new PendientesPagoScheduler(repo, -5);

        assertThat(scheduler.cancelarPendientesAnterioresA(AHORA)).isZero();
        verifyNoInteractions(repo);
    }
}
