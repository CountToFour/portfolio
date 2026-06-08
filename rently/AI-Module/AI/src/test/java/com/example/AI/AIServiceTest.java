package com.example.AI;
import com.example.AI.Payloads.Requests.*;
import com.example.AI.Services.GroqService;
import com.example.AI.Exceptions.GroqApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GroqServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GroqService groqService;

    @BeforeEach
    void setup() {
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void mockGroqApiResponse(String messageContent) {
        String jsonResponse = String.format("""
            {
                "choices": [
                    {
                        "message": {
                            "content": "%s"
                        }
                    }
                ]
            }
            """, messageContent);

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));
    }

    @Test
    void ratePrice_shouldReturnParsedAnswer() {
        String expectedAnswer = "The price seems fair.";
        mockGroqApiResponse(expectedAnswer);

        PriceRateRequest request = new PriceRateRequest();
        request.setLocation("Warsaw");
        request.setLatitude(52.2297);
        request.setLongitude(21.0122);
        request.setRooms(2);
        request.setPrice(java.math.BigDecimal.valueOf(1000));
        request.setArea(40);

        String result = groqService.ratePrice(request);

        assertThat(result).isEqualTo(expectedAnswer);
    }

    @Test
    void estimatePrice_shouldReturnParsedAnswer() {
        String expectedAnswer = "Estimated price is 1200 PLN.";
        mockGroqApiResponse(expectedAnswer);

        PriceEstimateRequest request = new PriceEstimateRequest();
        request.setLocation("Krakow");
        request.setLatitude(50.0647);
        request.setLongitude(19.9450);
        request.setRooms(3);
        request.setArea(60);

        String result = groqService.estimatePrice(request);

        assertThat(result).isEqualTo(expectedAnswer);
    }

    @Test
    void getTrends_shouldReturnParsedAnswer() {
        String expectedAnswer = "Prices are increasing in 2025.";
        mockGroqApiResponse(expectedAnswer);

        PriceTrendRequest request = new PriceTrendRequest();
        request.setLocation("Warsaw");
        request.setLatitude(52.2297);
        request.setLongitude(21.0122);

        String result = groqService.getTrends(request);

        assertThat(result).isEqualTo(expectedAnswer);
    }

    @Test
    void cityInfo_shouldReturnParsedAnswer() {
        String expectedAnswer = "Most expensive area is Śródmieście.";
        mockGroqApiResponse(expectedAnswer);

        PriceCityRequest request = new PriceCityRequest();
        request.setLocation("Warsaw");

        String result = groqService.cityInfo(request);

        assertThat(result).isEqualTo(expectedAnswer);
    }

    @Test
    void shouldThrowException_whenResponseIsMalformed() {
        String badJson = "{ \"wrong\": \"structure\" }";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(badJson));

        PriceRateRequest request = new PriceRateRequest();
        request.setLocation("Warsaw");
        request.setLatitude(52.2297);
        request.setLongitude(21.0122);
        request.setRooms(2);
        request.setPrice(java.math.BigDecimal.valueOf(1000));
        request.setArea(40);

        assertThatThrownBy(() -> groqService.ratePrice(request))
                .isInstanceOf(GroqApiException.class)
                .hasMessageContaining("Failed to parse response from Groq");
    }
}
