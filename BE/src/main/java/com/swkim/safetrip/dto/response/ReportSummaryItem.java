package com.swkim.safetrip.dto.response;

import com.swkim.safetrip.entity.enums.Source;
import lombok.Builder;

@Builder
public record ReportSummaryItem(
        Long reportId,

        Source source,

        String title,

        String scamName

){}
