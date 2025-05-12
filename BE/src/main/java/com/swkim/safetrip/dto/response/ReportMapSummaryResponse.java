package com.swkim.safetrip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMapSummaryResponse{

    private Long id;
    private String name;
    private String type;
    private Long scamCnt;
    private double lat;
    private double lng;
}