package com.swkim.safetrip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserReportUpdateRequest(

        @NotBlank(message = "Title must not be blank.")
        String title,

        @NotNull(message = "Scam action must not be null.")
        Long scamActionId,

        @NotNull(message = "Scam context must not be null.")
        Long scamContextId,

        @NotNull(message = "Country must not be null.")
        Long countryId,

        Long stateId,

        Long cityId,

        @NotBlank(message = "Description must not be blank")
        String description
) {}
