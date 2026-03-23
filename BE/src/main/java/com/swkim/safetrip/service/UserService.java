package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UpdateNicknameRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.validation.SignUpValidator;
import com.swkim.safetrip.mapper.UserMapper;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignUpValidator signUpValidator;

    @Transactional
    public Long signup(UserSignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.email())){
            throw new DuplicateUserEmailException();
        }

        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new DuplicateUserNicknameException();
        }

        User user = UserMapper.toUser(signUpRequest);
        // 비밀번호 암호화
        user.passwordEncode(passwordEncoder);

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateEmail(String email) {

        if(!signUpValidator.isValidEmail(email)) {
            return ValidationResponse.builder()
                    .isValidFormat(false)
                    .isAvailable(false)
                    .reason("Invalid email format")
                    .build();
        }

        boolean isDuplicated = userRepository.existsByEmail(email);

        return ValidationResponse.builder()
                .isValidFormat(true)
                .isAvailable(!isDuplicated)
                .reason(isDuplicated ? "Email already in use" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateNickname(String nickname) {

        if(!signUpValidator.isValidNickname(nickname)) {
            return ValidationResponse.builder()
                    .isValidFormat(false)
                    .isAvailable(false)
                    .reason("Invalid nickname format")
                    .build();
        }

        boolean isDuplicated = userRepository.existsByNickname(nickname);

        return ValidationResponse.builder()
                .isValidFormat(true)
                .isAvailable(!isDuplicated)
                .reason(isDuplicated ? "Nickname already in use" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateNickname(String email, UpdateNicknameRequest request) {
        if (userRepository.existsByNicknameAndEmailNot(request.nickname(), email)) {
            throw new DuplicateUserNicknameException();
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        user.updateNickname(request.nickname());
    }

}
