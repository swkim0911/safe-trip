package com.swkim.safetrip.dto.response.world;

import java.util.List;

public record CitiesResponse(List<CityDto> cities) {
    public record CityDto(Long id, String name){}
}
