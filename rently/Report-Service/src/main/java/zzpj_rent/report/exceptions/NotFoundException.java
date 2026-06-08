package zzpj_rent.report.exceptions;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends ReportException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
