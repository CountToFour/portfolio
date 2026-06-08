package com.example.AI;

import com.example.AI.Controllers.GroqController;
import com.example.AI.Exceptions.RecommendationException;
import com.example.AI.Microservices.ApartmentClient;
import com.example.AI.Payloads.Requests.*;
import com.example.AI.Payloads.Responses.ApartmentDTO;
import com.example.AI.Payloads.Responses.GroqResponse;
import com.example.AI.Services.GroqService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroqControllerTest {

    @Mock
    private GroqService groqService;

    @Mock
    private ApartmentClient apartmentClient;

    @InjectMocks
    private GroqController groqController;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ratePrice_shouldReturnGroqResponse() {
        PriceRateRequest request = new PriceRateRequest();
        request.setPrice(null);

        String mockAnswer = "Price seems fair";
        when(groqService.ratePrice(request)).thenReturn(mockAnswer);

        ResponseEntity<GroqResponse> response = groqController.ask(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOutput()).isEqualTo(mockAnswer);
    }

    @Test
    void estimatePrice_shouldReturnGroqResponse() {
        PriceEstimateRequest request = new PriceEstimateRequest();

        String mockAnswer = "Estimated price is 1000";
        when(groqService.estimatePrice(request)).thenReturn(mockAnswer);

        ResponseEntity<GroqResponse> response = groqController.estimate(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getOutput()).isEqualTo(mockAnswer);
    }

    @Test
    void trendPrice_shouldReturnGroqResponse() {
        PriceTrendRequest request = new PriceTrendRequest();

        String mockAnswer = "Prices rising steadily";
        when(groqService.getTrends(request)).thenReturn(mockAnswer);

        ResponseEntity<GroqResponse> response = groqController.trend(request);

        assertThat(response.getBody().getOutput()).isEqualTo(mockAnswer);
    }

    @Test
    void cityPrice_shouldReturnGroqResponse() {
        PriceCityRequest request = new PriceCityRequest();

        String mockAnswer = "City price data";
        when(groqService.cityInfo(request)).thenReturn(mockAnswer);

        ResponseEntity<GroqResponse> response = groqController.cityPrice(request);

        assertThat(response.getBody().getOutput()).isEqualTo(mockAnswer);
    }

    @Test
    void recommend_shouldThrowRecommendationException_whenExitCodeNotZero() throws Exception {
        RecommendationRequest request = new RecommendationRequest();
        when(apartmentClient.getApartments()).thenReturn(List.of(new ApartmentDTO()));

        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockProcess.waitFor()).thenReturn(1);

    }
}
