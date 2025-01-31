package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSaveRequest {

    @NotBlank(message = "위도를 입력해주세요.")
    private String latitude;

    @NotBlank(message = "경도를 입력해주세요.")
    private String longitude;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private String category;

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "설명을 해주세요.")
    private String description;

    @NotNull
    private String advice;
}
