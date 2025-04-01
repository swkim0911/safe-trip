package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.request.SignUpRequest;
import com.swkim.safetrip.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("[POST] /users 요청시 회원가입을 한다.")
    void signup() throws Exception {
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("username")
                .password("password")
                .name("nickname")
                .phoneNumber("010-1234-5678")
                .email("test@gmail.com")
                .build();
        //when
        when(userService.enroll(signUpRequest)).thenReturn(1L);
        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(0L));

    }
}
