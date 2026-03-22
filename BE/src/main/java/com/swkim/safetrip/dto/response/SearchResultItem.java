package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record SearchResultItem(
        Long id,
        String name,
        String type,
        Double lat,
        Double lng,
        Long countryId
) {}
