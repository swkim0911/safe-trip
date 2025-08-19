package com.swkim.safetrip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationScamSummaryResponse {

    private String type; // country or state
    private List<LocationScamSummaryItem> locationScamSummaryItems;

}
