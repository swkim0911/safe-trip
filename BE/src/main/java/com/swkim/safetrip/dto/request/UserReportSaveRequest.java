package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportSaveRequest {

    @NotBlank(message = "Title must not be blank.")
    private String title;

    @NotBlank(message = "Scam Category must not be blank.")
    private Long scamId;

    @NotBlank(message = "Country must not be blank.")
    private Long countryId;

    @NotBlank(message = "State must not be blank.")
    private Long stateId;

    @NotBlank(message = "Description must not be blank")
    private String description;
}
