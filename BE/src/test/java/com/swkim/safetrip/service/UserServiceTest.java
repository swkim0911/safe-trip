package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.EmailValidationResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.validation.EmailValidator;
import com.swkim.safetrip.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailValidator emailValidator;

    @Test
    void 회원가입_요청_성공시_User_Id를_반환한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "test@gmail.com",
                "password",
                "nickname"
        );

        given(userRepository.existsByEmail(signUpRequest.getEmail())).willReturn(false);
        given(userRepository.existsByNickname(signUpRequest.getNickname())).willReturn(false);

        String encodedPassword = "encodedPassword";
        given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

        User savedUser = User.builder()
                .email(signUpRequest.getEmail())
                .nickname(signUpRequest.getNickname())
                .password(encodedPassword)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        Long savedId = userService.signup(signUpRequest);

        // then
        assertThat(savedId).isEqualTo(1L);
    }

    @Test
    void 회원가입_요청에_이메일이_중복된_경우_예외가_발생한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "duplicatedEmail@gmail.com",
                "password",
                "nickname"
        );

        given(userRepository.existsByEmail(signUpRequest.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpRequest)).isInstanceOf(DuplicateUserEmailException.class);

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

        given(userRepository.existsByNickname(signUpRequest.getNickname())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpRequest)).isInstanceOf(DuplicateUserNicknameException.class);

        // verify
        verify(userRepository, never()).save(any());
    }

    @Test
    void 이메일_검증_요청에_형식이_잘못되었으면_isValidFormat_false을_반환한다() {
        // given
        String invalidEmail = "not-an-email";
        given(emailValidator.isValid(invalidEmail)).willReturn(false);

        // when
        EmailValidationResponse response = userService.validateEmail(invalidEmail);

        // then
        assertThat(response.isValidFormat()).isFalse();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getReason()).isEqualTo("Invalid email format");
    }
}