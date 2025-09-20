package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record UserInfoResponse(
    String nickname
){}
