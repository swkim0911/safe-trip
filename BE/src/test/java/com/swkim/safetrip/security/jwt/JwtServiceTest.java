package com.swkim.safetrip.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.global.exception.custom.AccessTokenExpiredException;
import com.swkim.safetrip.global.exception.custom.InvalidAccessTokenException;
import com.swkim.safetrip.global.exception.custom.InvalidRefreshTokenException;
import com.swkim.safetrip.global.exception.custom.RefreshTokenExpiredException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    private final String secretKey = "a123fsdni";

    @BeforeEach
    void setUp() {
        Long accessTokenExpirationPeriod = 3600000L;
        Long refreshTokenExpirationPeriod = 86400000L;
        String accessHeader = "Authorization";
        String refreshName = "refreshToken";

        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpirationMillis", accessTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpirationMillis", refreshTokenExpirationPeriod);
        ReflectionTestUtils.setField(jwtProvider, "accessHeader", accessHeader);
        ReflectionTestUtils.setField(jwtProvider, "refreshName", refreshName);


        ReflectionTestUtils.setField(jwtProvider, "secretKey", secretKey);
    }

    @Test
    void 액세스_토큰을_정상_발급한다() {
        // given
        String email = "test@gmail.com";

        // when
        String accessToken = jwtProvider.issueAccessToken(email, Role.USER);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(accessToken);

        Optional<String> tokenEmail = jwtProvider.extractEmail(decodedJWT);
        assertThat(tokenEmail).isPresent();
        assertThat(tokenEmail.get()).isEqualTo(email);
    }

    @Test
    void 리프레시_토큰을_정상_발급한다() {
        // given
        String email = "test@gmail.com";
        // when
        String refreshToken = jwtProvider.issueRefreshToken(email);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(refreshToken);

        Optional<String> result = jwtProvider.extractEmail(decodedJWT);
        assertThat(result).isPresent();
        String extractedEmail = result.get();
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void 액세스_토큰을_요청으로부터_정상_추출한다() {
        // given
        String email = "test@gmail.com";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String accessToken = jwtProvider.issueAccessToken(email, Role.USER);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);

        // when
        Optional<String> result = jwtProvider.extractAccessToken(request);

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
        Optional<String> result = jwtProvider.extractAccessToken(request);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 리프레시_토큰을_요청으로부터_정상_추출한다() {
        // given
        String email = "test@gmail.com";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String refreshToken = jwtProvider.issueRefreshToken(email);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        Cookie[] cookies = new Cookie[]{refreshTokenCookie};
        Mockito.when(request.getCookies()).thenReturn(cookies);

        // when
        Optional<String> result = jwtProvider.extractRefreshToken(request);

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
        Optional<String> result = jwtProvider.extractRefreshToken(request);

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
        Optional<String> result = jwtProvider.extractRefreshToken(request);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void 유효한_secretKey로_서명된_리프레시_토큰은_검증에_통과한다() {
        // given
        String email = "test@gmail.com";
        String refreshToken = jwtProvider.issueRefreshToken(email);

        // when & then
        assertThatCode(() -> jwtProvider.verifyRefreshToken(refreshToken)).doesNotThrowAnyException();
    }

    @Test
    void 유효한_secretKey로_서명된_액세스_토큰은_검증에_통과한다() {
        // given
        String refreshToken = jwtProvider.issueAccessToken("test@gmail.com", Role.USER);

        // when & then
        assertThatCode(() -> jwtProvider.verifyRefreshToken(refreshToken)).doesNotThrowAnyException();
    }

    @Test
    void 유효하지_않은_secretKey로_서명된_리프레시_토큰은_검증에_실패한다() {
        // given
        String strangeRefreshToken = JWT.create()
                .withSubject("testToken")
                .sign(Algorithm.HMAC512("invalidSecretKey"));

        // when & then
        assertThatThrownBy(() -> jwtProvider.verifyRefreshToken(strangeRefreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void 유효하지_않은_secretKey로_서명된_액세스_토큰은_검증에_실패한다() {
        // given
        String strangeAccessToken = JWT.create()
                .withSubject("testToken")
                .sign(Algorithm.HMAC512("invalidSecretKey"));

        // when & then
        assertThatThrownBy(() -> jwtProvider.verifyAccessToken(strangeAccessToken))
                .isInstanceOf(InvalidAccessTokenException.class);
    }

    @Test
    void 만료된_리프레시_토큰은_검증에_실패한다() {
        // given
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - 1000);
        String expiredRefreshToken = JWT.create()
                .withSubject("testToken")
                .withExpiresAt(expiredAt)
                .sign(Algorithm.HMAC512(secretKey));

        // when & then
        assertThatThrownBy(() -> jwtProvider.verifyRefreshToken(expiredRefreshToken))
                .isInstanceOf(RefreshTokenExpiredException.class);
    }

    @Test
    void 만료된_액세스_토큰은_검증에_실패한다() {
        // given
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() - 1000);
        String expiredAccessToken = JWT.create()
                .withSubject("testToken")
                .withExpiresAt(expiredAt)
                .sign(Algorithm.HMAC512(secretKey));

        // when & then
        assertThatThrownBy(() -> jwtProvider.verifyAccessToken(expiredAccessToken))
                .isInstanceOf(AccessTokenExpiredException.class);
    }

    @Test
    void 액세스_토큰으로부터_email을_성공적으로_반환한다() {
        // given
        String email = "test@email.com";
        String accessToken = jwtProvider.issueAccessToken(email, Role.USER);
        DecodedJWT decodedJWT = jwtProvider.verifyAccessToken(accessToken);

        // when
        Optional<String> extractedEmail = jwtProvider.extractEmail(decodedJWT);

        // then
        assertThat(extractedEmail).isPresent();
        assertThat(extractedEmail.get()).isEqualTo(email);
    }

    @Test
    void email_서브젝트가_없는_액세스_토큰으로부터_email을_추출하려하면_empty를_반환한다() {
        // given
        String noEmailAccessToken = JWT.create()
                .sign(Algorithm.HMAC512(secretKey));

        DecodedJWT decodedJWT = jwtProvider.verifyAccessToken(noEmailAccessToken);

        // when
        Optional<String> extractedEmail = jwtProvider.extractEmail(decodedJWT);

        // then
        assertThat(extractedEmail).isEmpty();
    }

}