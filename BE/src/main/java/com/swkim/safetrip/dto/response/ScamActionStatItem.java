package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record ScamActionStatItem(String name, Long count) {}
