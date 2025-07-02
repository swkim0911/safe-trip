package com.swkim.safetrip.login.handler;

import com.swkim.safetrip.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String email = extractUsername(authentication);

        String accessToken = jwtService.issueAccessToken(email);
        String refreshToken = jwtService.issueRefreshToken();

        jwtService.addTokensToResponseHeader(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(email, refreshToken);

        log.info("success to sign in. email: {}", email);
        log.info("success to sign in. accessToken: {}", accessToken);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
