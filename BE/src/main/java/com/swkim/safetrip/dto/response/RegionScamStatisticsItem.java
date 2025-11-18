package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegionScamStatisticsItem (

    Long id,
    String name, // country or state or city name
    Double lat,
    Double lng,
    Long scamCnt,
    String iso2 // ISO2 country code (country가 아니면 null)
){}

