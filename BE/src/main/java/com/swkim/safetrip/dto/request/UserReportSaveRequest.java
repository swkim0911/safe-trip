package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserReportSaveRequest(
        @NotBlank(message = "Title must not be blank.")
        String title,

        @NotNull(message = "Scam Category must not be null.")
        Long scamId,

        @NotNull(message = "Country must not be null.")
        Long countryId,

        @NotNull(message = "State must not be null.")
        Long stateId,

        @NotBlank(message = "Description must not be blank")
        String description
){}
