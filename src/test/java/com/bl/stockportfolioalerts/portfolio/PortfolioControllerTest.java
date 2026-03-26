package com.bl.stockportfolioalerts.portfolio;

import com.bl.stockportfolioalerts.alert.repository.AlertRepository;
import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import com.bl.stockportfolioalerts.portfolio.dto.CreatePortfolioRequest;
import com.bl.stockportfolioalerts.portfolio.dto.HoldingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setup() {
        alertRepository.deleteAll();   // ✅ FIRST (child)
        userRepository.deleteAll();    // ✅ THEN (parent)

        User user = new User();
        user.setEmail("testuser@email.com");
        user.setName("testuser");
        user.setPassword("password");

        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void shouldCreatePortfolio() throws Exception {

        HoldingRequest h = new HoldingRequest("AAPL", 10, 150);
        CreatePortfolioRequest req = new CreatePortfolioRequest(List.of(h));

        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }
}