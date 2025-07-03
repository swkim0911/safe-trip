package com.swkim.safetrip.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;


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
        String accessHeader = "Authorization";
        String refreshName = "refreshToken";

        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationPeriod", accessTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationPeriod", refreshTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtService, "accessHeader", accessHeader);
        ReflectionTestUtils.setField(jwtService, "refreshName", refreshName);


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

    @Test
    void 액세스_토큰을_요청으로부터_정상_추출한다() {
        // given
        String email = "test@gmail.com";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String accessToken = jwtService.issueAccessToken(email);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);

        // when
        Optional<String> result =jwtService.extractAccessToken(request);

        // then
        assertThat(result).isPresent();
        String extractedAccessToken = result.get();
        assertThat(extractedAccessToken).isEqualTo(accessToken);
    }

    @Test
    void 요청_헤더에_액세스_토큰이_없다면_empty를_반환한다() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        // when
        Optional<String> result =jwtService.extractAccessToken(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 리프레시_토큰을_요청으로부터_정상_추출한다() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String refreshToken = jwtService.issueRefreshToken();
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        Cookie[] cookies = new Cookie[]{refreshTokenCookie};
        Mockito.when(request.getCookies()).thenReturn(cookies);

        // when
        Optional<String> result = jwtService.extractRefreshToken(request);

        // then
        assertThat(result).isPresent();
        String extractedRefreshToken = result.get();
        assertThat(extractedRefreshToken).isEqualTo(refreshToken);
    }

    @Test
    void 요청에_쿠키가_없다면_empty를_반환한다() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getCookies()).thenReturn(null);

        //when
        Optional<String> result = jwtService.extractRefreshToken(request);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void 요청_쿠키에_리프레쉬_토큰이_없으면_empty를_반환한다() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie otherCookie = new Cookie("otherCookie", "i-am-not-token");
        Cookie[] cookies = new Cookie[]{otherCookie};


        Mockito.when(request.getCookies()).thenReturn(cookies);

        //when
        Optional<String> result = jwtService.extractRefreshToken(request);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void 유효한_secretKey로_서명된_토큰은_검증에_통과한다() {
        // given
        String token = jwtService.issueRefreshToken();

        // when
        boolean tokenValid = jwtService.isTokenValid(token);

        // then
        assertThat(tokenValid).isTrue();
    }

    @Test
    void 유효하지_않은_secretKey로_서명된_토큰은_검증에_실패한다() {
        // given
        String strangeToken = JWT.create()
                .withSubject("testToken")
                .sign(Algorithm.HMAC512("invalidSecretKey"));

        // when
        boolean tokenValid = jwtService.isTokenValid(strangeToken);

        // then
        assertThat(tokenValid).isFalse();
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() {
        // given
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - 1000);
        String expiredToken = JWT.create()
                .withSubject("testToken")
                .withExpiresAt(expiredAt)
                .sign(Algorithm.HMAC512(secretKey));

        // when
        boolean tokenValid = jwtService.isTokenValid(expiredToken);

        // then
        assertThat(tokenValid).isFalse();
    }

    @Test
    void 액세스_토큰으로부터_email을_성공적으로_반환한다() {
        // given
        String email = "test@email.com";
        String accessToken = jwtService.issueAccessToken(email);

        // when
        Optional<String> extractedEmail = jwtService.extractEmail(accessToken);

        // then
        assertThat(extractedEmail).isPresent();
        assertThat(extractedEmail.get()).isEqualTo(email);

    }

}