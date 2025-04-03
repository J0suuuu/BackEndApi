package com.hbc.tickets.controller;

import com.hbc.tickets.dto.JwtResponse;
import com.hbc.tickets.dto.LoginResponse;
import com.hbc.tickets.dto.ProfileResponse;
import com.hbc.tickets.dto.UsernameRequest;
import com.hbc.tickets.model.Role;
import com.hbc.tickets.model.User;
import com.hbc.tickets.repository.UserRepository;
import com.hbc.tickets.service.EmailService;
import com.hbc.tickets.util.PasswordUtil;

import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/update-email")
    public ResponseEntity<String> updateEmail(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, String> request) {
        
        String newEmail = request.get("email");

        // Verificar que el nuevo email no esté vacío
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("El nuevo correo electrónico no puede estar vacío.");
        }

        // Verificar que el nuevo email tenga formato válido
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("El formato del correo electrónico es inválido.");
        }

        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);

        // Verificar la validez del token
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        // Extraer el nombre de usuario del token
        String username = claims.getSubject();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Verificar si el nuevo correo electrónico ya está en uso
        if (userRepository.existsByEmail(newEmail)) {
            return ResponseEntity.badRequest().body("El correo electrónico ya está en uso.");
        }

        // Actualizar el correo electrónico
        user.setEmail(newEmail);
        userRepository.save(user);

        return ResponseEntity.ok("Correo electrónico actualizado correctamente.");
    }
    
    @GetMapping("/profile/me")
    public ResponseEntity<?> getProfileMe(@RequestHeader("Authorization") String token) {
        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);

        // Verificar la validez del token y extraer el nombre de usuario
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        String loggedInUsername = claims.getSubject(); // El nombre de usuario extraído del token
        User loggedInUser = userRepository.findByUsername(loggedInUsername);

        if (loggedInUser == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Convertir el usuario autenticado a la respuesta esperada
        ProfileResponse profileResponse = new ProfileResponse(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                loggedInUser.getEmail(),
                loggedInUser.getRole()
        );

        return ResponseEntity.ok(profileResponse);
    }

    
    @GetMapping("/profile/all")
    public ResponseEntity<?> getAllProfiles(@RequestHeader("Authorization") String token) {
        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);

        // Verificar la validez del token y extraer el nombre de usuario
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        String loggedInUsername = claims.getSubject(); // El nombre de usuario extraído del token
        User loggedInUser = userRepository.findByUsername(loggedInUsername);

        if (loggedInUser == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Si el usuario es un Owner, puede ver todos los usuarios
        if (loggedInUser.getRole() == Role.OWNER || loggedInUser.getRole() == Role.ADMINISTRADOR) {
            List<User> allUsers = userRepository.findAll();

            // Convertir la lista de usuarios a la respuesta esperada, incluyendo el id
            List<ProfileResponse> profileResponses = new ArrayList<>();
            for (User user : allUsers) {
                profileResponses.add(new ProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
            }

            return ResponseEntity.ok(profileResponses);
        }

        return ResponseEntity.status(403).body("No tienes permisos para acceder a esta información.");
    }




    
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);

        // Verificar la validez del token y extraer el nombre de usuario
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        String loggedInUsername = claims.getSubject(); // El nombre de usuario extraído del token
        User loggedInUser = userRepository.findByUsername(loggedInUsername);

        if (loggedInUser == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Si el usuario tiene el rol de ADMINISTRADOR o OWNER, puede ver el perfil de cualquier usuario
        if (loggedInUser.getRole() == Role.ADMINISTRADOR || loggedInUser.getRole() == Role.OWNER) {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado.");
            }

            User user = userOpt.get();
            // Mostrar email, username y rol del usuario
            return ResponseEntity.ok(new ProfileResponse(id, user.getUsername(), user.getEmail(), user.getRole()));
        }

        // Si el usuario no es ADMINISTRADOR ni OWNER, solo puede ver su propio perfil
        if (loggedInUser.getId().equals(id)) {
            return ResponseEntity.ok(new ProfileResponse(id, loggedInUser.getUsername(), loggedInUser.getEmail(), loggedInUser.getRole()));
        }

        // Si intenta acceder a otro perfil, retornar mensaje de error
        return ResponseEntity.status(403).body("Este no es tu perfil.");
    }

    
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, String> request) {
        
        String newPassword = request.get("password");

        // Validar la nueva contraseña (asegurarse de que cumple con las reglas)
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);
        
        // Verificar la validez del token
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        // Extraer el nombre de usuario del token
        String username = claims.getSubject();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Encriptar y establecer la nueva contraseña (mismo flujo que en forgot-password)
        user.setPassword(passwordEncoder.encode(newPassword)); // Encriptamos la nueva contraseña
        userRepository.save(user);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }


    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody UsernameRequest usernameRequest) {
        User user = null;

        // Verificar si se proporcionó un nombre de usuario
        if (usernameRequest.getUsername() != null && !usernameRequest.getUsername().isEmpty()) {
            user = userRepository.findByUsername(usernameRequest.getUsername());
        }

        // Verificar si se proporcionó un correo electrónico
        if (user == null && usernameRequest.getEmail() != null && !usernameRequest.getEmail().isEmpty()) {
            user = userRepository.findByEmail(usernameRequest.getEmail());
        }

        if (user == null) {
            return ResponseEntity.badRequest().body("Usuario o correo electrónico no encontrado.");
        }

        // Generamos una nueva contraseña aleatoria
        String newPassword = PasswordUtil.generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Enviamos el correo con la nueva contraseña
        try {
            String emailContent = String.format(
                    "Tu usuario: %s\nTu nueva contraseña: %s", 
                    user.getUsername(), newPassword);
            emailService.sendEmail(user.getEmail(), "Recuperación de contraseña", emailContent);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Error al enviar el correo electrónico.");
        }

        return ResponseEntity.ok("Se ha enviado un correo con tu nueva contraseña.");
    }



    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() ||
            user.getEmail() == null || user.getEmail().isEmpty() ||
            user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Todos los campos son obligatorios.");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está en uso.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CLIENTE);
        userRepository.save(user);

        return ResponseEntity.ok("Usuario registrado exitosamente como CLIENTE.");
    }

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        // Buscar el usuario por nombre de usuario
        User existingUser = userRepository.findByUsername(user.getUsername());

        // Verificar si el usuario existe y si las contraseñas coinciden
        if (existingUser == null || !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas.");
        }

        // Generar el token JWT
        String token = jwtUtil.generateToken(existingUser.getUsername());

        // Crear una respuesta con el token y el rol
        LoginResponse loginResponse = new LoginResponse(token, existingUser.getRole());

        return ResponseEntity.ok(loginResponse);
    }

    // ✅ NUEVA FUNCIÓN: Modificar usuario (Solo Administradores)
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            @RequestHeader("Authorization") String token) {

        // Extraer username del token
        String adminUsername = jwtUtil.extractUsername(token.substring(7));
        User adminUser = userRepository.findByUsername(adminUsername);

        if (adminUser == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Verificar si el usuario autenticado es un Owner
        if (adminUser.getRole() == Role.OWNER) {
            // El Owner puede modificar cualquier usuario, incluyendo cambiar roles a ADMINISTRADOR o OWNER
            return modifyUser(id, updatedUser);
        }

        // Verificar si el usuario autenticado es un Administrador
        if (adminUser.getRole() == Role.ADMINISTRADOR) {
            // Los administradores no pueden otorgar el rol ADMINISTRADOR ni OWNER
            if (updatedUser.getRole() == Role.ADMINISTRADOR || updatedUser.getRole() == Role.OWNER) {
                return ResponseEntity.status(403).body("No puedes otorgar el rol ADMINISTRADOR ni OWNER.");
            }

            return modifyUser(id, updatedUser);
        }

        return ResponseEntity.status(403).body("No tienes permisos para modificar usuarios.");
    }

    private ResponseEntity<String> modifyUser(Long id, User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }

        User existingUser = existingUserOpt.get();

        // Verificar si el nombre de usuario o el correo electrónico ya están en uso
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(existingUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso.");
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está en uso.");
        }

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }

        userRepository.save(existingUser);
        return ResponseEntity.ok("Usuario actualizado correctamente.");
    }
    @DeleteMapping("/selfdelete")
    public ResponseEntity<String> selfDelete(@RequestHeader("Authorization") String token) {
        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.substring(7);

        // Verificar la validez del token
        Claims claims = jwtUtil.getClaims(jwtToken);
        if (claims == null) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        // Extraer el nombre de usuario del token
        String username = claims.getSubject();
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        // Eliminar el usuario de la base de datos
        userRepository.delete(user);

        // Responder con éxito
        return ResponseEntity.ok("Cuenta eliminada correctamente.");
    }

    
}