package cl.duoc.ejemplo.microservicio.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import cl.duoc.ejemplo.microservicio.entity.Evento;
import cl.duoc.ejemplo.microservicio.entity.Ticket;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(52, 73, 94);

    public byte[] generateTicketPdf(Ticket ticket, Evento evento) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A5.rotate());
            document.setMargins(20, 20, 20, 20);

            // Header
            addHeader(document, evento);

            // Ticket info
            addTicketInfo(document, ticket, evento);

            // Footer con codigo
            addFooter(document, ticket);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF del ticket", e);
        }
    }

    private void addHeader(Document document, Evento evento) {
        Paragraph header = new Paragraph("TICKET DE ENTRADA")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(header);

        Paragraph eventName = new Paragraph(evento.getNombre())
                .setFontSize(18)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(eventName);
    }

    private void addTicketInfo(Document document, Ticket ticket, Evento evento) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        addTableRow(table, "Fecha del Evento:", evento.getFechaEvento().format(DATE_FORMATTER));
        addTableRow(table, "Ubicacion:", evento.getUbicacion());
        addTableRow(table, "Asistente:", ticket.getUsuarioNombre());
        addTableRow(table, "ID Usuario:", ticket.getUsuarioId());
        addTableRow(table, "Precio:", "$" + ticket.getPrecio().toString());
        addTableRow(table, "Estado:", ticket.getEstado().name());
        addTableRow(table, "Fecha de Compra:", ticket.getFechaCompra().format(DATE_FORMATTER));

        document.add(table);
    }

    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold().setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, Ticket ticket) {
        document.add(new Paragraph("\n"));

        Table codeTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(80))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Cell codeCell = new Cell()
                .add(new Paragraph("CODIGO DE TICKET")
                        .setFontSize(10)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(ticket.getCodigoTicket())
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(236, 240, 241))
                .setPadding(10);

        codeTable.addCell(codeCell);
        document.add(codeTable);

        Paragraph disclaimer = new Paragraph("Presente este ticket en la entrada del evento. Este ticket es personal e intransferible.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15);
        document.add(disclaimer);
    }
}
