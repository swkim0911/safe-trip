package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swkim.safetrip.entity.enums.Source;

import java.time.LocalDateTime;

public record ExternalReportDetailResponse (

        Source source,

        String sourceUrl,

        String scamType,

        String scamContext,

        String countryName,

        String stateName,

        String cityName,

        String title,

        String summary,

        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime postedAt,

        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime collectedAt
){}
