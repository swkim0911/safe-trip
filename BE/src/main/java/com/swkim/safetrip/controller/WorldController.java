package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.world.CitiesResponse;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.CityService;
import com.swkim.safetrip.service.CountryService;
import com.swkim.safetrip.service.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorldController {

    private final CountryService countryService;
    private final StateService stateService;
    private final CityService cityService;

    @GetMapping(value = "/countries")
    public ApiResult<CountriesResponse> getAllCountries() {
        CountriesResponse allCountries = countryService.getAllCountries();
        return ApiResult.of(HttpStatus.OK.value(), "All countries retrieved successfully", allCountries);
    }

    @GetMapping("/countries/{countryId}/states")
    public ApiResult<StatesResponse> getStates(@PathVariable Long countryId) {
        StatesResponse allStates = stateService.getStates(countryId);
        return ApiResult.of(HttpStatus.OK.value(), "All states retrieved successfully", allStates);
    }

    @GetMapping("/states/{stateId}/cities")
    public ApiResult<CitiesResponse> getCities(@PathVariable Long stateId) {
        CitiesResponse allCities = cityService.getCities(stateId);
        return ApiResult.of(HttpStatus.OK.value(), "All cities retrieved successfully", allCities);
    }


}
