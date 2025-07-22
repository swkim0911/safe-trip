package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.EmailValidationResponse;
import com.swkim.safetrip.dto.response.NicknameDuplicateResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.validation.EmailValidator;
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
    private final EmailValidator emailValidator;

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

    @Transactional(readOnly = true)
    public EmailValidationResponse validateEmail(String email) {

        if(!emailValidator.isValid(email)) {
            return EmailValidationResponse.builder()
                    .isValidFormat(false)
                    .isAvailable(false)
                    .reason("Invalid email format")
                    .build();
        }

        boolean isDuplicated = userRepository.existsByEmail(email);

        return EmailValidationResponse.builder()
                .isValidFormat(true)
                .isAvailable(!isDuplicated)
                .reason(isDuplicated ? "Email already in use" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public NicknameDuplicateResponse checkNicknameDuplicate(String nickname) {
        boolean isDuplicated = userRepository.existsByNickname(nickname);

        return NicknameDuplicateResponse.builder()
                .isDuplicated(isDuplicated).
                build();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }



}
