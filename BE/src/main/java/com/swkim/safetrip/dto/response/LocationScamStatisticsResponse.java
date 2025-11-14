package com.swkim.safetrip.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LocationScamStatisticsResponse(
        LocationType locationType,// country or state or city
        List<LocationScamStatisticsItem> items
) {

    public enum LocationType {
        COUNTRY,
        STATE,
        CITY
    }
}

