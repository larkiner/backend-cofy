package com.cafeteria.api.interno.imagen;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** El adaptador de Supabase se prueba contra un servidor simulado: sin red ni claves reales. */
class SupabaseAlmacenImagenesTests {

    private static final String URL = "https://proyecto.supabase.co";
    private static final byte[] BYTES = {1, 2, 3};

    @Test
    void subeAlBucketConLaClaveYDevuelveLaUrlPublica() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(URL + "/storage/v1/object/imagenes/productos/a.png"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "sb_secret_demo"))
                .andExpect(header("x-upsert", "false"))
                .andExpect(header("Content-Type", "image/png"))
                .andRespond(withSuccess());
        SupabaseAlmacenImagenes almacen =
                new SupabaseAlmacenImagenes(builder, URL + "/", "sb_secret_demo", "imagenes");

        String url = almacen.guardar("productos/a.png", "image/png", BYTES);

        assertThat(url).isEqualTo(URL + "/storage/v1/object/public/imagenes/productos/a.png");
        servidor.verify();
    }

    @Test
    void unaClaveJwtTambienVaComoBearer() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(URL + "/storage/v1/object/imagenes/productos/a.jpg"))
                .andExpect(header("Authorization", "Bearer eyJabc.def.ghi"))
                .andRespond(withSuccess());
        new SupabaseAlmacenImagenes(builder, URL, "eyJabc.def.ghi", "imagenes")
                .guardar("productos/a.jpg", "image/jpeg", BYTES);
        servidor.verify();
    }

    @Test
    void siSupabaseFallaResponde502SinFiltrarDetalles() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(URL + "/storage/v1/object/imagenes/productos/a.png"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"clave\":\"sb_secret_demo\"}"));
        SupabaseAlmacenImagenes almacen =
                new SupabaseAlmacenImagenes(builder, URL, "sb_secret_demo", "imagenes");

        assertThatThrownBy(() -> almacen.guardar("productos/a.png", "image/png", BYTES))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(502);
                    assertThat(e.getReason()).doesNotContain("sb_secret_demo");
                });
    }

    @Test
    void sinConfiguracionResponde503() {
        SupabaseAlmacenImagenes almacen = new SupabaseAlmacenImagenes(RestClient.builder(), "", "", "imagenes");

        assertThatThrownBy(() -> almacen.guardar("productos/a.png", "image/png", BYTES))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
    }
}
