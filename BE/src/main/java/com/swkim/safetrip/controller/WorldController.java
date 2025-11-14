package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.world.CitiesResponse;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.CityService;
import com.swkim.safetrip.service.CountryService;
import com.swkim.safetrip.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
        summary = "국가별 리포트 통계 조회", 
        description = "각 국가별 리포트 개수 및 통계 정보를 조회합니다. " +
                     "향후 스캠 유형별 분포, 위험도 점수, 트렌드 등의 통계 정보가 추가될 수 있습니다."
    )
    @GetMapping(value = "/countries/statistics")
    public ApiResult<Slice<LocationScamSummaryItem>> getCountryStatistics(Pageable pageable){
        Slice<LocationScamSummaryItem> countryStatistics = countryService.getCountryStatistics(pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by country", countryStatistics);
    }

    @Operation(
            summary = "주별 리포트 통계 조회",
            description = "특정 국가 내 제1 행정구역(주별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/states/statistics")
    public ApiResult<Slice<LocationScamSummaryItem>> getStateStatistics(@RequestParam Long countryId, Pageable pageable){
        Slice<LocationScamSummaryItem> stateStatistics = stateService.getStateStatistics(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by state", stateStatistics);
    }

    @Operation(
            summary = "도시별 리포트 통계 조회",
            description = "특정 주/도 내 도시별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/cities/statistics")
    public ApiResult<Slice<LocationScamSummaryItem>> getCityStatistics(@RequestParam Long stateId, Pageable pageable){
        Slice<LocationScamSummaryItem> cityStatistics = cityService.getCityStatistics(stateId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by city", cityStatistics);
    }


}
