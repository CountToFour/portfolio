package zzpj_rent.report.microservices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import zzpj_rent.report.dtos.request.ReservationDTO;

@FeignClient(name = "RESERVATION")
public interface ReservationClient {
    @GetMapping("/api/rent/reservations/{id}/owner/{ownerId}")
    ReservationDTO getReservationById(@PathVariable Long id, @PathVariable Long ownerId);
}
