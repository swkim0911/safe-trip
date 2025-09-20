package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserReportSaveRequest(
        @NotBlank(message = "Title must not be blank.")
        String title,

        @NotNull(message = "Scam action must not be null.")
        Long scamActionId,

        @NotNull(message = "Scam context must not be null.")
        Long scamContextId,

        @NotNull(message = "Country must not be null.")
        Long countryId,

        @NotNull(message = "State must not be null.")
        Long stateId,

        @NotNull(message = "City must not be null")
        Long cityId,

        @NotBlank(message = "Description must not be blank")
        String description
){}
