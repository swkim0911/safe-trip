package com.swkim.safetrip.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "SafeTrip API",
                version = "v1",
                description = "여행지 사기 정보 공유 플랫폼 SafeTrip의 REST API 문서입니다.",
                contact = @Contact(
                        name = "SafeTrip Team",
                        email = "kimsungwon364@gmail.com",
                        url = "https://github.com/swkim0911/safe-trip"
                )
        )
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT 토큰을 입력하세요. (Bearer 접두사 제외)"
)
@Configuration
public class OpenApiConfig {
}
