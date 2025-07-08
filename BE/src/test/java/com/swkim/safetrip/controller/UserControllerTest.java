package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.config.SecurityConfig;
import com.swkim.safetrip.dto.JwtDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.LoginService;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    private LoginService loginService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

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
                        .content(new ObjectMapper().writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(1L));
    }

    @Test
    void 로그인_요청_성공시_리프레시_토큰은_쿠키_액세스_토큰은_바디로_반환한다() throws Exception {
        // given
        String email = "test@gmail.com";
        String password = "password";

        UserLoginRequest loginRequest = UserLoginRequest
                .builder()
                .email(email)
                .password(password)
                .build();

        String accessToken = "im.access.token";
        String refreshToken = "im.refresh.token";

        JwtDto jwtDto = JwtDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        // when
        when(userService.login(any(UserLoginRequest.class))).thenReturn(jwtDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.result.accessToken").value(accessToken));

        verify(userService).login(any(UserLoginRequest.class));
        verify(jwtUtils).createRefreshTokenCookie(eq("im.refresh.token"));
    }
}
