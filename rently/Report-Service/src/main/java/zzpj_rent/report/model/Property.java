package zzpj_rent.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Entity
@Table(name = "apartment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal price;
    private String location;
    private Integer rooms;
    private String rentalType;
    private Boolean available;
    private Double latitude;
    private Double longitude;
    private Long ownerId;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Long viewCount;
}

