package com.swkim.safetrip.dto.response;

import java.time.LocalDateTime;

public record MyReportItem(
        Long id,
        String title,
        Long scamActionId,
        String scamActionName,
        Long scamContextId,
        String scamContextName,
        Long countryId,
        String countryName,
        Long stateId,
        String stateName,
        Long cityId,
        String cityName,
        String description,
        LocalDateTime createdAt
) {}
