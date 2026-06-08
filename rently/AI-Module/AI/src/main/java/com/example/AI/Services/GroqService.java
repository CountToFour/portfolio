package com.example.AI.Services;

import com.example.AI.Exceptions.GroqApiException;
import com.example.AI.Payloads.Requests.PriceCityRequest;
import com.example.AI.Payloads.Requests.PriceEstimateRequest;
import com.example.AI.Payloads.Requests.PriceRateRequest;
import com.example.AI.Payloads.Requests.PriceTrendRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${groq.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Mono<String> askGroq(String prompt) {
        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "llama-3.3-70b-versatile",
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are a helpful real estate assistant."),
                                Map.of("role", "user", "content", prompt)
                        )
                ))
                .retrieve()
                .bodyToMono(String.class);
    }

    public String ratePrice(@RequestBody PriceRateRequest request) {
        String prompt = String.format(
                "Given the following apartment data:\n" +
                        "- Location: %s\n" +
                        "- Latitude: %f\n" +
                        "- Longitude: %f\n" +
                        "- Number of rooms: %d\n" +
                        "- Price: %.2f PLN\n\n" +
                        "- Living area %.2f squared meters\n\n" +
                        "Is the price fair compared to the market in this location? Justify shortly. You have to give an answer.",
                request.getLocation(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRooms(),
                request.getPrice(),
                request.getArea()
        );

        return askGroq(prompt)
                .map(responseJson -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        return root.get("choices")
                                .get(0)
                                .get("message")
                                .get("content")
                                .asText()
                                .trim();
                    } catch (Exception e) {
                        throw new GroqApiException("Failed to parse response from Groq");
                    }
                })
                .block();
    }

    public String estimatePrice(@RequestBody PriceEstimateRequest request) {
        String prompt = String.format(
                "Given the following apartment data:\n" +
                        "- Location: %s\n" +
                        "- Latitude: %f\n" +
                        "- Longitude: %f\n" +
                        "- Number of rooms: %d\n" +
                        "- Living area %.2f squared meters\n\n" +
                        "Please provide your estimated fair market price for this apartment in PLN. Justify shortly. You have to give an answer.",
                request.getLocation(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRooms(),
                request.getArea()
        );

        return askGroq(prompt)
                .map(responseJson -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        return root.get("choices")
                                .get(0)
                                .get("message")
                                .get("content")
                                .asText()
                                .trim();
                    } catch (Exception e) {
                        throw new GroqApiException("Failed to parse response from Groq");
                    }
                })
                .block();
    }

    public String getTrends(@RequestBody PriceTrendRequest request) {
        String prompt = String.format(
                "Given the following apartment data:\n" +
                        "- Location: %s\n" +
                        "- Latitude: %f\n" +
                        "- Longitude: %f\n" +
                        "Analyze market trends for apartments in Warsaw in 2025. Maximum 2 sentences without unneccessary melting.",
                request.getLocation(),
                request.getLatitude(),
                request.getLongitude()
        );

        return askGroq(prompt)
                .map(responseJson -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        return root.get("choices")
                                .get(0)
                                .get("message")
                                .get("content")
                                .asText()
                                .trim();
                    } catch (Exception e) {
                        throw new GroqApiException("Failed to parse response from Groq");
                    }
                })
                .block();
    }

    public String cityInfo(@RequestBody PriceCityRequest request) {
        String prompt = String.format(
                "Given the following apartment data:\n" +
                        "- Location: %s\n" +
                        "Analyze marketplace in this city and tell me about the most expensive, the least expensive and the most progressing places to buy an estate. Justify in maximum 1 sentence about every nearby without unneccessary melting.",
                request.getLocation()
        );

        return askGroq(prompt)
                .map(responseJson -> {
                    try {
                        JsonNode root = objectMapper.readTree(responseJson);
                        return root.get("choices")
                                .get(0)
                                .get("message")
                                .get("content")
                                .asText()
                                .trim();
                    } catch (Exception e) {
                        throw new GroqApiException("Failed to parse response from Groq");
                    }
                })
                .block();
    }

}
