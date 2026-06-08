package zzpj_rent.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import zzpj_rent.report.services.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReservationClient reservationClient;

    @Mock
    private ApartmentClient arpartmentClient;

    @Mock
    private UserClient userClient;

    private ContractRequest validRequest() {
        ContractRequest request = new ContractRequest();
        request.setCity("Warszawa");
        request.setLandlordIdNumber("123456789");
        request.setTenantIdNumber("987654321");
        request.setArea(45.0);
        request.setPayDay(15);
        request.setDeposit(500.0);
        request.setReservationId(2L);
        request.setOwnerId(1L);
        request.setTenantId(2L);
        request.setOwnerCity("Warszawa");
        request.setOwnerStreet("Mala 3");
        request.setTenantCity("Warszawa");
        request.setTenantStreet("Mala 3");
        return request;
    }

    @Test
    void shouldCreateContractSuccessfully() throws JRException {
        ContractRequest request = new ContractRequest();
        request.setCity("Warszawa");
        request.setLandlordIdNumber("123456789");
        request.setTenantIdNumber("987654321");
        request.setArea(50.0);
        request.setPayDay(10);
        request.setDeposit(1000.0);
        request.setReservationId(2L);
        request.setOwnerId(1L);
        request.setTenantId(2L);
        request.setOwnerCity("Warszawa");
        request.setOwnerStreet("Mala 3");
        request.setTenantCity("Warszawa");
        request.setTenantStreet("Mala 3");

        ReservationDTO fakeReservation = new ReservationDTO();

        when(reservationClient.getReservationById(2L, 1L)).thenReturn(fakeReservation);
        fakeReservation.setTenantId(2L);
        fakeReservation.setId(2L);
        fakeReservation.setPropertyId(1L);
        fakeReservation.setTenantName("Marek");
        fakeReservation.setTenantSurname("Ogloza");
        fakeReservation.setStartDate(LocalDate.now());
        fakeReservation.setEndDate(LocalDate.now().plusDays(1));
        fakeReservation.setStatus("CONFIRMED");

        ApartmentDTO apartmentDTO = new ApartmentDTO();
        apartmentDTO.setLocation("Warszawa");
        apartmentDTO.setRooms(1);
        apartmentDTO.setPrice(new BigDecimal("1000"));

        when(arpartmentClient.getApartmentById(fakeReservation.getPropertyId())).thenReturn(apartmentDTO);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);

        JasperPrint jasperPrint = reportService.createContract(request);

        assertNotNull(jasperPrint);
    }

    @Test
    void shouldThrowWhenCityIsNull() {
        ContractRequest request = validRequest();
        request.setCity(null);

        assertThrows(BadRequestException.class, () -> reportService.createContract(request));
    }

    @Test
    void shouldThrowWhenCityIsEmpty() {
        ContractRequest request = validRequest();
        request.setCity("");

        assertThrows(BadRequestException.class, () -> reportService.createContract(request));
    }

    @Test
    void shouldThrowWhenTenantIdNumberTooShort() {
        ContractRequest request = validRequest();
        request.setTenantIdNumber("123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Id Number must be 9 characters", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTenantIdNumberTooLong() {
        ContractRequest request = validRequest();
        request.setTenantIdNumber("12345678912");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Id Number must be 9 characters", ex.getMessage());
    }

    @Test
    void shouldThrowWhenLandlordIdNumberTooShort() {
        ContractRequest request = validRequest();
        request.setLandlordIdNumber("123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Landlord Id Number must be 9 characters", ex.getMessage());
    }

    @Test
    void shouldThrowWhenLandlordIdNumberTooLong() {
        ContractRequest request = validRequest();
        request.setLandlordIdNumber("12345678912");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Landlord Id Number must be 9 characters", ex.getMessage());
    }

    @Test
    void LandlordIdNumberIsNullorEmpty() {
        ContractRequest request = validRequest();
        request.setLandlordIdNumber(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Landlord Id Number is required", ex.getMessage());

        request.setLandlordIdNumber("");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Landlord Id Number is required", ex2.getMessage());
    }

    @Test
    void TenantIdNumberIsNullorEmpty() {
        ContractRequest request = validRequest();
        request.setTenantIdNumber(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Id Number is required", ex.getMessage());

        request.setTenantIdNumber("");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Id Number is required", ex2.getMessage());
    }

    @Test
    void areaIsLowerThan0() {
        ContractRequest request = validRequest();
        request.setArea((double) 0);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Area is lower or equal to 0", ex.getMessage());
    }

    @Test
    void areaIsNull() {
        ContractRequest request = validRequest();
        request.setArea(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Area is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPayDayOutOfRange() {
        ContractRequest request = validRequest();
        request.setPayDay(50);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Pay day must be between 1 and 31", ex.getMessage());

        request.setPayDay(0);

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Pay day must be between 1 and 31", ex2.getMessage());
    }

    @Test
    void depositIsLowerThan0() {
        ContractRequest request = validRequest();
        request.setDeposit((double) -1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Deposit is lower than 0", ex.getMessage());
    }

    @Test
    void depositIsNull() {
        ContractRequest request = validRequest();
        request.setDeposit(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Deposit is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        ContractRequest request = validRequest();
        request.setReservationId(10L);
        request.setOwnerId(1L);

        when(reservationClient.getReservationById(10L, 1L)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> reportService.createContract(request));
        assertEquals("Reservation not found", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        ContractRequest request = validRequest();
        request.setReservationId(2L);
        request.setOwnerId(1L);
        request.setTenantId(3L);

        ReservationDTO fakeReservation = new ReservationDTO();
        fakeReservation.setTenantId(3L);
        fakeReservation.setId(2L);
        fakeReservation.setPropertyId(1L);
        fakeReservation.setTenantName("Jan");
        fakeReservation.setTenantSurname("Kowalski");
        fakeReservation.setStartDate(LocalDate.now());
        fakeReservation.setEndDate(LocalDate.now().plusDays(1));
        fakeReservation.setStatus("CONFIRMED");

        when(reservationClient.getReservationById(2L, 1L)).thenReturn(fakeReservation);
        when(arpartmentClient.getApartmentById(1L)).thenReturn(new ApartmentDTO());
        when(userClient.getUserById(3L)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> reportService.createContract(request));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void shouldThrowWhenReservationStatusCanceled() {
        ContractRequest request = validRequest();
        request.setReservationId(2L);
        request.setOwnerId(1L);

        ReservationDTO fakeReservation = new ReservationDTO();
        fakeReservation.setTenantId(2L);
        fakeReservation.setId(2L);
        fakeReservation.setPropertyId(1L);
        fakeReservation.setTenantName("Marek");
        fakeReservation.setTenantSurname("Ogloza");
        fakeReservation.setStartDate(LocalDate.now());
        fakeReservation.setEndDate(LocalDate.now().plusDays(1));
        fakeReservation.setStatus("CANCELLED");

        when(reservationClient.getReservationById(2L, 1L)).thenReturn(fakeReservation);

        ApartmentDTO apartmentDTO = new ApartmentDTO();
        apartmentDTO.setLocation("Warszawa");
        apartmentDTO.setRooms(1);
        apartmentDTO.setPrice(new BigDecimal("1000"));

        when(arpartmentClient.getApartmentById(fakeReservation.getPropertyId())).thenReturn(apartmentDTO);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Reservation Status is CANCELLED", ex.getMessage());
    }

    @Test
    void shouldThrowWhenOwnerCityIsNullOrEmpty() {
        ContractRequest request = validRequest();
        request.setOwnerCity(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Owner City is required", ex.getMessage());

        request.setOwnerCity("");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Owner City is required", ex2.getMessage());
    }

    @Test
    void shouldThrowWhenTenantStreetIsNullOrEmpty() {
        ContractRequest request = validRequest();
        request.setTenantStreet(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Street is required", ex.getMessage());

        request.setTenantStreet("");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Tenant Street is required", ex2.getMessage());
    }

    @Test
    void shouldCreateContractWhenPayDayIsBoundaryValues() throws JRException {
        ContractRequest request = validRequest();
        request.setPayDay(1);

        ReservationDTO fakeReservation = new ReservationDTO();
        fakeReservation.setTenantId(2L);
        fakeReservation.setId(2L);
        fakeReservation.setPropertyId(1L);
        fakeReservation.setTenantName("Marek");
        fakeReservation.setTenantSurname("Ogloza");
        fakeReservation.setStartDate(LocalDate.now());
        fakeReservation.setEndDate(LocalDate.now().plusDays(1));
        fakeReservation.setStatus("CONFIRMED");

        when(reservationClient.getReservationById(2L, 1L)).thenReturn(fakeReservation);

        ApartmentDTO apartmentDTO = new ApartmentDTO();
        apartmentDTO.setLocation("Warszawa");
        apartmentDTO.setRooms(1);
        apartmentDTO.setPrice(new BigDecimal("1000"));

        when(arpartmentClient.getApartmentById(fakeReservation.getPropertyId())).thenReturn(apartmentDTO);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);

        JasperPrint jasperPrint = reportService.createContract(request);
        assertNotNull(jasperPrint);

        request.setPayDay(31);

        jasperPrint = reportService.createContract(request);
        assertNotNull(jasperPrint);
    }

    @Test
    void shouldThrowWhenOwnerAddressMissing() {
        ContractRequest request = validRequest();
        request.setOwnerStreet(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Owner Street is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenTenantNameIsEmpty() {
        ContractRequest request = validRequest();
        request.setReservationId(1L);
        request.setOwnerId(1L);
        request.setTenantId(2L);

        ReservationDTO reservation = new ReservationDTO();
        reservation.setTenantId(2L);
        reservation.setPropertyId(1L);
        reservation.setTenantName("");
        reservation.setTenantSurname("");
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));
        reservation.setStatus("CONFIRMED");

        ApartmentDTO apartment = new ApartmentDTO();
        apartment.setLocation("Warszawa");
        apartment.setRooms(2);
        apartment.setPrice(BigDecimal.valueOf(2000));

        when(reservationClient.getReservationById(1L, 1L)).thenReturn(reservation);
        when(arpartmentClient.getApartmentById(1L)).thenReturn(apartment);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);


        NotFoundException ex = assertThrows(NotFoundException.class, () -> reportService.createContract(request));
        assertEquals("Tenant name not found", ex.getMessage());
    }

    @Test
    void shouldThrowWhenReservationStatusIsNull() {
        ContractRequest request = validRequest();
        request.setReservationId(1L);
        request.setOwnerId(1L);
        request.setTenantId(2L);

        ReservationDTO reservation = new ReservationDTO();
        reservation.setTenantId(2L);
        reservation.setPropertyId(1L);
        reservation.setTenantName("Jan");
        reservation.setTenantSurname("Kowalski");
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));
        reservation.setStatus(null);

        when(reservationClient.getReservationById(1L, 1L)).thenReturn(reservation);

        ApartmentDTO apartmentDTO = new ApartmentDTO();
        apartmentDTO.setLocation("Warszawa");
        apartmentDTO.setRooms(1);
        apartmentDTO.setPrice(new BigDecimal("1000"));

        when(arpartmentClient.getApartmentById(reservation.getPropertyId())).thenReturn(apartmentDTO);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reportService.createContract(request));
        assertEquals("Reservation Status is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenApartmentLocationMissing() {
        ContractRequest request = validRequest();
        request.setReservationId(1L);
        request.setOwnerId(1L);
        request.setTenantId(2L);

        ReservationDTO reservation = new ReservationDTO();
        reservation.setTenantId(2L);
        reservation.setPropertyId(1L);
        reservation.setTenantName("Jan");
        reservation.setTenantSurname("Kowalski");
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));
        reservation.setStatus("CONFIRMED");

        ApartmentDTO apartment = new ApartmentDTO();
        apartment.setLocation(null);
        apartment.setRooms(2);
        apartment.setPrice(BigDecimal.valueOf(2000));

        when(reservationClient.getReservationById(1L, 1L)).thenReturn(reservation);
        when(arpartmentClient.getApartmentById(1L)).thenReturn(apartment);

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Marek");
        userDTO.setLastName("Ogloza");

        when(userClient.getUserById(2L)).thenReturn(userDTO);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> reportService.createContract(request));
        assertEquals("Location not found", ex.getMessage());
    }

    @Test
    void shouldCreateReportPropertySuccessfully() throws JRException {
        // Tworzymy property do testu
        Property property = new Property();
        property.setRentalType("LONG_TERM");
        property.setAvailable(true);
        property.setLatitude(52.2297);
        property.setLongitude(21.0122);
        property.setLocation("Warszawa");
        property.setRooms(3);
        property.setPrice(BigDecimal.valueOf(2500));
        
        ReportService spyService = Mockito.spy(reportService);
        
        doReturn(property).when(spyService).getProperty(1L);

        JasperPrint jasperPrint = spyService.createReportProperty(1L);

        assertNotNull(jasperPrint);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRentalTypeInvalid() {
        Property property = new Property();
        property.setRentalType("INVALID_TYPE");
        property.setAvailable(true);
        property.setLatitude(52.2297);
        property.setLongitude(21.0122);
        property.setLocation("Warszawa");
        property.setRooms(3);
        property.setPrice(BigDecimal.valueOf(2500));

        ReportService spyService = Mockito.spy(reportService);
        doReturn(property).when(spyService).getProperty(1L);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> spyService.createReportProperty(1L));
        assertEquals("Rental Type not found", ex.getMessage());
    }


    @Test
    void createReportPropertyStats_ShouldReturnJasperPrint_WhenValidId() throws JRException {
        Long testId = 1L;

        ApartmentDTO apartmentDTO = new ApartmentDTO();
        apartmentDTO.setId(testId);
        apartmentDTO.setLatitude(52.0);
        apartmentDTO.setLongitude(21.0);
        apartmentDTO.setLocation("Warsaw");
        apartmentDTO.setAverageRating(BigDecimal.valueOf(4.5));
        apartmentDTO.setViewCount(100L);
        apartmentDTO.setRatingCount(20);

        when(arpartmentClient.getApartmentById(testId)).thenReturn(apartmentDTO);
        
        JasperPrint jasperPrint = reportService.createReportPropertyStats(testId);
        
        assertNotNull(jasperPrint, "Raport nie może być nullem");
    }

    private ReservationDTO createReservationDTO() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1L);
        dto.setPropertyId(10L);
        dto.setTenantId(100L);
        dto.setTenantName("Jan");
        dto.setTenantSurname("Kowalski");
        dto.setStartDate(LocalDate.of(2025, 6, 1));
        dto.setEndDate(LocalDate.of(2025, 6, 10));
        dto.setStatus("CONFIRMED");
        return dto;
    }

    @Test
    void createReportReservation_ShouldReturnJasperPrint_WhenValidStatus() throws JRException {
        Long reservationId = 1L;
        Long ownerId = 2L;
        
        ReservationDTO dto = createReservationDTO();
        when(reservationClient.getReservationById(reservationId, ownerId)).thenReturn(dto);

        JasperPrint result = reportService.createReportReservation(reservationId, ownerId);

        assertNotNull(result, "Raport nie powinien być null");
    }


}
