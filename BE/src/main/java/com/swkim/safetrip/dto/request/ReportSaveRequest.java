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

    @NotBlank(message = "Title must not be blank.")
    private String title;

    @NotNull(message = "Scam Category must not be blank.")
    private Long scamId;

    @NotBlank(message = "Country must not be blank.")
    private String country;

    @NotBlank(message = "City must not be blank.")
    private String city;

    @NotBlank(message = "Description must not be blank")
    private String description;
}
