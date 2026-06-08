package com.example.AI.Payloads.Requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class RecommendationRequest {
    @NotBlank
    private String location;

    @Min(1)
    private int rooms;

    @DecimalMin("0.0")
    private double price;
}
