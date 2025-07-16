package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.AuthTokensResponseDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

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
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String accessToken = "im.access.token";
        String refreshToken = "im.refresh.token";

        User mockUser = User.builder()
                .email(email)
                .password(password)
                .nickname("nickname")
                .role(Role.USER)
                .build();

        when(jwtUtils.issueAccessToken(email, Role.USER)).thenReturn(accessToken);
        when(jwtUtils.issueRefreshToken()).thenReturn(refreshToken);
        when(jwtUtils.createRefreshTokenCookie(refreshToken)).thenReturn(ResponseCookie.from("refreshToken", refreshToken).build());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // when
        AuthTokensResponseDto authTokensResponseDto = authService.login(loginRequest);
        AccessTokenResponse accessTokenResponse = authTokensResponseDto.getAccessTokenResponse();
        ResponseCookie refreshTokenCookie = authTokensResponseDto.getRefreshTokenCookie();


        // then
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

        // when & then
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));
        Assertions.assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).issueAccessToken(any(), any(Role.class));
        verify(jwtUtils, never()).issueRefreshToken();
    }

    @Test
    void 로그인_요청에_잘못된_비밀번호_입력시_예외가_발생한다() {
        // given
        UserLoginRequest loginRequest = new UserLoginRequest(
                "test@email.com",
                "wrongPassword"
        );

        // when & then
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));
        Assertions.assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).issueAccessToken(any(), any(Role.class));
        verify(jwtUtils, never()).issueRefreshToken();
    }


}