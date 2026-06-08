package zzpj_rent.report.exceptions;

import org.springframework.http.HttpStatus;

public final class BadRequestException extends ReportException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
