package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swkim.safetrip.entity.enums.Source;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserReportDetailResponse {

    private Source source;

    private String nickname;

    private String scamAction;

    private String scamContext;

    private String countryName;

    private String stateName;

    private String cityName;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;

    @Setter
    private List<String> urls;

    @Builder
    public UserReportDetailResponse(
            Source source,
            String nickname,
            String scamAction,
            String scamContext,
            String countryName,
            String stateName,
            String cityName,
            String title,
            String description,
            LocalDateTime createdAt
    ) {
        this.source = source;
        this.nickname = nickname;
        this.scamAction = scamAction;
        this.scamContext = scamContext;
        this.countryName = countryName;
        this.stateName = stateName;
        this.cityName =  cityName;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }
}
