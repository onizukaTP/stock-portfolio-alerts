package com.bl.stockportfolioalerts.alert;

import com.bl.stockportfolioalerts.alert.dto.CreateAlertRequest;
import com.bl.stockportfolioalerts.alert.service.AlertService;

import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AlertServiceTest {

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAlert() {

        // 1. Set authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser@email.com", null, List.of())
        );
        SecurityContextHolder.setContext(context);

        // 2. Insert user into DB
        User user = new User();
        user.setName("testuser");
        user.setEmail("testuser@email.com");
        user.setPassword("password");
        userRepository.save(user);

        // 3. Call service
        CreateAlertRequest request = CreateAlertRequest.builder()
                .ticker("AAPL")
                .thresholdPrice(150)
                .build();

        var alert = alertService.createAlert(request);

        // 4. Assertions
        assertThat(alert).isNotNull();
        assertThat(alert.getTicker()).isEqualTo("AAPL");
    }
}