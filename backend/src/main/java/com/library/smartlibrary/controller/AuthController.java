package com.library.smartlibrary.controller;

import com.library.smartlibrary.dto.LoginRequest;
import com.library.smartlibrary.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request != null && 
            "admin".equalsIgnoreCase(request.getUsername()) && 
            "admin123".equals(request.getPassword())) {
            String token = "lib-" + UUID.randomUUID().toString();
            return ResponseEntity.ok(new LoginResponse(
                    true,
                    token,
                    "Librarian Admin",
                    "ADMIN",
                    "Login successful"
            ));
        }

        // Accept demo login for college project demonstration
        if (request != null && request.getUsername() != null && !request.getUsername().trim().isEmpty() 
            && "admin".equalsIgnoreCase(request.getUsername()) == false 
            && request.getPassword() != null && request.getPassword().length() >= 4) {
            String token = "user-" + UUID.randomUUID().toString();
            return ResponseEntity.ok(new LoginResponse(
                    true,
                    token,
                    request.getUsername(),
                    "LIBRARIAN",
                    "Welcome, " + request.getUsername()
            ));
        }

        return ResponseEntity.status(401).body(new LoginResponse(
                false,
                null,
                null,
                null,
                "Invalid username or password. Default is admin / admin123"
        ));
    }
}
