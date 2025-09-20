package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record AccessTokenResponse(
        String accessToken
){

}
