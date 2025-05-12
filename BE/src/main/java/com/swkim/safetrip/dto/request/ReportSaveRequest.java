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

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotNull(message = "카테고리를 입력해주세요.")
    private Long scamId;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotBlank(message = "위도를 입력해주세요.")
    private String lat;

    @NotBlank(message = "경도를 입력해주세요.")
    private String lng;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank(message = "설명을 해주세요.")
    private String description;

    @NotBlank(message = "조언을 해주세요.")
    private String advice;
}
