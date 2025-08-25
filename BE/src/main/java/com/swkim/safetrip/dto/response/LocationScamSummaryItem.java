package com.swkim.safetrip.dto.response;

import lombok.Builder;

@Builder
public record LocationScamSummaryItem (

    Long id,
    String name, // country or state name
    Double lat,
    Double lng,
    Long scamCnt
){}