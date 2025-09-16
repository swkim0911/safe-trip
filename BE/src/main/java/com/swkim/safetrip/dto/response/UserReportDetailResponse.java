package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserReportDetailResponse {

    private String nickname;

    private String scamType;

    private String scamContext;

    private String countryName;

    private String stateName;

    private String cityName;

    private String title;

    private String description;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;

    @Setter
    private List<String> URLs;

    @Builder
    public UserReportDetailResponse(
            String nickname,
            String scamType,
            String scamContext,
            String countryName,
            String stateName,
            String cityName,
            String title,
            String description,
            LocalDateTime createdAt
    ) {
        this.nickname = nickname;
        this.scamType = scamType;
        this.scamContext = scamContext;
        this.countryName = countryName;
        this.stateName = stateName;
        this.cityName =  cityName;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }
}
