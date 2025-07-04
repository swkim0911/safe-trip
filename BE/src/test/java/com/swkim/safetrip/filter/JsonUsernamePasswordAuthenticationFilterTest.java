package com.swkim.safetrip.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JsonUsernamePasswordAuthenticationFilterTest {

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
                .email("swkim-test@gmail.com")
                .password(passwordEncoder.encode("password"))
                .nickname("swkim")
                        .role(Role.USER)
                .build());
    }

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 요청시, 잘못된 아이디를 입력한 경우 401 에러가 발생한다.")
    void return_401_when_email_is_incorrect() throws Exception{
        Map<String, String> loginRequest = Map.of(
                "email", "wrong-email@gmail.com",
                "password", "password"
        );

        // when then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Bad credentials"));

    }

    @Test
    @DisplayName("로그인 요청시, 잘못된 비밀번호를 입력한 경우 401 에러가 발생한다.")
    void return_401_when_password_is_incorrect() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "email", "swkim-test@gmail.com",
                "password", "wrong-password"
        );

        // when then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }
}
