package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.LoginResultDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입_요청_성공시_User_Id를_반환한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "test@gmail.com",
                "password",
                "nickname"
        );

        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(false);

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .email(signUpRequest.getEmail())
                .nickname(signUpRequest.getNickname())
                .password(encodedPassword)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        Long savedId = userService.signup(signUpRequest);

        // then
        Assertions.assertThat(savedId).isEqualTo(1L);
    }

    @Test
    void 회원가입_요청에_이메일이_중복된_경우_예외가_발생한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "duplicatedEmail@gmail.com",
                "password",
                "nickname"
        );

        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

        // when & then
        Assertions.assertThatThrownBy(() -> userService.signup(signUpRequest)).isInstanceOf(DuplicateUserEmailException.class);

        // verify
        verify(userRepository, never()).save(any());
    }

    @Test
    void 회원가입_요청에_닉네임이_중복된_경우_예외가_발생한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "test@gmail.com",
                "password",
                "duplicatedNickname"
        );

        when(userRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(true);

        // when & then
        Assertions.assertThatThrownBy(() -> userService.signup(signUpRequest)).isInstanceOf(DuplicateUserNicknameException.class);

        // verify
        verify(userRepository, never()).save(any());
    }

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

        when(jwtUtils.issueAccessToken(email)).thenReturn(accessToken);
        when(jwtUtils.issueRefreshToken()).thenReturn(refreshToken);

        // when
        LoginResultDto loginResultDto = userService.login(loginRequest);

        // then
        Assertions.assertThat(loginResultDto.getAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(loginResultDto.getRefreshToken()).isEqualTo(refreshToken);
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
        Assertions.assertThatThrownBy(() -> userService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).issueAccessToken(any());
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
        Assertions.assertThatThrownBy(() -> userService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);

        // verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).issueAccessToken(any());
        verify(jwtUtils, never()).issueRefreshToken();
    }

}