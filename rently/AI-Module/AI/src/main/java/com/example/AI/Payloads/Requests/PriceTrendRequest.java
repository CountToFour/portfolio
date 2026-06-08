package com.example.AI.Payloads.Requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PriceTrendRequest {
    Double longitude;
    Double latitude;
    String location;
}
