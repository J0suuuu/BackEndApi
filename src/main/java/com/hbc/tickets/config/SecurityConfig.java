package com.hbc.tickets.config;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hbc.tickets.security.JwtFilter;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource()) // Habilitar CORS
            .and()
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/events/filter/**", "/uploaded-images/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Use allowedOriginPatterns instead of allowedOrigin when dealing with credentials.
        configuration.addAllowedOriginPattern("http://192.168.1.88:8081"); // Allow React app on localhost:3000
        // If you have other domains, add them like this:
        // configuration.addAllowedOriginPattern("https://your-other-domain.com");

        configuration.addAllowedMethod("*"); // Permitir todos los métodos (GET, POST, PUT, DELETE, etc.)
        configuration.addAllowedHeader("*"); // Permitir todos los encabezados
        configuration.setAllowCredentials(true); // Permitir el envío de credenciales (cookies, etc.)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public Pbkdf2PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder("mySecretKey", 16, 310000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    }
}
