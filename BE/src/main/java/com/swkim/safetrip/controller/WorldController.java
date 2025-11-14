package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.world.CitiesResponse;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.CityService;
import com.swkim.safetrip.service.CountryService;
import com.swkim.safetrip.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class WorldController {

    private final CountryService countryService;
    private final StateService stateService;
    private final CityService cityService;

    @Operation(
            summary = "전체 국가 목록 조회",
            description = "등록된 모든 국가 목록을 조회합니다."
    )
    @GetMapping(value = "/countries")
    public ApiResult<CountriesResponse> getAllCountries() {
        CountriesResponse allCountries = countryService.getAllCountries();
        return ApiResult.of(HttpStatus.OK.value(), "All countries retrieved successfully", allCountries);
    }

    @Operation(
            summary = "특정 국가의 주(State) 목록 조회",
            description = "선택한 국가에 속한 모든 주(State) 목록을 조회합니다."
    )
    @GetMapping("/countries/{countryId}/states")
    public ApiResult<StatesResponse> getStates(@PathVariable Long countryId) {
        StatesResponse allStates = stateService.getStates(countryId);
        return ApiResult.of(HttpStatus.OK.value(), "All states retrieved successfully", allStates);
    }

    @Operation(
            summary = "특정 주(State)의 도시(City) 목록 조회",
            description = "선택한 주(State)에 속한 모든 도시(City) 목록을 조회합니다."
    )
    @GetMapping("/states/{stateId}/cities")
    public ApiResult<CitiesResponse> getCities(@PathVariable Long stateId) {
        CitiesResponse allCities = cityService.getCities(stateId);
        return ApiResult.of(HttpStatus.OK.value(), "All cities retrieved successfully", allCities);
    }


}
