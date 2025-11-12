package com.swkim.safetrip.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        userRepository.save(User.builder()
                .email("test@gmail.com")
                .password(passwordEncoder.encode("password"))
                .nickname("nickname")
                .role(Role.USER)
                .build());
    }

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    void 로그인_요청시_존재하지_않는_email이면_401_에러코드를_반환한다() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("test@gmail.com", "wrongPassword");

        // when then
       mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.result").value("Authentication Failed"));
    }

    @Test
    void 로그인_요청시_잘못된_비밀번호를_입력하면_401_에러코드를_반환한다() throws Exception {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest("right@example.com", "password");

        // when then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.result").value("Authentication Failed"));

    }
}
