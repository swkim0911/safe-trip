package com.swkim.safetrip.global.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SignUpValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final Pattern NICKNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9가-힣_-]{2,15}$");


    public boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidNickname(String nickname) {
        return NICKNAME_PATTERN.matcher(nickname).matches();
    }
}