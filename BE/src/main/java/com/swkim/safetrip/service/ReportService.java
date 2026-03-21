package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.RegionScamStatisticsResponse;
import com.swkim.safetrip.enums.RiskLevel;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swkim.safetrip.dto.response.RegionScamStatisticsResponse.RegionType.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJdbcRepository reportJdbcRepository;
    private final ReportNativeRepository reportNativeRepository;

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCountryStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findCountryStatistics();
        return new RegionScamStatisticsResponse(COUNTRY, assignRiskLevels(items));
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getStateStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findStateStatistics();
        return new RegionScamStatisticsResponse(STATE, assignRiskLevels(items));
    }

    @Transactional(readOnly = true)
    public RegionScamStatisticsResponse getCityStatistics(){
        List<RegionScamStatisticsItem> items = reportJdbcRepository.findCityStatistics();
        return new RegionScamStatisticsResponse(CITY, assignRiskLevels(items));
    }

    private List<RegionScamStatisticsItem> assignRiskLevels(List<RegionScamStatisticsItem> items) {
        if (items.isEmpty()) return items;

        List<RegionScamStatisticsItem> sorted = items.stream()
                .sorted(Comparator.comparingLong(RegionScamStatisticsItem::scamCnt))
                .toList();

        int size = sorted.size();
        int lowEnd = (int) Math.ceil(size * 0.2);
        int highStart = (int) Math.floor(size * 0.8);

        Map<Long, RiskLevel> riskMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            RiskLevel level = i < lowEnd ? RiskLevel.LOW
                    : i >= highStart ? RiskLevel.HIGH
                    : RiskLevel.MEDIUM;
            riskMap.put(sorted.get(i).id(), level);
        }

        return items.stream()
                .map(item -> RegionScamStatisticsItem.builder()
                        .id(item.id())
                        .name(item.name())
                        .lat(item.lat())
                        .lng(item.lng())
                        .scamCnt(item.scamCnt())
                        .iso2(item.iso2())
                        .riskLevel(riskMap.get(item.id()))
                        .build())
                .toList();
    }

}
