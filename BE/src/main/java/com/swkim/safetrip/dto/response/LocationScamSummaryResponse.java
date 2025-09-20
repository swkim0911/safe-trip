package com.swkim.safetrip.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record LocationScamSummaryResponse(
        LocationType locationType,// country or state or city
        List<LocationScamSummaryItem> items
) {

    public enum LocationType {
        COUNTRY,
        STATE,
        CITY
    }
}


