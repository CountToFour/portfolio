package com.example.AI.Payloads.Requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PriceEstimateRequest {
    Double longitude;
    Double latitude;
    String location;
    int rooms;
    float area;
}
