package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

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
}