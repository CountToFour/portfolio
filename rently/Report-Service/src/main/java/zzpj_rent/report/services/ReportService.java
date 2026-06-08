package zzpj_rent.report.services;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import net.sf.jasperreports.engine.*;
import zzpj_rent.report.dtos.request.ApartmentDTO;
import zzpj_rent.report.dtos.request.ContractRequest;
import zzpj_rent.report.dtos.request.ReservationDTO;
import zzpj_rent.report.dtos.request.UserDTO;
import zzpj_rent.report.exceptions.BadRequestException;
import zzpj_rent.report.exceptions.NotFoundException;
import zzpj_rent.report.microservices.ApartmentClient;
import zzpj_rent.report.microservices.ReservationClient;
import zzpj_rent.report.microservices.UserClient;
import zzpj_rent.report.model.Property;
import zzpj_rent.report.model.Reservation;
import zzpj_rent.report.model.User;

@Service
@AllArgsConstructor
public class ReportService {

    private final ApartmentClient apartmentClient;
    private final UserClient userClient;
    private final ReservationClient reservationClient;

    public JasperPrint createReportProperty(Long id) {
        try {
            String rentalType;
            LocalDate nowDate = LocalDate.now();
            String avab;

            Property property = getProperty(id);

            rentalType = switch (property.getRentalType()) {
                case "LONG_TERM" -> "Długoterminowy";
                case "SHORT_TERM" -> "Krótkoterminowy";
                case "DAILY" -> "Dzienny";
                default -> throw new NotFoundException("Rental Type not found");
            };

            if (property.getAvailable()) {
                avab = "Dostępny";
            } else {
                avab = "Niedostępny";
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Data", nowDate);
            parameters.put("latitude", property.getLatitude());
            parameters.put("longitude", property.getLongitude());
            parameters.put("location", property.getLocation());
            parameters.put("rooms", property.getRooms());
            parameters.put("rentalType", rentalType);
            parameters.put("available", avab);
            parameters.put("id", id);
            parameters.put("price", property.getPrice().doubleValue());


            String filePath = "reportsModels/ReportProperty.jrxml";
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (reportStream == null) {
                throw new IllegalArgumentException("Plik raportu nie został znaleziony: " + filePath);
            }

            JasperReport report = JasperCompileManager.compileReport(reportStream);
            return JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            throw new BadRequestException("An error occurred while creating the report: " + e.getMessage());
        }
    }

    public JasperPrint createReportPropertyStats(Long id) {
        try {
            LocalDate nowDate = LocalDate.now();

            Property property = getProperty(id);


            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Data", nowDate);
            parameters.put("latitude", property.getLatitude());
            parameters.put("longitude", property.getLongitude());
            parameters.put("location", property.getLocation());
            parameters.put("id", id);
            parameters.put("averageRating", property.getAverageRating().doubleValue());
            parameters.put("viewCount", property.getViewCount());
            parameters.put("ratingCount", property.getRatingCount());


            String filePath = "reportsModels/ReportPropertyStats.jrxml";
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (reportStream == null) {
                throw new IllegalArgumentException("Plik raportu nie został znaleziony: " + filePath);
            }

            JasperReport report = JasperCompileManager.compileReport(reportStream);
            return JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            throw new BadRequestException("An error occurred while creating the report: " + e.getMessage());
        }
    }

    public JasperPrint createReportReservation(Long id, Long ownerId) {
        try {
            LocalDate nowDate = LocalDate.now();
            String status;

            Reservation reservation = getReservation(id, ownerId);

            System.out.println(reservation);

            status = switch (reservation.getStatus()) {
                case "PENDING" -> "Oczekujaca";
                case "CONFIRMED" -> "Potwierdzona";
                case "CANCELLED" -> "Anulowana";
                case "REJECTED" -> "Odrzucona";
                case "FINISHED" -> "Zakończona";
                default -> throw new NotFoundException("Reservation status not found");
            };

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Data", nowDate);
            parameters.put("idProperty", reservation.getPropertyId());
            parameters.put("idTenant", reservation.getTenantId());
            parameters.put("createdAt", reservation.getEndDate());
            parameters.put("sinceDate", reservation.getStartDate());
            parameters.put("untilDate", reservation.getEndDate());
            parameters.put("status", status);
            parameters.put("id", reservation.getId());
            parameters.put("tenantName", reservation.getTenantName());
            parameters.put("tenantSurname", reservation.getTenantSurname());


            String filePath = "reportsModels/ReportReservation.jrxml";
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (reportStream == null) {
                throw new IllegalArgumentException("Plik raportu nie został znaleziony: " + filePath);
            }

            JasperReport report = JasperCompileManager.compileReport(reportStream);
            return JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            throw new BadRequestException("An error occurred while creating the report: " + e.getMessage());
        }
    }

    public JasperPrint createContract(ContractRequest request) {
        try {

            if (request.getCity() == null || request.getCity().isEmpty()) {
                throw new BadRequestException("City is required");
            } else if (request.getLandlordIdNumber() == null || request.getLandlordIdNumber().isEmpty()) {
                throw new BadRequestException("Landlord Id Number is required");
            } else if (request.getLandlordIdNumber().length() != 9) {
                throw new BadRequestException("Landlord Id Number must be 9 characters");
            } else if (request.getTenantIdNumber() == null || request.getTenantIdNumber().isEmpty()) {
                throw new BadRequestException("Tenant Id Number is required");
            } else if (request.getTenantIdNumber().length() != 9) {
                throw new BadRequestException("Tenant Id Number must be 9 characters");
            } else if (request.getArea() == null) {
                throw new BadRequestException("Area is required");
            } else if (request.getArea() <= 0) {
                throw new BadRequestException("Area is lower or equal to 0");
            } else if (request.getPayDay() > 31 || request.getPayDay() < 1) {
                throw new BadRequestException("Pay day must be between 1 and 31");
            } else if (request.getDeposit() == null) {
                throw new BadRequestException("Deposit is required");
            } else if (request.getDeposit() < 0) {
                throw new BadRequestException("Deposit is lower than 0");
            } else if (request.getReservationId() == null) {
                throw new BadRequestException("Reservation Id is required");
            } else if (request.getReservationId() <= 0) {
                throw new BadRequestException("Reservation Id is lower than 0");
            } else if (request.getTenantId() == null) {
                throw new BadRequestException("Tenant Id is required");
            } else if (request.getTenantId() <= 0) {
                throw new BadRequestException("Tenant Id is lower than 0");
            } else if (request.getOwnerId() == null) {
                throw new BadRequestException("Owner Id is required");
            } else if (request.getOwnerId() <= 0) {
                throw new BadRequestException("Owner Id is lower than 0");
            } else if (request.getOwnerCity() == null || request.getOwnerCity().isEmpty()) {
                throw new BadRequestException("Owner City is required");
            } else if (request.getOwnerStreet() == null || request.getOwnerStreet().isEmpty()) {
                throw new BadRequestException("Owner Street is required");
            } else if (request.getTenantCity() == null || request.getTenantCity().isEmpty()) {
                throw new BadRequestException("Tenant City is required");
            } else if (request.getTenantStreet() == null || request.getTenantStreet().isEmpty()) {
                throw new BadRequestException("Tenant Street is required");
            }

            Reservation reservation = getReservation(request.getReservationId(), request.getOwnerId());
            Property property = getProperty(reservation.getPropertyId());
            User owner = getUser(reservation.getTenantId());
            LocalDate nowDate = LocalDate.now();

            if (property.getLocation() == null || property.getLocation().isEmpty()) {
                throw new NotFoundException("Location not found");
            } else if (property.getRooms() <= 0) {
                throw new NotFoundException("Rooms not found");
            } else if (reservation.getStartDate() == null) {
                throw new NotFoundException("SinceDate not found");
            } else if (reservation.getEndDate() == null) {
                throw new NotFoundException("UntilDate not found");
            } else if (owner.getFirstName() == null || owner.getFirstName().isEmpty()) {
                throw new NotFoundException("First name not found");
            } else if (owner.getLastName() == null || owner.getLastName().isEmpty()) {
                throw new NotFoundException("Last name not found");
            } else if (reservation.getTenantName() == null || reservation.getTenantName().isEmpty()) {
                throw new NotFoundException("Tenant name not found");
            } else if (reservation.getTenantSurname() == null || reservation.getTenantSurname().isEmpty()) {
                throw new NotFoundException("Tenant Surname not found");
            } else if (property.getPrice().doubleValue() < 0) {
                throw new BadRequestException("Price must be greater than 0");
            } else if (reservation.getStatus() == null) {
                throw new BadRequestException("Reservation Status is required");
            } else if (reservation.getStatus().equals("CANCELLED")) {
                throw new BadRequestException("Reservation Status is CANCELLED");
            }


            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Data", nowDate);
            parameters.put("location", property.getLocation());
            parameters.put("rooms", property.getRooms());
            parameters.put("city", request.getCity());
            parameters.put("landlordName", owner.getFirstName() + " " + owner.getLastName());
            parameters.put("landlordCity", request.getOwnerCity());
            parameters.put("landlordAddress", request.getOwnerStreet());
            parameters.put("landlordIdNumber", request.getLandlordIdNumber());
            parameters.put("tenantName", reservation.getTenantName() + " " + reservation.getTenantSurname());
            parameters.put("tenantCity", request.getTenantCity());
            parameters.put("tenantAddress", request.getTenantStreet());
            parameters.put("tenantIdNumber", request.getTenantIdNumber());
            parameters.put("area", request.getArea());
            parameters.put("sinceDate", reservation.getStartDate());
            parameters.put("untilDate", reservation.getEndDate());
            parameters.put("fee", property.getPrice().doubleValue());
            parameters.put("payDay", request.getPayDay());
            parameters.put("transmissionDate", reservation.getStartDate());
            parameters.put("deposit", request.getDeposit());


            String filePath = "reportsModels/Contract.jrxml";
            InputStream reportStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (reportStream == null) {
                throw new NotFoundException("Plik raportu nie został znaleziony: " + filePath);
            }

            JasperReport report = JasperCompileManager.compileReport(reportStream);
            return JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            throw new BadRequestException("An error occurred while creating the contract: " + e.getMessage());
        }
    }

    public Property getProperty(Long id) {
        ApartmentDTO apartmentDto;
        try {
            apartmentDto = apartmentClient.getApartmentById(id);
        } catch (FeignException.NotFound _) {
            throw new NotFoundException("Apartment not found");
        }

        if (apartmentDto == null) {
            throw new NotFoundException("Apartment not found");
        }


        return  new Property(apartmentDto.getId(), apartmentDto.getPrice(), apartmentDto.getLocation(), apartmentDto.getRooms(), apartmentDto.getRentalType(),
                apartmentDto.getAvailable(), apartmentDto.getLatitude(),
                apartmentDto.getLongitude(), apartmentDto.getOwnerId(), apartmentDto.getAverageRating(),
                apartmentDto.getRatingCount(), apartmentDto.getViewCount());
    }

    public User getUser(Long id) {
        UserDTO userDto;
        try {
            userDto = userClient.getUserById(id);
        } catch (FeignException.NotFound _) {
            throw new NotFoundException("User not found");
        }

        if (userDto == null) {
            throw new NotFoundException("User not found");
        }

        return new User(userDto.getId(), userDto.getUsername(), userDto.getEmail(),
                userDto.getFirstName(), userDto.getLastName());
    }

    public Reservation getReservation(Long id, Long ownerId) {
        ReservationDTO reservationDto;
        try {
            reservationDto = reservationClient.getReservationById(id, ownerId);
        } catch (FeignException.NotFound _) {
            throw new NotFoundException("Reservation not found");
        }

        if (reservationDto == null) {
            throw new NotFoundException("Reservation not found");
        }

        System.out.println(reservationDto);

        return new Reservation(reservationDto.getId(), reservationDto.getPropertyId(), reservationDto.getTenantId(),
                reservationDto.getTenantName(), reservationDto.getTenantSurname(),
                reservationDto.getStartDate(), reservationDto.getEndDate(), reservationDto.getStatus());
    }

}
