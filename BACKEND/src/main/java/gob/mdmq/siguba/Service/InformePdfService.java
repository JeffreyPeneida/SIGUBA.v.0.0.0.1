package gob.mdmq.siguba.Service;

import static gob.mdmq.siguba.Service.InformeFormato.*;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import gob.mdmq.siguba.Service.InformeInspeccionService.InformeExportable;
import gob.mdmq.siguba.dto.DtoInformeInspeccion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.DetalleEspecie;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.GrupoRecomendacion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.Participante;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Informe de inspeccion en PDF, con la maqueta del modelo oficial: franja
 * azul a la izquierda, logos arriba y pie institucional en cada pagina.
 *
 * Se arma un XHTML y lo convierte openhtmltopdf; por eso todo texto pasa por
 * esc() y las etiquetas van cerradas.
 */
@Service
public class InformePdfService {

    /** Caja de cada foto del anexo, en puntos (dos por fila). */
    private static final double FOTO_ANCHO = 215;
    private static final double FOTO_ALTO = 165;

    private static final String CSS = """
        @page {
            size: A4;
            margin: 78pt 50pt 70pt 80pt;
            @top-center { content: element(encabezado); }
            @bottom-left { content: element(pie-izq); }
            @bottom-right { content: element(pie-der); }
            background-image: url("data:image/png;base64,%4$s");
            background-repeat: repeat-y;
            background-position: 0 0;
            background-size: 34pt 842pt;
        }
        #encabezado { position: running(encabezado); text-align: center; padding-top: 8pt; }
        #pie-izq { position: running(pie-izq); }
        #pie-der { position: running(pie-der); }
        body { font-family: Helvetica, Arial, sans-serif; font-size: 9.5pt; color: #000; }
        .seccion { background: #%1$s; color: #fff; font-weight: bold; font-size: 10pt;
                   padding: 2pt 5pt; margin: 12pt 0 4pt 0; page-break-after: avoid; }
        .subtitulo { color: #%1$s; font-weight: bold; margin: 10pt 0 4pt 0; page-break-after: avoid; }
        table { width: 100%%; border-collapse: collapse; }
        td, th { border: 0.75pt solid #%3$s; padding: 5pt 6pt; vertical-align: middle; }
        tr { page-break-inside: avoid; }
        td.etq { background: #%2$s; color: #%1$s; font-weight: bold; }
        th { background: #%1$s; color: #fff; font-weight: bold; text-align: center; }
        p { margin: 0 0 5pt 0; text-align: justify; }
        ul { margin: 0 0 6pt 0; padding-left: 22pt; }
        li { margin-bottom: 3pt; text-align: justify; }
        .entidad { font-weight: bold; margin: 6pt 0 3pt 0; page-break-after: avoid; }
        .firma { height: 70pt; }
        .bloque-firmas { page-break-inside: avoid; }
        .nota { color: #555; font-size: 8.5pt; margin-top: 4pt; }
        table.fotos td { vertical-align: top; width: 50%%; }
        td.foto { text-align: center; height: 175pt; vertical-align: middle; }
        .sin-foto { color: #888; font-style: italic; }
        """.formatted(AZUL, CELESTE, BORDE, Base64.getEncoder().encodeToString(recurso("franja.png")));

    public byte[] generar(InformeExportable exportable) {

        String html = html(exportable.informe(), exportable.fotos());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del informe", e);
        }
    }

    // ------------------------------------------------------------------ documento

    private String html(DtoInformeInspeccion d, List<byte[]> fotos) {

        StringBuilder h = new StringBuilder(32_000);

        h.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
         .append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\"><head><meta charset=\"UTF-8\"/>")
         .append("<title>Informe de inspección ").append(esc(valor(d.getCodigoTramite()))).append("</title>")
         .append("<style>").append(CSS).append("</style></head><body>");

        h.append("<div id=\"encabezado\">").append(img("encabezado.png", 181, 39)).append("</div>")
         .append("<div id=\"pie-izq\">").append(img("pie-direccion.png", 152, 17)).append("</div>")
         .append("<div id=\"pie-der\">").append(img("pie-quito-renace.png", 101, 22)).append("</div>");

        // 1. Antecedentes
        seccion(h, "1. ANTECEDENTES");
        h.append("<table>");
        fila4(h, "Documento Atendido", d.getDocumentoAtendido(), "Tipo de Inspección", d.getTipoInspeccion());
        fila4(h, "Fecha y Hora", fechaHora(d.getFechaInspeccion()), "Plagas", d.getPlagas());
        fila4(h, "Nivel", d.getNivel(), "Asunto", d.getAsunto());
        h.append("</table>");

        // 2. Informacion general del lugar
        seccion(h, "2. INFORMACIÓN GENERAL DEL LUGAR");
        h.append("<table>");
        fila2(h, "Dirección Inspeccionada", d.getDireccion());
        fila2(h, "Sector / Barrio", d.getSectorBarrio());
        fila2(h, "Administración Zonal", d.getAdminZonal());
        fila2(h, "Tipo de Lugar", d.getTipoLugar());
        fila2(h, "Áreas Supervisadas", d.getAreasSupervisadas());
        fila2(h, "Coordenadas GPS", d.getCoordenadas());
        fila2(h, "N Beneficiarios", d.getBeneficiarios());
        fila2(h, "Persona Contactada", d.getPersonaContactada());
        h.append("</table>");

        h.append("<div class=\"subtitulo\">b. Personas que participan en la inspección:</div>");
        h.append("<table><tr><th style=\"width:40%\">Nombre</th><th style=\"width:35%\">Cargo</th><th>Institución</th></tr>");
        if (d.getParticipantes().isEmpty()) {
            h.append("<tr><td colspan=\"3\">").append(VACIO).append("</td></tr>");
        }
        for (Participante p : d.getParticipantes()) {
            h.append("<tr><td>").append(esc(valor(p.getNombre()))).append("</td><td>")
             .append(esc(valor(p.getCargo()))).append("</td><td>")
             .append(esc(valor(p.getInstitucion()))).append("</td></tr>");
        }
        h.append("</table>");

        // 3. Detalle de la inspeccion
        seccion(h, "3. DETALLE DE LA INSPECCIÓN");
        h.append("<table><tr><th style=\"width:28%\">Especie</th><th style=\"width:24%\">Nivel de Infestación</th><th>Indicios / Observaciones</th></tr>");
        if (d.getDetalleEspecies().isEmpty()) {
            h.append("<tr><td colspan=\"3\">").append(VACIO).append("</td></tr>");
        }
        for (DetalleEspecie e : d.getDetalleEspecies()) {
            h.append("<tr><td>").append(esc(valor(e.getEspecie()))).append("</td><td>")
             .append(esc(valor(e.getNivel()))).append("</td><td>")
             .append(esc(valor(e.getIndicios()))).append("</td></tr>");
        }
        h.append("</table>");

        // 4. Diagnostico
        seccion(h, "4. DIAGNÓSTICO DE SITUACIÓN");
        if (d.getFactoresRiesgo() != null && !d.getFactoresRiesgo().isBlank()) {
            h.append("<p><b>Factores de riesgo identificados:</b> ")
             .append(esc(d.getFactoresRiesgo().trim())).append("</p>");
        }
        parrafos(h, d.getDiagnostico());

        // 5. Programa de actuacion
        seccion(h, "5. PROGRAMA DE ACTUACIÓN");
        h.append("<p><b>Tipo de control: ")
         .append(casilla("ACTIVO".equals(d.getTipoControl()))).append(" Control Activo ")
         .append(casilla("PASIVO".equals(d.getTipoControl()))).append(" Control Pasivo</b></p>");
        parrafos(h, d.getProgramaActuacion());

        // 6. Conclusiones
        seccion(h, "6. CONCLUSIONES");
        parrafos(h, d.getConclusiones());

        // 7. Recomendaciones
        seccion(h, "7. RECOMENDACIONES");
        boolean alguna = false;
        for (GrupoRecomendacion g : d.getRecomendaciones()) {
            if (!tieneItems(g)) continue;
            alguna = true;
            h.append("<div class=\"entidad\">").append(esc(g.getEntidad())).append(":</div><ul>");
            for (String item : g.getItems()) {
                if (item != null && !item.isBlank()) {
                    h.append("<li>").append(esc(item.trim())).append("</li>");
                }
            }
            h.append("</ul>");
        }
        if (!alguna) h.append("<p>").append(VACIO).append("</p>");

        // 8. Firmas
        // Titulo, cuadro y fecha juntos: una firma partida entre paginas no vale.
        h.append("<div class=\"bloque-firmas\">");
        seccion(h, "8. FIRMAS DE RESPONSABILIDAD");
        h.append("<table>")
         .append("<tr><td class=\"etq\" style=\"width:50%\">Elaborado por</td><td class=\"etq\">Revisado y aprobado por</td></tr>")
         .append("<tr><td>").append(esc(valor(d.getElaboradoNombre()))).append("</td><td>")
         .append(esc(valor(d.getRevisadoNombre()))).append("</td></tr>")
         .append("<tr><td>").append(esc(valor(d.getElaboradoCargo()))).append("</td><td>")
         .append(esc(valor(d.getRevisadoCargo()))).append("</td></tr>")
         .append("<tr><td class=\"firma\"></td><td class=\"firma\"></td></tr>")
         .append("</table>")
         .append("<div class=\"nota\">Fecha de elaboración: ").append(esc(fecha(d.getFechaElaboracion()))).append("</div></div>");

        // 9. Anexo fotografico
        if (!fotos.isEmpty()) {
            seccion(h, "9. ANEXO FOTOGRÁFICO");
            anexo(h, d, fotos);
        }

        h.append("</body></html>");
        return h.toString();
    }

    private void anexo(StringBuilder h, DtoInformeInspeccion d, List<byte[]> fotos) {

        h.append("<table class=\"fotos\">");

        for (int i = 0; i < fotos.size(); i += 2) {
            StringBuilder imagenes = new StringBuilder("<tr>");
            StringBuilder pies = new StringBuilder("<tr>");

            for (int j = i; j < i + 2; j++) {
                if (j >= fotos.size()) {
                    imagenes.append("<td></td>");
                    pies.append("<td></td>");
                    continue;
                }
                Imagen img = preparar(fotos.get(j));
                imagenes.append("<td class=\"foto\">");
                if (img == null) {
                    imagenes.append("<span class=\"sin-foto\">Imagen no disponible</span>");
                } else {
                    double[] m = img.encajar(FOTO_ANCHO, FOTO_ALTO);
                    imagenes.append("<img src=\"data:image/jpeg;base64,")
                            .append(Base64.getEncoder().encodeToString(img.jpeg()))
                            .append("\" style=\"width:").append(pt(m[0]))
                            .append(";height:").append(pt(m[1])).append("\"/>");
                }
                imagenes.append("</td>");

                pies.append("<td><b>FOTOGRAFÍA N° ").append(j + 1).append(".</b><br/>")
                    .append("<b>Sitio de Inspección:</b> ")
                    .append(esc(valor(d.getFotos().get(j).getDescripcion())))
                    .append("</td>");
            }

            // Foto y pie en el mismo bloque: que no queden en paginas distintas.
            h.append("<tbody style=\"page-break-inside: avoid\">")
             .append(imagenes).append("</tr>").append(pies).append("</tr></tbody>");
        }

        h.append("</table>");
    }

    // ------------------------------------------------------------------ piezas

    private static void seccion(StringBuilder h, String titulo) {
        h.append("<div class=\"seccion\">").append(esc(titulo)).append("</div>");
    }

    private static void fila2(StringBuilder h, String etiqueta, String valor) {
        h.append("<tr><td class=\"etq\" style=\"width:35%\">").append(esc(etiqueta))
         .append("</td><td>").append(esc(valor(valor))).append("</td></tr>");
    }

    private static void fila4(StringBuilder h, String e1, String v1, String e2, String v2) {
        h.append("<tr><td class=\"etq\" style=\"width:24%\">").append(esc(e1)).append("</td><td style=\"width:26%\">")
         .append(esc(valor(v1))).append("</td><td class=\"etq\" style=\"width:24%\">").append(esc(e2))
         .append("</td><td>").append(esc(valor(v2))).append("</td></tr>");
    }

    private static void parrafos(StringBuilder h, String texto) {
        List<String> ps = InformeFormato.parrafos(texto);
        if (ps.isEmpty()) {
            h.append("<p>").append(VACIO).append("</p>");
        }
        ps.forEach(p -> h.append("<p>").append(esc(p)).append("</p>"));
    }

    private static String img(String recurso, double ancho, double alto) {
        return "<img src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(recurso(recurso))
                + "\" style=\"width:" + pt(ancho) + ";height:" + pt(alto) + "\"/>";
    }

    private static String pt(double v) {
        return String.format(java.util.Locale.ROOT, "%.1fpt", v);
    }

    /** Escapado XML: el texto lo escribe el usuario y el conversor exige XHTML valido. */
    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder r = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> r.append("&amp;");
                case '<' -> r.append("&lt;");
                case '>' -> r.append("&gt;");
                case '"' -> r.append("&quot;");
                case '\'' -> r.append("&#39;");
                default -> {
                    // Caracteres de control no validos en XML (salvo tab y saltos).
                    if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') continue;
                    r.append(c);
                }
            }
        }
        return r.toString();
    }
}
