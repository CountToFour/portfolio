package zzpj_rent.report.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public sealed class ReportException extends RuntimeException permits NotFoundException, ForbiddenException, BadRequestException {
    @Getter
    private final HttpStatus status;
    private final String message;
    @Getter
    private final LocalDateTime timestamp;

    public ReportException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
