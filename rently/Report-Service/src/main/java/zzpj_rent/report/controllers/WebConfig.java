package zzpj_rent.report.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Możesz zawęzić do "/report/**"
                .allowedOrigins("*") // W środowisku produkcyjnym podaj konkretny adres bramki!
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .exposedHeaders("Content-Disposition"); // <-- TO JEST KLUCZOWA LINIA
    }
}
