package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserReportDetailResponse {

    private String nickname;

    private String title;

    private String scamName;

    private String countryName;

    private String stateName;

    private String description;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;

    @Setter
    private List<String> URLs;

    public UserReportDetailResponse(
            String nickname,
            String title,
            String scamName,
            String countryName,
            String stateName,
            String description,
            LocalDateTime createdAt
    ) {
        this.nickname = nickname;
        this.title = title;
        this.scamName = scamName;
        this.countryName = countryName;
        this.stateName = stateName;
        this.description = description;
        this.createdAt = createdAt;
    }
}
