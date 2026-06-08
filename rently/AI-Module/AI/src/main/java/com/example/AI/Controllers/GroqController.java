package com.example.AI.Controllers;

import com.example.AI.Exceptions.RecommendationException;
import com.example.AI.Microservices.ApartmentClient;
import com.example.AI.Payloads.Requests.*;
import com.example.AI.Payloads.Responses.ApartmentDTO;
import com.example.AI.Payloads.Responses.GroqResponse;
import com.example.AI.Services.GroqService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:3000/", maxAge = 3600)
public class GroqController {

    private final GroqService groqService;
    private final ApartmentClient apartmentClient;

    public GroqController(GroqService groqService, ApartmentClient apartmentClient) {
        this.groqService = groqService;
        this.apartmentClient = apartmentClient;
    }

    @PostMapping("/rate_price")
    public ResponseEntity<GroqResponse> ask(@RequestBody PriceRateRequest priceRateRequest) {
        GroqResponse response = new GroqResponse(this.groqService.ratePrice(priceRateRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/estimate_price")
    public ResponseEntity<GroqResponse> estimate(@RequestBody PriceEstimateRequest priceEstimateRequest) {
        GroqResponse response = new GroqResponse(this.groqService.estimatePrice(priceEstimateRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trend_price")
    public ResponseEntity<GroqResponse> trend(@RequestBody PriceTrendRequest priceTrendRequest) {
        GroqResponse response = new GroqResponse(this.groqService.getTrends(priceTrendRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/city_price")
    public ResponseEntity<GroqResponse> cityPrice(@RequestBody PriceCityRequest priceCityRequest) {
        GroqResponse response = new GroqResponse(this.groqService.cityInfo(priceCityRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommendation")
    public ResponseEntity<Object> recommend(@RequestBody RecommendationRequest recommendationRequest) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "recommendation.py");
        processBuilder.redirectErrorStream(true);

        List<ApartmentDTO> apartments = apartmentClient.getApartments();

        Process process = processBuilder.start();

        Map<String, Object> inputData = new HashMap<>();
        inputData.put("recommendationRequest", recommendationRequest);
        inputData.put("apartments", apartments);

        ObjectMapper objectMapper = new ObjectMapper();
        String inputJson = objectMapper.writeValueAsString(inputData);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(inputJson);
            writer.flush();
        }

        String outputJson = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines()
                .collect(Collectors.joining());

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RecommendationException("Recommendation script failed with exit code " + exitCode);
        }

        Object response = objectMapper.readValue(outputJson, Object.class);


        return ResponseEntity.ok(response);
    }
}
