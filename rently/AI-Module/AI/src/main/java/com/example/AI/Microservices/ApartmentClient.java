package com.example.AI.Microservices;

import com.example.AI.Payloads.Responses.ApartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "Rental-listing-service")
public interface ApartmentClient {

    @GetMapping("/api/apartments")
    List<ApartmentDTO> getApartments();

}