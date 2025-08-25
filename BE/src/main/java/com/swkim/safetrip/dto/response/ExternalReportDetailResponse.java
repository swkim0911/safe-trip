package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swkim.safetrip.entity.enums.Source;

import java.time.LocalDateTime;

public record ExternalReportDetailResponse (

        Source source,

        String sourceUrl,

        String title,

        String scamName,

        String countryName,

        String stateName,

        String description,

        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime originalCreatedAt,

        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime collectedAt
){}
