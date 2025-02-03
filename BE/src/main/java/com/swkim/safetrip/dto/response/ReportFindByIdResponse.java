package com.swkim.safetrip.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFindByIdResponse {

    private String title;

    private String category;

    private List<String> URLs;

    private String latitude;

    private String longitude;

    private String description;

    private String advice;

    private Integer likes;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;
}
