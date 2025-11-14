package com.swkim.safetrip.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RegionScamStatisticsResponse(
        RegionType regionType,// country or state or city
        List<RegionScamStatisticsItem> items
) {

    public enum RegionType {
        COUNTRY,
        STATE,
        CITY
    }
}

