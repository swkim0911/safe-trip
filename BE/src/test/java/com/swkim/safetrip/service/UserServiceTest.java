package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.validation.SignUpValidator;
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
    private SignUpValidator signUpValidator;

    @Test
    void 회원가입_요청_성공시_User_Id를_반환한다() {
        // given
        UserSignUpRequest signUpRequest = new UserSignUpRequest(
                "test@gmail.com",
                "password",
                "nickname"
        );

        given(userRepository.existsByEmail(signUpRequest.email())).willReturn(false);
        given(userRepository.existsByNickname(signUpRequest.nickname())).willReturn(false);

        String encodedPassword = "encodedPassword";
        given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);

        User savedUser = User.builder()
                .email(signUpRequest.email())
                .nickname(signUpRequest.nickname())
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

        given(userRepository.existsByEmail(signUpRequest.email())).willReturn(true);

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

        given(userRepository.existsByNickname(signUpRequest.nickname())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signUpRequest)).isInstanceOf(DuplicateUserNicknameException.class);

        // verify
        verify(userRepository, never()).save(any());
    }

    @Test
    void 이메일_검증_요청에_형식이_잘못되었으면_isValidFormat_false을_반환한다() {
        // given
        String invalidEmail = "not-an-email";
        given(signUpValidator.isValidEmail(invalidEmail)).willReturn(false);

        // when
        ValidationResponse response = userService.validateEmail(invalidEmail);

        // then
        assertThat(response.isValidFormat()).isFalse();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.reason()).isEqualTo("Invalid email format");
    }

    @Test
    void 이메일_검증_요청에_이메일이_중복되면_isAvailable_false을_반환한다() {
        // given
        String duplicatedEmail = "duplicated@gmail.com";
        given(signUpValidator.isValidEmail(duplicatedEmail)).willReturn(true);
        given(userRepository.existsByEmail(duplicatedEmail)).willReturn(true);

        // when
        ValidationResponse response = userService.validateEmail(duplicatedEmail);

        // then
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.reason()).isEqualTo("Email already in use");
    }

    @Test
    void 이메일_검증_요청에_이메일이_올바르다면_true를_반환한다() {
        // given
        String rightEmail = "right@gmail.com";
        given(signUpValidator.isValidEmail(rightEmail)).willReturn(true);
        given(userRepository.existsByEmail(rightEmail)).willReturn(false);

        // when
        ValidationResponse response = userService.validateEmail(rightEmail);

        // then
        assertThat(response.isValidFormat()).isTrue();
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.reason()).isNull();
    }

    @Test
    void 닉네임_검증_요청에_닉네임이_올바르다면_true를_반환한다() {
        // given
        String rightNickname = "rightName";
        given(signUpValidator.isValidNickname(rightNickname)).willReturn(true);
        given(userRepository.existsByNickname(rightNickname)).willReturn(false);

        // when
        ValidationResponse response = userService.validateNickname(rightNickname);

        // then
        assertThat(response.isValidFormat()).isTrue();
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.reason()).isNull();
    }

    @Test
    void 닉네임_검증_요청에_닉네임이_중복되면_isAvailable_false을_반환한다() {
        // given
        String duplicatedNickname = "duplicated@gmail.com";
        given(signUpValidator.isValidNickname(duplicatedNickname)).willReturn(true);
        given(userRepository.existsByNickname(duplicatedNickname)).willReturn(true);

        // when
        ValidationResponse response = userService.validateNickname(duplicatedNickname);

        // then
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.reason()).isEqualTo("Nickname already in use");
    }
}