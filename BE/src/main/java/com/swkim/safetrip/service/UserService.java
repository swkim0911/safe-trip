package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.exception.DuplicateUserEmailException;
import com.swkim.safetrip.exception.DuplicateUserNicknameException;
import com.swkim.safetrip.mapper.UserMapper;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Long signup(UserSignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            throw new DuplicateUserEmailException();
        }

        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            throw new DuplicateUserNicknameException();
        }

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }




}
