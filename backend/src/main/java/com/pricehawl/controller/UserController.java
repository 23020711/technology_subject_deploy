package com.pricehawl.controller;

import com.pricehawl.dto.*;
import com.pricehawl.entity.User;
import com.pricehawl.security.UserPrincipal;
import com.pricehawl.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = {"/users", "/api/users"})
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        UUID userId = UUID.fromString(auth.getName());

        String email = "unknown@email.com";
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            email = ((UserPrincipal) principal).getEmail();
        }

        return ResponseEntity.ok(service.getOrCreate(userId, email));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> update(Authentication auth,
                       @RequestBody UpdateUserRequest req) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(service.update(userId, req));
    }

    @PatchMapping("/me/preferences")
    public ResponseEntity<?> preferences(Authentication auth,
                            @RequestBody UpdatePreferencesRequest req) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(service.updatePreferences(userId, req));
    }
}