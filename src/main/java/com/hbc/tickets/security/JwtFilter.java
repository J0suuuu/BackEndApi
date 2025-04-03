package com.hbc.tickets.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hbc.tickets.controller.JwtUtil;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Primero, buscar el token en las cookies
        String token = getTokenFromCookies(request);

        // Si no se encuentra en las cookies, buscar en el header de Authorization
        if (token == null) {
            token = getTokenFromHeader(request);
        }

        // Verificar si se ha encontrado un token
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            System.out.println("Token validado. Username extraído: " + username);

            UserDetails userDetails = new User(username, "", Collections.emptyList());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            System.out.println("Token no válido o no presente.");
        }

        chain.doFilter(request, response);
    }

    // Método para extraer el token de las cookies
    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT".equals(cookie.getName())) { // Suponiendo que la cookie se llama 'JWT'
                    System.out.println("Token encontrado en cookies: " + cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Método para extraer el token del header Authorization
    private String getTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            System.out.println("Token encontrado en header Authorization: " + authHeader.substring(7));
            return authHeader.substring(7); // Extraer el token después de "Bearer "
        }
        return null;
    }
}
