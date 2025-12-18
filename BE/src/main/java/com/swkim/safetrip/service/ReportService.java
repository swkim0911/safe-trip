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

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCountryStatistics(){
        List<RegionScamStatisticsItem> countryStatisticsItems = reportJdbcRepository.findCountryStatistics();
        return new RegionScamStatisticsResponse(COUNTRY, countryStatisticsItems);
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getStateStatistics(){
        List<RegionScamStatisticsItem> stateStatisticsItems = reportJdbcRepository.findStateStatistics();
        return new RegionScamStatisticsResponse(STATE, stateStatisticsItems);
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCityStatistics(){
        List<RegionScamStatisticsItem> cityStatisticsItems = reportJdbcRepository.findCityStatistics();
        return new RegionScamStatisticsResponse(CITY, cityStatisticsItems);
    }

}
