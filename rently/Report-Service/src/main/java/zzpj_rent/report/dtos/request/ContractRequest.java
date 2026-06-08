package zzpj_rent.report.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractRequest {
    @Schema(description = "Miasto", example = "Warszawa")
    private String city;
    @Schema(description = "Numer dokumentu właściciela", example = "123456789")
    private String landlordIdNumber;
    @Schema(description = "Numer dokumentu najemcy", example = "123456789")
    private String tenantIdNumber;
    @Schema(description = "Powierzchnia", example = "55")
    private Double area;
    @Schema(description = "Dzień zapłaty", example = "10")
    private int payDay;
    @Schema(description = "Kaucja", example = "1400")
    private Double deposit;
    @Schema(description = "Id rezerwacji", example = "1")
    private Long reservationId;
    @Schema(description = "Id wynajmującego", example = "1")
    private Long tenantId;
    @Schema(description = "Id właściciela", example = "1")
    private Long ownerId;
    @Schema(description = "Miasto właściciela", example = "Warszawa")
    private String ownerCity;
    @Schema(description = "Ulica właściciela", example = "Złota 45")
    private String ownerStreet;
    @Schema(description = "Miasto wynajmującego", example = "Łódź")
    private String tenantCity;
    @Schema(description = "Ulica wynajmującego", example = "Czarna 2")
    private String tenantStreet;
}
