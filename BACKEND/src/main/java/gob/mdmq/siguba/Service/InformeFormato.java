package gob.mdmq.siguba.Service;

import gob.mdmq.siguba.dto.DtoInformeInspeccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

/**
 * Lo que comparten el informe en Word y en PDF: textos, fechas, colores y
 * fotos. Tenerlo en un solo sitio es lo que hace que ambos salgan iguales.
 */
final class InformeFormato {

    static final String AZUL = "1A5276";
    static final String CELESTE = "EAF4FC";
    static final String BORDE = "BFBFBF";
    static final String VACIO = "—";

    private static final Locale ES = Locale.forLanguageTag("es-EC");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", ES);

    /** Lado mayor de las fotos del anexo: suficiente para imprimir, ligero para enviar. */
    private static final int LADO_MAXIMO = 1400;

    private InformeFormato() {
    }

    static String valor(String s) {
        return s == null || s.isBlank() ? VACIO : s.trim();
    }

    /** "09:37 h – 28 de agosto de 2026", como en el modelo. */
    static String fechaHora(LocalDateTime f) {
        if (f == null) return VACIO;
        String dia = FECHA.format(f);
        return f.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                ? dia
                : HORA.format(f) + " h – " + dia;
    }

    static String fecha(LocalDate f) {
        return f == null ? VACIO : FECHA.format(f);
    }

    /** Un parrafo por linea escrita; se descartan las lineas en blanco. */
    static List<String> parrafos(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("\\R"))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .toList();
    }

    static boolean tieneItems(DtoInformeInspeccion.GrupoRecomendacion g) {
        return g.getItems() != null && g.getItems().stream().anyMatch(i -> i != null && !i.isBlank());
    }

    static String casilla(boolean marcada) {
        return marcada ? "[X]" : "[ ]";
    }

    static byte[] recurso(String nombre) {
        try (InputStream in = InformeFormato.class.getResourceAsStream("/informe/" + nombre)) {
            if (in == null) throw new IllegalStateException("Falta el recurso /informe/" + nombre);
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Foto lista para incrustar: JPEG, reducida, con sus medidas en pixeles. */
    record Imagen(byte[] jpeg, int ancho, int alto) {

        /** Medidas que caben en la caja dada sin deformar la foto. */
        double[] encajar(double anchoCaja, double altoCaja) {
            double escala = Math.min(anchoCaja / ancho, altoCaja / alto);
            return new double[]{ancho * escala, alto * escala};
        }
    }

    /**
     * Normaliza una foto para el informe. Devuelve null si no es una imagen
     * que Java sepa leer (p. ej. WEBP): el anexo muestra un aviso en su lugar.
     */
    static Imagen preparar(byte[] original) {

        if (original == null) return null;

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
            if (img == null) return null;

            double escala = Math.min(1.0, (double) LADO_MAXIMO / Math.max(img.getWidth(), img.getHeight()));
            int w = Math.max(1, (int) Math.round(img.getWidth() * escala));
            int h = Math.max(1, (int) Math.round(img.getHeight() * escala));

            // Lienzo RGB con fondo blanco: JPEG no admite transparencia.
            BufferedImage lienzo = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = lienzo.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.drawImage(img, 0, 0, w, h, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(lienzo, "jpg", out);
            return new Imagen(out.toByteArray(), w, h);

        } catch (IOException e) {
            return null;
        }
    }
}
