package com.swkim.safetrip.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    private String username;

    private String password;

    private String name;

    private String phoneNumber;

    private String email;
}
