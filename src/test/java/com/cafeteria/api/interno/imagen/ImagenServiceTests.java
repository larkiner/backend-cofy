package com.cafeteria.api.interno.imagen;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validación de la subida de imágenes. No necesita Oracle ni Supabase: el
 * almacén es un doble que recuerda lo que le llega.
 */
class ImagenServiceTests {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

    private final List<String> guardados = new ArrayList<>();
    private final AlmacenImagenes almacen = (ruta, tipo, contenido) -> {
        guardados.add(ruta + "|" + tipo);
        return "https://cdn.test/" + ruta;
    };
    private final ImagenService servicio = new ImagenService(almacen);

    private static MockMultipartFile archivo(String nombre, String tipoDeclarado, byte[] bytes) {
        return new MockMultipartFile("archivo", nombre, tipoDeclarado, bytes);
    }

    @Test
    void guardaPngConNombreNuevoYDevuelveLaUrl() {
        String url = servicio.subirImagenProducto(archivo("foto-original.png", "image/png", PNG));

        assertThat(url).startsWith("https://cdn.test/productos/").endsWith(".png");
        assertThat(url).doesNotContain("foto-original");
        assertThat(guardados).singleElement().asString().endsWith("|image/png");
    }

    @Test
    void elTipoSaleDeLosBytesNoDelNombreNiDelContentType() {
        // Un JPG disfrazado de .png con Content-Type de PNG se guarda como lo que es.
        String url = servicio.subirImagenProducto(archivo("engano.png", "image/png", JPG));

        assertThat(url).endsWith(".jpg");
        assertThat(guardados).singleElement().asString().endsWith("|image/jpeg");
    }

    @Test
    void aceptaWebp() {
        assertThat(servicio.subirImagenProducto(archivo("a.webp", "image/webp", WEBP))).endsWith(".webp");
    }

    @Test
    void rechazaArchivosQueNoSonImagen() {
        byte[] html = "<script>alert(1)</script>".getBytes();
        assertThatThrownBy(() -> servicio.subirImagenProducto(archivo("x.png", "image/png", html)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("415");
        assertThat(guardados).isEmpty();
    }

    @Test
    void rechazaArchivoVacioONulo() {
        assertThatThrownBy(() -> servicio.subirImagenProducto(archivo("x.png", "image/png", new byte[0])))
                .hasMessageContaining("400");
        assertThatThrownBy(() -> servicio.subirImagenProducto(null)).hasMessageContaining("400");
    }

    @Test
    void rechazaImagenesDeMasDe5Mb() {
        byte[] grande = new byte[(int) ImagenService.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);
        assertThatThrownBy(() -> servicio.subirImagenProducto(archivo("g.png", "image/png", grande)))
                .hasMessageContaining("413");
        assertThat(guardados).isEmpty();
    }
}
