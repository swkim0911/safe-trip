package com.swkim.safetrip.dto.request;

import com.swkim.safetrip.entity.enums.ReportInaccuracyReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportInaccuracyRequest(
        @NotNull ReportInaccuracyReason reason,
        @Size(max = 500) String description
) {}
