package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.jwt.JwtProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;


    @Test
    void 로그인_요청_성공시_액세스_토큰과_리프레시_토큰을_발급한다() {
        String email = "test@gmail.com";
        String password = "password";

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        String accessToken = "im.access.token";
        String refreshToken = "im.refresh.token";

        User mockUser = User.builder()
                .email(email)
                .password(password)
                .nickname("nickname")
                .role(Role.USER)
                .build();

        given(jwtProvider.issueAccessToken(email, Role.USER)).willReturn(accessToken);
        given(jwtProvider.issueRefreshToken(email)).willReturn(refreshToken);
        given(jwtProvider.createRefreshTokenCookie(refreshToken)).willReturn(ResponseCookie.from("refreshToken", refreshToken).build());
        given(userService.findUserByEmail(email)).willReturn(Optional.of(mockUser));

        // when
        AuthTokensResponseDto authTokensResponseDto = authService.login(loginRequest);

        // then
        AccessTokenResponse accessTokenResponse = authTokensResponseDto.getAccessTokenResponse();
        ResponseCookie refreshTokenCookie = authTokensResponseDto.getRefreshTokenCookie();

        Assertions.assertThat(accessTokenResponse.getAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(refreshTokenCookie.getValue()).isEqualTo(refreshToken);
    }

    @Test
    void 로그인_요청에_존재하지_않은_이메일_입력시_예외가_발생한다() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(
                "notfound@email.com",
                "password"
        );
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        Assertions.assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProvider, never()).issueAccessToken(any(), any(Role.class));
        verify(jwtProvider, never()).issueRefreshToken(any());
    }

    @Test
    void 로그인_요청에_잘못된_비밀번호_입력시_예외가_발생한다() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(
                "test@email.com",
                "wrongPassword"
        );

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        Assertions.assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtProvider, never()).issueAccessToken(any(), any(Role.class));
        verify(jwtProvider, never()).issueRefreshToken(any());
    }

}