package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse.StateDto;
import com.swkim.safetrip.entity.world.State;
import com.swkim.safetrip.global.exception.custom.InvalidSortKeyException;
import com.swkim.safetrip.global.exception.custom.StateNotFoundException;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import com.swkim.safetrip.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StateService{

    private final StateRepository stateRepository;
    private final ReportNativeRepository reportNativeRepository;
    private final ReportJdbcRepository reportJdbcRepository;
    private final RiskLevelService riskLevelService;

    public State findStateByIdWithCountry(Long id) {
        return stateRepository.findByIdWithCountry(id).orElseThrow(StateNotFoundException::new);
    }

    public StatesResponse getStates(Long countryId) {
        List<StateDto> states = stateRepository.findByCountryId(countryId).stream()
                .map(s -> new StateDto(s.getId(), s.getName()))
                .toList();

        return new StatesResponse(states);
    }

    public RegionScamStatisticsItem getStateInfo(Long stateId) {
        return reportJdbcRepository.findStateInfo(stateId);
    }

    @Transactional(readOnly = true)
    public Slice<RegionScamStatisticsItem> getStateStatistics(Long countryId, Pageable pageable) {
        List<RegionScamStatisticsItem> all = reportJdbcRepository.findStateStatisticsByCountryId(countryId);
        List<RegionScamStatisticsItem> withRisk = riskLevelService.assignRiskLevels(all);

        List<RegionScamStatisticsItem> sorted = withRisk.stream()
                .sorted(buildComparator(pageable.getSort()))
                .toList();

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        int end = Math.min(offset + pageSize, sorted.size());
        List<RegionScamStatisticsItem> content = offset >= sorted.size() ? List.of() : sorted.subList(offset, end);

        return new SliceImpl<>(content, pageable, end < sorted.size());
    }

    private Comparator<RegionScamStatisticsItem> buildComparator(Sort sort) {
        if (!sort.iterator().hasNext()) {
            return Comparator.comparingLong(RegionScamStatisticsItem::scamCnt).reversed()
                    .thenComparingLong(RegionScamStatisticsItem::id);
        }
        Comparator<RegionScamStatisticsItem> result = null;
        for (Sort.Order order : sort) {
            Comparator<RegionScamStatisticsItem> c = switch (order.getProperty()) {
                case "scamCnt" -> Comparator.comparingLong(RegionScamStatisticsItem::scamCnt);
                case "name", "stateName" -> Comparator.comparing(RegionScamStatisticsItem::name);
                default -> throw new InvalidSortKeyException();
            };
            if (order.isDescending()) c = c.reversed();
            result = result == null ? c : result.thenComparing(c);
        }
        return result.thenComparingLong(RegionScamStatisticsItem::id);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByState(Long stateId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByStateId(stateId, pageable);
    }
}
