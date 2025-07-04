package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.JwtDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.jwt.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Test
    void 로그인_요청_성공시_액세스_토큰과_리프레시_토큰을_발급한다() {
        String email = "test@gmail.com";
        String password = "password";

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String accessToken = "im.access.token";
        String refreshToken = "im.refresh.token";

        Mockito.when(jwtService.issueAccessToken(email)).thenReturn(accessToken);
        Mockito.when(jwtService.issueRefreshToken()).thenReturn(refreshToken);

        // when
        JwtDto jwtDto = userService.login(loginRequest);

        // then
        Assertions.assertThat(jwtDto.getAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(jwtDto.getRefreshToken()).isEqualTo(refreshToken);
    }

}