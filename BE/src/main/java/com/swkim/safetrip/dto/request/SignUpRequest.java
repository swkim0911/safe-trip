package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
    @NotBlank(message = "사용자 이름을 입력해주세요.")
    private String name;
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
}
