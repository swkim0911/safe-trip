package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swkim.safetrip.entity.enums.Source;

import java.time.LocalDateTime;

public record ExternalReportDetailResponse (

        Source source,

        String sourceUrl,

        String author,

        String scamAction,

        String scamContext,

        String countryName,

        String stateName,

        String cityName,

        String title,

        String summary,

        LocalDateTime postedAt,

        LocalDateTime collectedAt
){}
