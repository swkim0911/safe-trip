package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.RegionScamStatisticsResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.ScamActionStatItem;
import com.swkim.safetrip.dto.response.SearchResultItem;
import com.swkim.safetrip.dto.response.world.CitiesResponse;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.CityService;
import com.swkim.safetrip.service.CountryService;
import com.swkim.safetrip.service.ReportService;
import com.swkim.safetrip.service.SearchService;
import com.swkim.safetrip.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
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
    private final SearchService searchService;
    private final ReportService reportService;

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

    @Operation(summary = "국가 기본 정보 조회", description = "국가 ID로 이름과 scam 건수를 조회합니다.")
    @GetMapping("/countries/{countryId}/info")
    public ApiResult<RegionScamStatisticsItem> getCountryInfo(@PathVariable Long countryId) {
        return ApiResult.of(HttpStatus.OK.value(), "Country info", countryService.getCountryInfo(countryId));
    }

    @Operation(summary = "주 기본 정보 조회", description = "주 ID로 이름과 scam 건수를 조회합니다.")
    @GetMapping("/states/{stateId}/info")
    public ApiResult<RegionScamStatisticsItem> getStateInfo(@PathVariable Long stateId) {
        return ApiResult.of(HttpStatus.OK.value(), "State info", stateService.getStateInfo(stateId));
    }

    @Operation(
        summary = "국가별 리포트 통계 조회",
        description = "각 국가별 리포트 개수 및 통계 정보를 조회합니다. " +
                     "향후 스캠 유형별 분포, 위험도 점수, 트렌드 등의 통계 정보가 추가될 수 있습니다."
    )
    @GetMapping(value = "/countries/statistics")
    public ApiResult<Slice<RegionScamStatisticsItem>> getCountryStatistics(Pageable pageable){
        Slice<RegionScamStatisticsItem> countryStatistics = countryService.getCountryStatistics(pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by country", countryStatistics);
    }

    @Operation(
            summary = "주별 리포트 통계 조회",
            description = "특정 국가 내 제1 행정구역(주별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/states/statistics")
    public ApiResult<Slice<RegionScamStatisticsItem>> getStateStatistics(@RequestParam Long countryId, Pageable pageable){
        Slice<RegionScamStatisticsItem> stateStatistics = stateService.getStateStatistics(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by state", stateStatistics);
    }

    @Operation(
            summary = "도시별 리포트 통계 조회",
            description = "특정 주/도 내 도시별 리포트 개수 및 통계 정보를 조회합니다."
    )
    @GetMapping(value = "/cities/statistics")
    public ApiResult<Slice<RegionScamStatisticsItem>> getCityStatistics(@RequestParam Long stateId, Pageable pageable){
        Slice<RegionScamStatisticsItem> cityStatistics = cityService.getCityStatistics(stateId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "Report statistics by city", cityStatistics);
    }

    @Operation(
            summary = "특정 국가의 모든 스캠 리포트 조회",
            description = "선택한 국가에서 발생한 모든 스캠 리포트 요약 정보를 조회합니다."
    )
    @GetMapping("/countries/{countryId}/reports")
    public ApiResult<Slice<ReportSummaryItem>> getReportsByCountry(@PathVariable Long countryId, Pageable pageable) {
        Slice<ReportSummaryItem> reports = countryService.getReportsByCountry(countryId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "All report summaries in country", reports);
    }

    @Operation(
            summary = "특정 주(State)의 모든 스캠 리포트 조회",
            description = "선택한 주(State)에서 발생한 모든 스캠 리포트 요약 정보를 조회합니다."
    )
    @GetMapping("/states/{stateId}/reports")
    public ApiResult<Slice<ReportSummaryItem>> getReportsByState(@PathVariable Long stateId, Pageable pageable) {
        Slice<ReportSummaryItem> reports = stateService.getReportsByState(stateId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "All report summaries in state", reports);
    }

    @Operation(
            summary = "특정 도시(City)의 모든 스캠 리포트 조회",
            description = "선택한 도시(City)에서 발생한 모든 스캠 리포트 요약 정보를 조회합니다."
    )
    @GetMapping("/cities/{cityId}/reports")
    public ApiResult<Slice<ReportSummaryItem>> getReportsByCity(@PathVariable Long cityId, Pageable pageable) {
        Slice<ReportSummaryItem> reports = cityService.getReportsByCity(cityId, pageable);
        return ApiResult.of(HttpStatus.OK.value(), "All report summaries in city", reports);
    }

    @Operation(
            summary = "사기 유형별 건수 통계 조회",
            description = "전체 리포트를 사기 유형(scam_action)별로 집계하여 건수 내림차순으로 반환합니다."
    )
    @GetMapping("/scam-actions/statistics")
    public ApiResult<List<ScamActionStatItem>> getScamActionStatistics() {
        List<ScamActionStatItem> stats = reportService.getScamActionStats();
        return ApiResult.of(HttpStatus.OK.value(), "Scam action statistics", stats);
    }

    @Operation(
            summary = "국가/주/도시 통합 검색",
            description = "검색어로 국가, 주, 도시를 통합 검색합니다. 최대 10건 반환."
    )
    @GetMapping("/search")
    public ApiResult<List<SearchResultItem>> search(@RequestParam String q) {
        List<SearchResultItem> results = searchService.search(q);
        return ApiResult.of(HttpStatus.OK.value(), "Search results", results);
    }

    @Operation(
            summary = "사기 맥락별 건수 통계 조회",
            description = "전체 리포트를 사기 맥락(scam_context)별로 집계하여 건수 내림차순으로 반환합니다."
    )
    @GetMapping("/scam-contexts/statistics")
    public ApiResult<List<ScamActionStatItem>> getScamContextStatistics() {
        List<ScamActionStatItem> stats = reportService.getScamContextStats();
        return ApiResult.of(HttpStatus.OK.value(), "Scam context statistics", stats);
    }

    @Operation(
            summary = "특정 국가의 사기 유형별 건수 통계 조회",
            description = "선택한 국가의 리포트를 사기 유형(scam_action)별로 집계하여 건수 내림차순으로 반환합니다."
    )
    @GetMapping("/countries/{countryId}/scam-actions/statistics")
    public ApiResult<List<ScamActionStatItem>> getScamActionStatsByCountry(@PathVariable Long countryId) {
        List<ScamActionStatItem> stats = reportService.getScamActionStatsByCountry(countryId);
        return ApiResult.of(HttpStatus.OK.value(), "Scam action statistics by country", stats);
    }

    @Operation(
            summary = "특정 국가의 사기 맥락별 건수 통계 조회",
            description = "선택한 국가의 리포트를 사기 맥락(scam_context)별로 집계하여 건수 내림차순으로 반환합니다."
    )
    @GetMapping("/countries/{countryId}/scam-contexts/statistics")
    public ApiResult<List<ScamActionStatItem>> getScamContextStatsByCountry(@PathVariable Long countryId) {
        List<ScamActionStatItem> stats = reportService.getScamContextStatsByCountry(countryId);
        return ApiResult.of(HttpStatus.OK.value(), "Scam context statistics by country", stats);
    }

    @Operation(
            summary = "지도 통계 요약 조회",
            description = "지도 zoom 레벨에 따라 적절한 집계 단위(국가/주/도시)의 통계 개요 정보를 반환합니다. " +
                         "zoom < 6: 국가별 통계, 6 <= zoom < 9: 주별 통계, zoom >= 9: 도시별 통계"
    )
    @GetMapping("/map/overview")
    public ApiResult<RegionScamStatisticsResponse> getMapOverview(@RequestParam Integer zoom){
        RegionScamStatisticsResponse summaries;

        if (zoom < 6) {
            summaries = reportService.getCountryStatistics();
        } else if (zoom < 9) {
            summaries = reportService.getStateStatistics();
        } else {
            summaries = reportService.getCityStatistics();
        }
        return ApiResult.of(HttpStatus.OK.value(), "Map overview by zoom level", summaries);
    }

}
