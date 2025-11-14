package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.LocationScamStatisticsItem;
import com.swkim.safetrip.dto.response.LocationScamStatisticsResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.swkim.safetrip.dto.response.LocationScamStatisticsResponse.LocationType.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJdbcRepository reportJdbcRepository;
    private final ReportNativeRepository reportNativeRepository;

    @Transactional(readOnly = true)
    public LocationScamStatisticsResponse getCountryStatistics(){
        List<LocationScamStatisticsItem> countryStatisticsItems = reportJdbcRepository.findCountryStatistics();
        return new LocationScamStatisticsResponse(COUNTRY, countryStatisticsItems);
    }

    @Transactional(readOnly = true)
    public LocationScamStatisticsResponse getStateStatistics(){
        List<LocationScamStatisticsItem> stateStatisticsItems = reportJdbcRepository.findStateStatistics();
        return new LocationScamStatisticsResponse(STATE, stateStatisticsItems);
    }

    @Transactional(readOnly = true)
    public LocationScamStatisticsResponse getCityStatistics(){
        List<LocationScamStatisticsItem> cityStatisticsItems = reportJdbcRepository.findCityStatistics();
        return new LocationScamStatisticsResponse(CITY, cityStatisticsItems);
    }

}
