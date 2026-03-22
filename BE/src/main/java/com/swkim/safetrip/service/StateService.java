package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse.StateDto;
import com.swkim.safetrip.entity.enums.RiskLevel;
import com.swkim.safetrip.entity.world.State;
import com.swkim.safetrip.global.exception.custom.StateNotFoundException;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import com.swkim.safetrip.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        Slice<RegionScamStatisticsItem> slice = reportNativeRepository.findStateStatisticsSliceByCountryId(countryId, pageable);
        Map<Long, RiskLevel> riskLevels = riskLevelService.getStateRiskLevels();
        List<RegionScamStatisticsItem> content = slice.getContent().stream()
                .map(item -> RegionScamStatisticsItem.builder()
                        .id(item.id()).name(item.name()).lat(item.lat()).lng(item.lng())
                        .scamCnt(item.scamCnt()).iso2(item.iso2())
                        .riskLevel(riskLevels.get(item.id()))
                        .build())
                .toList();
        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByState(Long stateId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByStateId(stateId, pageable);
    }
}
