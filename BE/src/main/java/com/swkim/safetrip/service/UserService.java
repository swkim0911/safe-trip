package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.JwtDto;
import com.swkim.safetrip.dto.request.UserLoginRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.jwt.JwtService;
import com.swkim.safetrip.mapper.UserMapper;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
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

    public JwtDto login(UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtService.issueAccessToken(email);
        String refreshToken = jwtService.issueRefreshToken();

        return JwtDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken).build();

    }



    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
