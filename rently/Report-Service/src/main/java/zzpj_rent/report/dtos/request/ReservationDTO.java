package zzpj_rent.report.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long propertyId;
    private Long tenantId;
    private String tenantName;
    private String tenantSurname;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
