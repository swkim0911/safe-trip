package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.RegionScamStatisticsResponse;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.swkim.safetrip.dto.response.RegionScamStatisticsResponse.RegionType.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJdbcRepository reportJdbcRepository;
    private final ReportNativeRepository reportNativeRepository;
    private final RiskLevelService riskLevelService;

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCountryStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findCountryStatistics();
        return new RegionScamStatisticsResponse(COUNTRY, riskLevelService.assignRiskLevels(items));
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getStateStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findStateStatistics();
        return new RegionScamStatisticsResponse(STATE, riskLevelService.assignRiskLevels(items));
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCityStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findCityStatistics();
        return new RegionScamStatisticsResponse(CITY, riskLevelService.assignRiskLevels(items));
    }

}
