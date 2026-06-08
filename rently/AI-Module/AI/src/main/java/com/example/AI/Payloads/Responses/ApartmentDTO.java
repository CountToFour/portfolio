package com.example.AI.Payloads.Responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApartmentDTO {
    private Long id;
    private BigDecimal price;
    private String location;
    private Integer rooms;
    private String rentalType;
    private Boolean available;
    private Double latitude;
    private Double longitude;
    private Long ownerId;
    private String ownerName;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Long viewCount;

}