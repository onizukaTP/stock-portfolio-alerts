package com.bl.stockportfolioalerts.auth.service;

import com.bl.stockportfolioalerts.auth.dto.RegisterRequest;
import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import com.bl.stockportfolioalerts.auth.validation.UserValidation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterRequest request) {

        if (!UserValidation.isValidEmail.test(request.getEmail())) {
            throw new IllegalArgumentException("Invalid Email");
        }

        if (!UserValidation.isStrongPassword.test(request.getPassword())) {
            throw new IllegalArgumentException("Weak Password");
        }

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already registered");
                });

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }
}

