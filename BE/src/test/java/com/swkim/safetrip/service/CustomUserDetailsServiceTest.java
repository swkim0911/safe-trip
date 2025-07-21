package com.swkim.safetrip.service;

import com.swkim.safetrip.repository.UserRepository;
import com.swkim.safetrip.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("role 이름이 \"ROLE_\"으로 시작하면 예외가 발생한다")
    void roles_with_ROLE_prefix_should_throw_exception() {
        // given
        String roleWithPrefix = "ROLE_USER";

        // when & then
        assertThatThrownBy(() ->
                org.springframework.security.core.userdetails.User.builder()
                        .username("test@gmail.com")
                        .password("password")
                        .roles(roleWithPrefix)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot start with ROLE_");
    }


}