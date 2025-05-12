package com.swkim.safetrip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMapSummaryItem {

    private Long id;
    private String name;
    private Long scamCnt;
    private double lat;
    private double lng;
}