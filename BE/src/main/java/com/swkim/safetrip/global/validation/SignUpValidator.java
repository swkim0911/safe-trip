package com.swkim.safetrip.global.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SignUpValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) return false;
        int len = nickname.length();
        return len >= 2 && len <= 15;
    }
}