package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record LocationScamStatisticsItem (

    Long id,
    String name, // country or state or city name
    Double lat,
    Double lng,
    Long scamCnt
){}

