package com.bl.stockportfolioalerts.auth.controller;

import com.bl.stockportfolioalerts.auth.dto.RegisterRequest;
import com.bl.stockportfolioalerts.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {

        userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }
}
