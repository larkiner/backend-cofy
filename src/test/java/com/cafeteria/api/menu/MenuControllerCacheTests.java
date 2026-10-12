package com.cafeteria.api.menu;

import com.cafeteria.api.config.CatalogoCacheControl;
import com.cafeteria.api.sucursal.SucursalActiva;
import com.cafeteria.api.sucursal.SucursalController;
import com.cafeteria.api.sucursal.SucursalService;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El catálogo público debe salir con Cache-Control explícito (si no, Spring
 * Security lo marca no-store). No necesita Oracle: servicios simulados.
 */
class MenuControllerCacheTests {

    private final CatalogoCacheControl cacheControl = new CatalogoCacheControl(60);

    @Test
    void menuCompletoSeSirveConCacheControlPublico() throws Exception {
        MenuService menuService = mock(MenuService.class);
        when(menuService.obtenerMenu()).thenReturn(List.of());
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new MenuController(menuService, cacheControl)).build();

        mvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60, public"));
    }

    @Test
    void menuPorCategoriaSeSirveConCacheControlPublico() throws Exception {
        MenuService menuService = mock(MenuService.class);
        when(menuService.obtenerMenuPorCategoria(1L)).thenReturn(List.of());
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new MenuController(menuService, cacheControl)).build();

        mvc.perform(get("/api/menu/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60, public"));
    }

    @Test
    void sucursalesSeSirvenConCacheControlPublico() throws Exception {
        SucursalService sucursalService = mock(SucursalService.class);
        when(sucursalService.listarActivas()).thenReturn(List.<SucursalActiva>of());
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new SucursalController(sucursalService, cacheControl)).build();

        mvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60, public"));
    }

    @Test
    void lasCabecerasPorDefectoDeSpringSecurityNoPisanElCacheControl() throws Exception {
        MenuService menuService = mock(MenuService.class);
        when(menuService.obtenerMenu()).thenReturn(List.of());
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new MenuController(menuService, cacheControl))
                .addFilters(new HeaderWriterFilter(List.of(new CacheControlHeadersWriter())))
                .build();

        mvc.perform(get("/api/menu"))
                .andExpect(header().string("Cache-Control", "max-age=60, public"));
    }
}
