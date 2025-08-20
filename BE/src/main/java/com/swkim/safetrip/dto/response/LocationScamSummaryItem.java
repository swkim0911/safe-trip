package com.swkim.safetrip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationScamSummaryItem {

    private Long id;
    private String name; // country or state name
    private double lat;
    private double lng;
    private Long scamCnt;
}