package zzpj_rent.report.exceptions;

import org.springframework.http.HttpStatus;

public final class ForbiddenException extends ReportException {
    public ForbiddenException(String message) {
        super(HttpStatus.BAD_REQUEST, message);;
    }
}
