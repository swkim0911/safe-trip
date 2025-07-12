package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.config.SecurityConfig;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void 회원가입_요청_성공시_201_응답을_반환한다() throws Exception {
        //given
        UserSignUpRequest signUpRequest = UserSignUpRequest.builder()
                .email("swkim@gmail.com")
                .password("password")
                .nickname("nickname")
                .build();

        //when
        when(userService.signup(any(UserSignUpRequest.class))).thenReturn(1L);

        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(1L));
    }
}
