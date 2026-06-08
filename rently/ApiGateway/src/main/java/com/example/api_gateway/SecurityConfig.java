package com.example.api_gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers("/helloworldmicro1/v3/api-docs/**").permitAll()
//                        .pathMatchers("/helloworldmicro2/v3/api-docs/**").permitAll()
//                        .pathMatchers("/reservation/v3/api-docs/**").permitAll()
//
//                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
                        .pathMatchers("/login/**", "/oauth2/**", "/api/auth/register", "/css/**", "/js/**",
                                "/report/**")
                        .permitAll()

                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .logout(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
//package com.example.api_gateway;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/login/**", "/oauth2/**","/api/auth/register", "/css/**", "/js/**","/swagger-ui.html", "/swagger-ui/**","/v3/api-docs/**").permitAll()
//                        .pathMatchers("/rentlyauth/swagger-ui/**", "/rentlyauth/swagger-ui.html").permitAll()
//                        .pathMatchers("/rentlyauth/v3/api-docs/**", "/rentlyauth/v3/api-docs/swagger-config").permitAll()
//                        .anyExchange().authenticated()
//                )
//                .oauth2Login(Customizer.withDefaults())
//                .oauth2Client(Customizer.withDefaults())
//                .logout(Customizer.withDefaults())
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//}