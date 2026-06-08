package zzpj_rent.report.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zzpj_rent.report.dtos.request.ContractRequest;
import zzpj_rent.report.exceptions.ReportException;
import zzpj_rent.report.services.ReportService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.ByteArrayOutputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@RequestMapping("/api/report")
//@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Report API", description = "Endpoints for managing reports")
public class ReportController {

    @Autowired
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/property")
    @Operation(summary = "Generuje raport dotyczący nieruchomości jako plik PDF")
    @ApiResponse(
            responseCode = "200",
            description = "Plik PDF z raportem",
            content = @Content(
                    mediaType = "application/pdf",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @ApiResponse(responseCode = "500", description = "Błąd podczas generowania raportu")
    public ResponseEntity<?> generateReportProperty(@Parameter(description = "ID of the property") @RequestParam Long propertyId) {
        try {
            JasperPrint jasperPrint;
            jasperPrint = reportService.createReportProperty(propertyId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        } catch (ReportException ex) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }
    }

    @GetMapping("/propertyStats")
    @Operation(summary = "Generuje raport dotyczący nieruchomości jako plik PDF")
    @ApiResponse(
            responseCode = "200",
            description = "Plik PDF z raportem",
            content = @Content(
                    mediaType = "application/pdf",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @ApiResponse(responseCode = "500", description = "Błąd podczas generowania raportu")
    public ResponseEntity<?> generateReportPropertyStats(@Parameter(description = "ID of the property") @RequestParam Long propertyId) {
        try {
            JasperPrint jasperPrint;
            jasperPrint = reportService.createReportPropertyStats(propertyId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        } catch (ReportException ex) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }
    }

    @GetMapping("/reservation")
    @Operation(summary = "Generuje raport dotyczący rezerwacji jako plik PDF")
    @ApiResponse(
            responseCode = "200",
            description = "Plik PDF z raportem",
            content = @Content(
                    mediaType = "application/pdf",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @ApiResponse(responseCode = "500", description = "Błąd podczas generowania raportu")
    public ResponseEntity<?> generateReportReservation(@Parameter(description = "ID of the reservation") @RequestParam Long reservationId,
                                                            @Parameter(description = "ID of the owner") @RequestParam Long ownerId) {
        try {
            JasperPrint jasperPrint;
            jasperPrint = reportService.createReportReservation(reservationId, ownerId);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        } catch (ReportException ex) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }
    }

    @PostMapping("/contract")
    @Operation(
            summary = "Generuje umowę w formacie PDF na podstawie danych wejściowych"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Wygenerowany plik PDF z umową",
            content = @Content(
                    mediaType = "application/pdf",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @ApiResponse(responseCode = "500", description = "Błąd podczas generowania raportu")
    public ResponseEntity<?> generateContract(@RequestBody ContractRequest request) {
        try {
            JasperPrint jasperPrint;
            jasperPrint = reportService.createContract(request);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        } catch (ReportException ex) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }
    }
}
