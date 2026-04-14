package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.entity.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class RiskLevelService {

    public List<RegionScamStatisticsItem> assignRiskLevels(List<RegionScamStatisticsItem> items) {
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
