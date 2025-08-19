package com.swkim.safetrip.dto.response;

import com.swkim.safetrip.entity.enums.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryItem {

    private Long reportId;

    private Source source;

    private String title;

    private String scamName;
}
