package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.LoginResultDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.AccessTokenResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.mapper.UserMapper;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(UserSignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            throw new DuplicateUserEmailException();
        }

        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            throw new DuplicateUserNicknameException();
        }

        User user = UserMapper.toUser(signUpRequest);
        // 비밀번호 암호화
        user.passwordEncode(passwordEncoder);

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Transactional
    public LoginResultDto login(UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtUtils.issueAccessToken(email);
        String refreshToken = jwtUtils.issueRefreshToken();

        User findUser = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        findUser.updateRefreshToken(refreshToken);

        ResponseCookie refreshTokenCookie = jwtUtils.createRefreshTokenCookie(refreshToken);
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();

        return LoginResultDto.builder()
                .accessTokenResponse(accessTokenResponse)
                .refreshTokenCookie(refreshTokenCookie)
                .build();

    }



    @Transactional(readOnly = true)
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }
}
