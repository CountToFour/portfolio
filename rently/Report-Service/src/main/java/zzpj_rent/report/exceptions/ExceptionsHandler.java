package zzpj_rent.report.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zzpj_rent.report.dtos.response.ErrorMessage;

@RestControllerAdvice
public class ExceptionsHandler {
    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ErrorMessage> handleReservationException(ReportException ex) {
        System.out.println("Obsłużono ReportException: " + ex.getMessage());

        ErrorMessage errorResponse = new ErrorMessage(
                ex.getStatus(),
                ex.getMessage(),
                ex.getTimestamp()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}