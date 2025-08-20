package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.global.exception.custom.RefreshTokenExpiredException;
import com.swkim.safetrip.global.exception.custom.RefreshTokenMissingException;
import com.swkim.safetrip.jwt.JwtProvider;
import com.swkim.safetrip.security.config.SecurityConfig;
import com.swkim.safetrip.service.AuthService;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void 로그인_요청_성공시_리프레시_토큰은_쿠키로_반환하고_액세스_토큰은_바디로_반환한다() throws Exception {
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

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();

        AuthTokensResponseDto authTokensResponseDto = AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(ResponseCookie.from("refreshToken", refreshToken).build())
                .build();

        // when
        when(authService.login(any(UserLoginRequest.class))).thenReturn(authTokensResponseDto);

        // then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.result.accessToken").value(accessToken))
                .andExpect(cookie().value("refreshToken", refreshToken));

    }

    @Test
    void 액세스_토큰_재발급_요청시_리프레시_토큰이_없다면_400_예외가_발생한다() throws Exception {
        // give & when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh"));

        // then
        resultActions
                .andExpect(status().isBadRequest()) // 예외에 따라 상태코드 조정
                .andExpect(result -> assertInstanceOf(RefreshTokenMissingException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Refresh token is empty"));
    }

    @Test
    void 액세스_토큰_재발급_요청시_쿠키에_리프레시_토큰이_없다면_400_예외가_발생한다() throws Exception {
        // give & when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", ""))); // 빈 문자열 전달
        // when
        resultActions
                .andExpect(status().isBadRequest()) // 예외 매핑에 따라 조정
                .andExpect(result -> assertInstanceOf(RefreshTokenMissingException.class, result.getResolvedException()));
    }

    @Test
    void 액세스_토큰_재발급_요청시_쿠키의_이름이_refreshToken이_아니면_400_예외가_발생한다() throws Exception {
        // give & when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("wrongName", "im.refresh.token")));
        // when
        resultActions
                .andExpect(status().isBadRequest()) // 예외 매핑에 따라 조정
                .andExpect(result -> assertInstanceOf(RefreshTokenMissingException.class, result.getResolvedException()));
    }

    @Test
    void 액세스_토큰_재발급_요청시_리프레시_토큰이_만료되었다면_401_예외가_발생한다() throws Exception {
        // given
        String expiredRefreshToken = "expired.refresh.token";

        given(authService.reIssueAccessToken(expiredRefreshToken))
                .willThrow(new RefreshTokenExpiredException());

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", expiredRefreshToken)));

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(RefreshTokenExpiredException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token is expired"));
    }

    @Test
    void 액세스_토큰_재발급_요청시_리프레시_토큰이_유효하지_않다면_401_예외가_발생한다() throws Exception {
        // given
        String inValidRefreshToken = "invalid.refresh.token";

        given(authService.reIssueAccessToken(inValidRefreshToken))
                .willThrow(new InvalidRefreshTokenException());

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", inValidRefreshToken)));
        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertInstanceOf(InvalidRefreshTokenException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid"));
    }

    @Test
    void 액세스_토큰_재발급_요청시_리프레시_토큰이_유효하지만_대응하는_유저를_찾을_수_없는_경우_401_에러가_발생한다() throws Exception {
        // given
        String refreshToken = "im.refresh.token";

        given(jwtProvider.verifyRefreshToken(refreshToken)).willReturn(any(String.class));
        given(authService.reIssueAccessToken(refreshToken))
                .willThrow(new InvalidRefreshTokenException());

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)));
        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid"));
    }

    @Test
    void 액세스_토큰_재발급_요청이_성공일_경우_200_응답을_보낸다() throws Exception {
        // given
        String validRefreshToken = "valid.refresh.token";
        String reIssuedAccessToken = "new.access.token";
        String reIssuedRefreshToken = "new.refresh.token";

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", reIssuedRefreshToken).build();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(reIssuedAccessToken)
                .build();

        AuthTokensResponseDto tokensResponseDto = AuthTokensResponseDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
        given(authService.reIssueAccessToken(validRefreshToken)).willReturn(tokensResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", validRefreshToken)));
        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Access Token is reissued under RTR"))
                .andExpect(jsonPath("$.result.accessToken").value(reIssuedAccessToken));
    }

}