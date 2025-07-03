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

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    private final String secretKey = "1FD5151F374A7B3C9877AD728F769";

    @BeforeEach
    void setUp() {
        Long accessTokenExpirationPeriod = 3600000L;
        Long refreshTokenExpirationPeriod = 86400000L;

        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationPeriod", accessTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationPeriod", refreshTokenExpirationPeriod);


        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);

    }

    @Test
    void 액세스_토큰을_정상_발급한다() {
        // given
        String email = "test@gmail.com";

        // when
        String accessToken = jwtService.issueAccessToken(email);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(accessToken);

        String tokenEmail = decodedJWT.getClaim("email").asString();
        assertThat(tokenEmail).isEqualTo(email);
    }

    @Test
    void 리프레시_토큰을_정상_발급한다() {

        // given, when
        String refreshToken = jwtService.issueRefreshToken();

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(refreshToken);

        String subject = decodedJWT.getSubject();
        assertThat(subject).isEqualTo("RefreshToken");
    }

}