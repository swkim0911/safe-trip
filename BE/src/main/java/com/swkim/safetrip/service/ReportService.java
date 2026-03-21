package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.RegionScamStatisticsResponse;
import com.swkim.safetrip.entity.enums.RiskLevel;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        List<Long> distinctCounts = items.stream()
                .map(RegionScamStatisticsItem::scamCnt)
                .distinct()
                .sorted()
                .toList();

        int n = distinctCounts.size();
        int lowEnd = (int) Math.ceil(n * 0.2);
        int highStart = (int) Math.floor(n * 0.8);

        Map<Long, RiskLevel> countToLevel = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            RiskLevel level = i < lowEnd ? RiskLevel.LOW
                    : i >= highStart ? RiskLevel.HIGH
                    : RiskLevel.MEDIUM;
            countToLevel.put(distinctCounts.get(i), level);
        }

        return items.stream()
                .map(item -> RegionScamStatisticsItem.builder()
                        .id(item.id())
                        .name(item.name())
                        .lat(item.lat())
                        .lng(item.lng())
                        .scamCnt(item.scamCnt())
                        .iso2(item.iso2())
                        .riskLevel(countToLevel.get(item.scamCnt()))
                        .build())
                .toList();
    }

}
