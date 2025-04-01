package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.SignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Long enroll(SignUpRequest signUpRequest) {

        User user = UserMapper.toUser(signUpRequest);

        return userRepository.save(user);
    }




}
