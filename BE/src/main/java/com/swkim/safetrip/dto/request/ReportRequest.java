package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotBlank(message = "위치를 입력해주세요.")
    private String address;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private String category;

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "설명을 해주세요.")
    private String description;

    private String advice;
}
