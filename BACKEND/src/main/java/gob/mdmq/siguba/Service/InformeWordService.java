package gob.mdmq.siguba.Service;

import static gob.mdmq.siguba.Service.InformeFormato.*;

import gob.mdmq.siguba.Service.InformeInspeccionService.InformeExportable;
import gob.mdmq.siguba.dto.DtoInformeInspeccion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.DetalleEspecie;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.GrupoRecomendacion;
import gob.mdmq.siguba.dto.DtoInformeInspeccion.Participante;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTPosH;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTPosV;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromH;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromV;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

/**
 * Informe de inspeccion en Word (.docx), editable, con la misma maqueta y el
 * mismo orden de secciones que el PDF.
 *
 * Sirve para lo que el formulario no cubre: retocar la redaccion, agregar
 * algo puntual o firmarlo electronicamente despues con FirmaEC.
 */
@Service
public class InformeWordService {

    private static final String FUENTE = "Arial";
    private static final int TAMANO = 10;

    // Medidas en twips (1 pt = 20 twips). A4 con los margenes del modelo.
    private static final int PAGINA_ANCHO = 11906;
    private static final int PAGINA_ALTO = 16838;
    private static final int MARGEN_IZQ = 1600;
    private static final int MARGEN_DER = 1000;
    private static final int ANCHO_UTIL = PAGINA_ANCHO - MARGEN_IZQ - MARGEN_DER;

    private static final double FOTO_ANCHO = 215;
    private static final double FOTO_ALTO = 165;

    public byte[] generar(InformeExportable exportable) {

        DtoInformeInspeccion d = exportable.informe();

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            pagina(doc);
            encabezadoYPie(doc);

            // 1. Antecedentes
            seccion(doc, "1. ANTECEDENTES");
            XWPFTable t = tabla(doc, 24, 26, 24, 26);
            fila4(t, 0, "Documento Atendido", d.getDocumentoAtendido(), "Tipo de Inspección", d.getTipoInspeccion());
            fila4(t, -1, "Fecha y Hora", fechaHora(d.getFechaInspeccion()), "Plagas", d.getPlagas());
            fila4(t, -1, "Nivel", d.getNivel(), "Asunto", d.getAsunto());

            // 2. Informacion general del lugar
            seccion(doc, "2. INFORMACIÓN GENERAL DEL LUGAR");
            t = tabla(doc, 35, 65);
            fila2(t, 0, "Dirección Inspeccionada", d.getDireccion());
            fila2(t, -1, "Sector / Barrio", d.getSectorBarrio());
            fila2(t, -1, "Administración Zonal", d.getAdminZonal());
            fila2(t, -1, "Tipo de Lugar", d.getTipoLugar());
            fila2(t, -1, "Áreas Supervisadas", d.getAreasSupervisadas());
            fila2(t, -1, "Coordenadas GPS", d.getCoordenadas());
            fila2(t, -1, "N Beneficiarios", d.getBeneficiarios());
            fila2(t, -1, "Persona Contactada", d.getPersonaContactada());

            subtitulo(doc, "b. Personas que participan en la inspección:");
            t = tabla(doc, 40, 35, 25);
            cabecera(t, "Nombre", "Cargo", "Institución");
            if (d.getParticipantes().isEmpty()) filaDatos(t, VACIO, "", "");
            for (Participante p : d.getParticipantes()) {
                filaDatos(t, valor(p.getNombre()), valor(p.getCargo()), valor(p.getInstitucion()));
            }

            // 3. Detalle de la inspeccion
            seccion(doc, "3. DETALLE DE LA INSPECCIÓN");
            t = tabla(doc, 28, 24, 48);
            cabecera(t, "Especie", "Nivel de Infestación", "Indicios / Observaciones");
            if (d.getDetalleEspecies().isEmpty()) filaDatos(t, VACIO, "", "");
            for (DetalleEspecie e : d.getDetalleEspecies()) {
                filaDatos(t, valor(e.getEspecie()), valor(e.getNivel()), valor(e.getIndicios()));
            }

            // 4. Diagnostico
            seccion(doc, "4. DIAGNÓSTICO DE SITUACIÓN");
            if (d.getFactoresRiesgo() != null && !d.getFactoresRiesgo().isBlank()) {
                XWPFParagraph p = parrafo(doc);
                texto(p, "Factores de riesgo identificados: ", true, null);
                texto(p, d.getFactoresRiesgo().trim(), false, null);
            }
            parrafos(doc, d.getDiagnostico());

            // 5. Programa de actuacion
            seccion(doc, "5. PROGRAMA DE ACTUACIÓN");
            texto(parrafo(doc), "Tipo de control: "
                    + casilla("ACTIVO".equals(d.getTipoControl())) + " Control Activo "
                    + casilla("PASIVO".equals(d.getTipoControl())) + " Control Pasivo", true, null);
            parrafos(doc, d.getProgramaActuacion());

            // 6. Conclusiones
            seccion(doc, "6. CONCLUSIONES");
            parrafos(doc, d.getConclusiones());

            // 7. Recomendaciones
            seccion(doc, "7. RECOMENDACIONES");
            boolean alguna = false;
            for (GrupoRecomendacion g : d.getRecomendaciones()) {
                if (!tieneItems(g)) continue;
                alguna = true;
                XWPFParagraph titulo = parrafo(doc);
                titulo.setKeepNext(true);
                texto(titulo, g.getEntidad() + ":", true, null);
                for (String item : g.getItems()) {
                    if (item != null && !item.isBlank()) vineta(doc, item.trim());
                }
            }
            if (!alguna) texto(parrafo(doc), VACIO, false, null);

            // 8. Firmas
            seccion(doc, "8. FIRMAS DE RESPONSABILIDAD");
            t = tabla(doc, 50, 50);
            celda(t.getRow(0).getCell(0), "Elaborado por", true, AZUL, CELESTE);
            celda(t.getRow(0).getCell(1), "Revisado y aprobado por", true, AZUL, CELESTE);
            filaDatos(t, valor(d.getElaboradoNombre()), valor(d.getRevisadoNombre()));
            filaDatos(t, valor(d.getElaboradoCargo()), valor(d.getRevisadoCargo()));
            XWPFTableRow espacioFirma = t.createRow();
            espacioFirma.setHeight(1400);
            texto(parrafo(doc), "Fecha de elaboración: " + fecha(d.getFechaElaboracion()), false, "555555")
                    .setFontSize(9);

            // 9. Anexo fotografico
            if (!exportable.fotos().isEmpty()) {
                seccion(doc, "9. ANEXO FOTOGRÁFICO");
                anexo(doc, d, exportable.fotos());
            }

            doc.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Word del informe", e);
        }
    }

    // ------------------------------------------------------------------ pagina

    private void pagina(XWPFDocument doc) {
        CTSectPr sect = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr()
                : doc.getDocument().getBody().addNewSectPr();

        CTPageSz tam = sect.isSetPgSz() ? sect.getPgSz() : sect.addNewPgSz();
        tam.setW(BigInteger.valueOf(PAGINA_ANCHO));
        tam.setH(BigInteger.valueOf(PAGINA_ALTO));

        CTPageMar m = sect.isSetPgMar() ? sect.getPgMar() : sect.addNewPgMar();
        m.setTop(BigInteger.valueOf(1560));
        m.setBottom(BigInteger.valueOf(1400));
        m.setLeft(BigInteger.valueOf(MARGEN_IZQ));
        m.setRight(BigInteger.valueOf(MARGEN_DER));
        m.setHeader(BigInteger.valueOf(300));
        m.setFooter(BigInteger.valueOf(400));
    }

    private void encabezadoYPie(XWPFDocument doc) throws Exception {

        XWPFHeader encabezado = doc.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph p = encabezado.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        imagen(p.createRun(), recurso("encabezado.png"), PictureType.PNG, "encabezado.png", 181, 39);

        // La franja azul va anclada a la pagina y detras del texto: al estar en
        // el encabezado se repite en todas las hojas, como en el modelo.
        XWPFRun franja = p.createRun();
        imagen(franja, recurso("franja.png"), PictureType.PNG, "franja.png", 34, 842);
        anclarAlBorde(franja);

        XWPFFooter pie = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph pp = pie.createParagraph();
        CTTabStop tab = (pp.getCTP().isSetPPr() ? pp.getCTP().getPPr() : pp.getCTP().addNewPPr())
                .addNewTabs().addNewTab();
        tab.setVal(STTabJc.RIGHT);
        tab.setPos(BigInteger.valueOf(ANCHO_UTIL));
        imagen(pp.createRun(), recurso("pie-direccion.png"), PictureType.PNG, "pie-direccion.png", 152, 17);
        XWPFRun r = pp.createRun();
        r.addTab();
        imagen(r, recurso("pie-quito-renace.png"), PictureType.PNG, "pie-quito-renace.png", 101, 22);
    }

    /** Convierte la imagen en linea del run en una anclada en la esquina de la pagina. */
    private void anclarAlBorde(XWPFRun run) throws Exception {

        CTDrawing dibujo = run.getCTR().getDrawingArray(0);
        CTInline enLinea = dibujo.getInlineArray(0);

        CTAnchor ancla = dibujo.addNewAnchor();
        ancla.setDistT(0L);
        ancla.setDistB(0L);
        ancla.setDistL(0L);
        ancla.setDistR(0L);
        ancla.setSimplePos2(false);
        ancla.setRelativeHeight(0L);
        ancla.setBehindDoc(true);
        ancla.setLocked(true);
        ancla.setLayoutInCell(true);
        ancla.setAllowOverlap(true);

        CTPoint2D origen = ancla.addNewSimplePos();
        origen.setX(0L);
        origen.setY(0L);

        CTPosH horizontal = ancla.addNewPositionH();
        horizontal.setRelativeFrom(STRelFromH.PAGE);
        horizontal.setPosOffset(0);

        CTPosV vertical = ancla.addNewPositionV();
        vertical.setRelativeFrom(STRelFromV.PAGE);
        vertical.setPosOffset(0);

        ancla.setExtent(enLinea.getExtent());
        ancla.addNewWrapNone();

        CTNonVisualDrawingProps propiedades = ancla.addNewDocPr();
        propiedades.setId(enLinea.getDocPr().getId() + 1000);
        propiedades.setName("Franja");

        ancla.setGraphic(enLinea.getGraphic());
        dibujo.removeInline(0);
    }

    // ------------------------------------------------------------------ anexo

    private void anexo(XWPFDocument doc, DtoInformeInspeccion d, List<byte[]> fotos) throws Exception {

        XWPFTable t = tabla(doc, 50, 50);
        boolean primera = true;

        for (int i = 0; i < fotos.size(); i += 2) {
            XWPFTableRow filaFotos = primera ? t.getRow(0) : t.createRow();
            XWPFTableRow filaPies = t.createRow();
            primera = false;
            filaFotos.setCantSplitRow(true);

            for (int c = 0; c < 2; c++) {
                int j = i + c;
                XWPFTableCell cFoto = filaFotos.getCell(c);
                XWPFTableCell cPie = filaPies.getCell(c);
                if (j >= fotos.size()) continue;

                XWPFParagraph pf = cFoto.getParagraphs().get(0);
                pf.setAlignment(ParagraphAlignment.CENTER);
                pf.setKeepNext(true);
                cFoto.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

                Imagen img = preparar(fotos.get(j));
                if (img == null) {
                    texto(pf, "Imagen no disponible", false, "888888").setItalic(true);
                } else {
                    double[] m = img.encajar(FOTO_ANCHO, FOTO_ALTO);
                    imagen(pf.createRun(), img.jpeg(), PictureType.JPEG, "foto" + (j + 1) + ".jpg", m[0], m[1]);
                }

                XWPFParagraph pp = cPie.getParagraphs().get(0);
                texto(pp, "FOTOGRAFÍA N° " + (j + 1) + ".", true, null).addBreak();
                texto(pp, "Sitio de Inspección: ", true, null);
                texto(pp, valor(d.getFotos().get(j).getDescripcion()), false, null);
            }
        }
    }

    // ------------------------------------------------------------------ piezas

    private void seccion(XWPFDocument doc, String titulo) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(240);
        p.setSpacingAfter(80);
        p.setKeepNext(true);
        CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        CTShd sombra = ppr.addNewShd();
        sombra.setVal(STShd.CLEAR);
        sombra.setFill(AZUL);
        texto(p, " " + titulo, true, "FFFFFF");
    }

    private void subtitulo(XWPFDocument doc, String titulo) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        p.setSpacingAfter(80);
        p.setKeepNext(true);
        texto(p, titulo, true, AZUL);
    }

    private XWPFParagraph parrafo(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);
        p.setSpacingAfter(100);
        return p;
    }

    private void parrafos(XWPFDocument doc, String texto) {
        List<String> ps = InformeFormato.parrafos(texto);
        if (ps.isEmpty()) texto(parrafo(doc), VACIO, false, null);
        ps.forEach(t -> texto(parrafo(doc), t, false, null));
    }

    private void vineta(XWPFDocument doc, String item) {
        XWPFParagraph p = parrafo(doc);
        p.setIndentationLeft(560);
        p.setIndentationHanging(280);
        p.setSpacingAfter(60);
        XWPFRun r = texto(p, "•", false, null);
        r.addTab();
        texto(p, item, false, null);
    }

    private XWPFRun texto(XWPFParagraph p, String texto, boolean negrita, String color) {
        XWPFRun r = p.createRun();
        r.setFontFamily(FUENTE);
        r.setFontSize(TAMANO);
        r.setBold(negrita);
        if (color != null) r.setColor(color);
        r.setText(texto);
        return r;
    }

    private void imagen(XWPFRun r, byte[] datos, PictureType tipo, String nombre, double anchoPt, double altoPt)
            throws Exception {
        r.addPicture(new ByteArrayInputStream(datos), tipo, nombre,
                Units.toEMU(anchoPt), Units.toEMU(altoPt));
    }

    /** Tabla de ancho completo con columnas en porcentaje del ancho util. */
    private XWPFTable tabla(XWPFDocument doc, int... porcentajes) {

        XWPFTable t = doc.createTable(1, porcentajes.length);

        CTTblPr pr = t.getCTTbl().getTblPr() != null ? t.getCTTbl().getTblPr() : t.getCTTbl().addNewTblPr();
        CTTblWidth ancho = pr.isSetTblW() ? pr.getTblW() : pr.addNewTblW();
        ancho.setType(STTblWidth.DXA);
        ancho.setW(BigInteger.valueOf(ANCHO_UTIL));
        (pr.isSetTblLayout() ? pr.getTblLayout() : pr.addNewTblLayout()).setType(STTblLayoutType.FIXED);

        CTTblGrid grid = t.getCTTbl().getTblGrid() != null ? t.getCTTbl().getTblGrid() : t.getCTTbl().addNewTblGrid();
        while (grid.sizeOfGridColArray() > 0) grid.removeGridCol(0);
        for (int pct : porcentajes) {
            grid.addNewGridCol().setW(BigInteger.valueOf((long) ANCHO_UTIL * pct / 100));
        }

        for (int c = 0; c < porcentajes.length; c++) {
            anchoCelda(t.getRow(0).getCell(c), (long) ANCHO_UTIL * porcentajes[c] / 100);
        }

        t.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, BORDE);
        t.setCellMargins(80, 110, 80, 110);
        return t;
    }

    private void anchoCelda(XWPFTableCell c, long twips) {
        CTTcPr pr = c.getCTTc().isSetTcPr() ? c.getCTTc().getTcPr() : c.getCTTc().addNewTcPr();
        CTTblWidth w = pr.isSetTcW() ? pr.getTcW() : pr.addNewTcW();
        w.setType(STTblWidth.DXA);
        w.setW(BigInteger.valueOf(twips));
    }

    /** Fila nueva (o la primera si fila == 0) con la forma de la tabla. */
    private XWPFTableRow fila(XWPFTable t, int fila) {
        XWPFTableRow r = fila == 0 ? t.getRow(0) : t.createRow();
        r.setCantSplitRow(true);
        return r;
    }

    private void fila2(XWPFTable t, int fila, String etiqueta, String valor) {
        XWPFTableRow r = fila(t, fila);
        celda(r.getCell(0), etiqueta, true, AZUL, CELESTE);
        celda(r.getCell(1), valor(valor), false, null, null);
    }

    private void fila4(XWPFTable t, int fila, String e1, String v1, String e2, String v2) {
        XWPFTableRow r = fila(t, fila);
        celda(r.getCell(0), e1, true, AZUL, CELESTE);
        celda(r.getCell(1), valor(v1), false, null, null);
        celda(r.getCell(2), e2, true, AZUL, CELESTE);
        celda(r.getCell(3), valor(v2), false, null, null);
    }

    private void cabecera(XWPFTable t, String... titulos) {
        XWPFTableRow r = t.getRow(0);
        r.setRepeatHeader(true);
        for (int c = 0; c < titulos.length; c++) {
            celda(r.getCell(c), titulos[c], true, "FFFFFF", AZUL)
                    .getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
        }
    }

    private void filaDatos(XWPFTable t, String... valores) {
        XWPFTableRow r = fila(t, -1);
        for (int c = 0; c < valores.length; c++) {
            celda(r.getCell(c), valores[c], false, null, null);
        }
    }

    private XWPFTableCell celda(XWPFTableCell c, String texto, boolean negrita, String color, String fondo) {
        if (fondo != null) c.setColor(fondo);
        c.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph p = c.getParagraphs().get(0);
        p.setSpacingAfter(0);
        texto(p, texto, negrita, color);
        return c;
    }
}
