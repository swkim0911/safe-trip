package com.swkim.safetrip.dto.response.world;

import java.util.List;

public record CountriesResponse(List<CountryDto> countries) {
    public record CountryDto(Long id, String name) {}
}