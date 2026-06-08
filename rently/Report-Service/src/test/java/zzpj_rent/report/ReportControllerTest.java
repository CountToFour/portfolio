package zzpj_rent.report;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.base.JRBasePrintPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import zzpj_rent.report.controllers.ReportController;
import zzpj_rent.report.dtos.request.ContractRequest;
import zzpj_rent.report.exceptions.ReportException;
import zzpj_rent.report.services.ReportService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private ReportService reportService;
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        reportController = new ReportController(reportService);
    }

    @Test
    void testGenerateReportProperty_success() throws Exception {
        Long propertyId = 2L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("test-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());

        when(reportService.createReportProperty(propertyId)).thenReturn(jasperPrint);


        ResponseEntity<?> response = reportController.generateReportProperty(propertyId);
        System.out.println(response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getHeaders().getContentType()).toString().contains("pdf"));
        assertInstanceOf(byte[].class, response.getBody());
    }

    @Test
    void testGenerateReportProperty_reportException() throws Exception {
        Long propertyId = 1L;
        ReportException exception = new ReportException(HttpStatus.BAD_REQUEST, "Report error");
        when(reportService.createReportProperty(propertyId)).thenThrow(exception);

        ResponseEntity<?> response = reportController.generateReportProperty(propertyId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Report error", response.getBody());
    }

    @Test
    void testGenerateReportProperty_generalException() throws Exception {
        Long propertyId = 1L;
        when(reportService.createReportProperty(propertyId)).thenThrow(new RuntimeException("Internal Error"));

        ResponseEntity<?> response = reportController.generateReportProperty(propertyId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Error", response.getBody());
    }

    @Test
    void testGenerateReportPropertyStats_success() throws Exception {
        Long propertyId = 2L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("test-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
        when(reportService.createReportPropertyStats(propertyId)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateReportPropertyStats(propertyId);

        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(byte[].class, response.getBody());
    }

    @Test
    void testGenerateReportReservation_success() throws Exception {
        Long reservationId = 5L;
        Long ownerId = 10L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("test-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
        when(reportService.createReportReservation(reservationId, ownerId)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateReportReservation(reservationId, ownerId);

        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(byte[].class, response.getBody());
    }

    @Test
    void testGenerateContract_generalException() throws Exception {
        ContractRequest request = new ContractRequest();
        when(reportService.createContract(request)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = reportController.generateContract(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void testGenerateReportReservation_generalException() throws Exception {
        Long reservationId = 5L;
        Long ownerId = 10L;
        when(reportService.createReportReservation(reservationId, ownerId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = reportController.generateReportReservation(reservationId, ownerId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody());
    }

    @Test
    void testGenerateReportPropertyStats_reportException() throws Exception {
        Long propertyId = 7L;
        ReportException exception = new ReportException(HttpStatus.BAD_REQUEST, "Stats error");
        when(reportService.createReportPropertyStats(propertyId)).thenThrow(exception);

        ResponseEntity<?> response = reportController.generateReportPropertyStats(propertyId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Stats error", response.getBody());
    }

    @Test
    void testGenerateReportPropertyStats_generalException() throws Exception {
        Long propertyId = 7L;
        when(reportService.createReportPropertyStats(propertyId)).thenThrow(new RuntimeException("Unexpected failure"));

        ResponseEntity<?> response = reportController.generateReportPropertyStats(propertyId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected failure", response.getBody());
    }

    @Test
    void testGenerateReportReservation_reportException() throws Exception {
        Long reservationId = 5L;
        Long ownerId = 10L;
        ReportException exception = new ReportException(HttpStatus.BAD_REQUEST, "Reservation error");
        when(reportService.createReportReservation(reservationId, ownerId)).thenThrow(exception);

        ResponseEntity<?> response = reportController.generateReportReservation(reservationId, ownerId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Reservation error", response.getBody());
    }



    @Test
    void testGenerateContract_success() throws Exception {
        ContractRequest request = new ContractRequest();
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("test-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
        when(reportService.createContract(request)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateContract(request);

        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(byte[].class, response.getBody());
    }

    @Test
    void testGenerateContract_reportException() throws Exception {
        ContractRequest request = new ContractRequest();
        ReportException exception = new ReportException(HttpStatus.NOT_FOUND, "Contract error");
        when(reportService.createContract(request)).thenThrow(exception);

        ResponseEntity<?> response = reportController.generateContract(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Contract error", response.getBody());
    }

    @Test
    void testGenerateReportProperty_shouldReturnProperHeaders() throws Exception {
        Long propertyId = 3L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("test-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
        when(reportService.createReportProperty(propertyId)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateReportProperty(propertyId);

        assertEquals("attachment; filename=report.pdf", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("application/pdf", response.getHeaders().getFirst("Content-Type"));
    }

    @Test
    void testGenerateContract_responseBodyIsNotEmpty() throws Exception {
        ContractRequest request = new ContractRequest();
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setName("contract-report");
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());
        when(reportService.createContract(request)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateContract(request);
        byte[] pdf = (byte[]) response.getBody();

        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "Plik PDF powinien mieć sensowną długość");
    }

    @Test
    void testGenerateReportProperty_multipleCalls() throws Exception {
        Long propertyId = 2L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());

        when(reportService.createReportProperty(propertyId)).thenReturn(jasperPrint);

        reportController.generateReportProperty(propertyId);
        reportController.generateReportProperty(propertyId);

        verify(reportService, times(2)).createReportProperty(propertyId);
    }

    @Test
    void testGenerateReportProperty_emptyPages() throws Exception {
        Long propertyId = 99L;
        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        when(reportService.createReportProperty(propertyId)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateReportProperty(propertyId);
        assertEquals(200, response.getStatusCodeValue());
        byte[] pdf = (byte[]) response.getBody();

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerateContract_withFilledRequest() throws Exception {
        ContractRequest request = new ContractRequest();

        JasperPrint jasperPrint = new JasperPrint();
        jasperPrint.setPageWidth(595);
        jasperPrint.setPageHeight(842);
        jasperPrint.addPage(new JRBasePrintPage());

        when(reportService.createContract(request)).thenReturn(jasperPrint);

        ResponseEntity<?> response = reportController.generateContract(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(byte[].class, response.getBody());
    }

}

