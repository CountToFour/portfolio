package com.example.AI.Payloads.Requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PriceRateRequest {
    private BigDecimal price;
    Double longitude;
    Double latitude;
    String location;
    int rooms;
    float area;
}
