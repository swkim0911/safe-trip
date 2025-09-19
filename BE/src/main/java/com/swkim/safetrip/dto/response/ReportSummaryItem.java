package com.swkim.safetrip.dto.response;

import com.swkim.safetrip.entity.enums.Source;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReportSummaryItem(
        Long reportId,

        Source source,

        String title,

        String scamAction,

        String scamContext,

        LocalDateTime posted_at

){}
