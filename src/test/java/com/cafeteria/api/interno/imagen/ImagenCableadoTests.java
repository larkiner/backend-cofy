package com.cafeteria.api.interno.imagen;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Los beans de la subida deben poder crearse SOLOS, como en la API real, sin
 * depender de beans que Spring Boot no registre (así pasó con RestClient.Builder
 * y la API no arrancaba). No necesita Oracle ni Supabase.
 */
class ImagenCableadoTests {

    @Test
    void elServicioYElAdaptadorSeCreanSinConfiguracionExtra() {
        try (var contexto = new AnnotationConfigApplicationContext(
                SupabaseAlmacenImagenes.class, ImagenService.class, ImagenAdminController.class)) {
            assertThat(contexto.getBean(AlmacenImagenes.class)).isInstanceOf(SupabaseAlmacenImagenes.class);
            assertThat(contexto.getBean(ImagenAdminController.class)).isNotNull();
        }
    }
}
