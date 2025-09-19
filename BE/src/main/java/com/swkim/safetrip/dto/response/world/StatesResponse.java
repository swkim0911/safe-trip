package com.swkim.safetrip.dto.response.world;

import java.util.List;

public record StatesResponse(List<StateDto> states) {
    public record StateDto(Long id, String name) {}
}