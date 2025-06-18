package com.swkim.safetrip.login.service;

import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("The email does not exist"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(findUser.getEmail())
                .password(findUser.getPassword())
                .roles(findUser.getRole().name())
                .build();
    }
}
