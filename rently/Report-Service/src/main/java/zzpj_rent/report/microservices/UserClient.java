package zzpj_rent.report.microservices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import zzpj_rent.report.dtos.request.UserDTO;

@FeignClient(name = "RENTLYAUTH")
public interface UserClient {
    @GetMapping("/findUserByID/{userId}")
    UserDTO getUserById(@PathVariable Long userId);
}
