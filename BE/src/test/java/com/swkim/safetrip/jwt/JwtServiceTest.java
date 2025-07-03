package com.swkim.safetrip.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Long accessTokenExpirationPeriod = 3600000L;
        String secretKey = "1FD5151F374A7B3C9877AD728F769";

        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationPeriod", accessTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);

    }

    @Test
    void 액세스_토큰을_정상_발급한다() {
        // given
        String email = "test@gmail.com";

        // when
        String accessToken = jwtService.issueAccessToken(email);

        // then
        Assertions.assertThat(accessToken).isNotNull();
        Assertions.assertThat(accessToken).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("1FD5151F374A7B3C9877AD728F769"))
                .build()
                .verify(accessToken);

        String tokenEmail = decodedJWT.getClaim("email").asString();
        Assertions.assertThat(tokenEmail).isEqualTo(email);
    }

}