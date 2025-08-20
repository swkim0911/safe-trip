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
public class UserReportSaveRequest {

    @NotBlank(message = "Title must not be blank.")
    private String title;

    @NotNull(message = "Scam Category must not be null.")
    private Long scamId;

    @NotNull(message = "Country must not be null.")
    private Long countryId;

    @NotNull(message = "State must not be null.")
    private Long stateId;

    @NotBlank(message = "Description must not be blank")
    private String description;
}
