package com.swkim.safetrip.dto.response;

import java.time.LocalDateTime;

public record ReportInaccuracyItem(
        Long id,
        Long externalReportId,
        String externalReportTitle,
        String reporterNickname,
        String reason,
        String description,
        String status,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {}
