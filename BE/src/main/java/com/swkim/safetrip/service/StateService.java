package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.LocationScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.dto.response.world.StatesResponse.StateDto;
import com.swkim.safetrip.entity.world.State;
import com.swkim.safetrip.global.exception.custom.StateNotFoundException;
import com.swkim.safetrip.repository.ReportNativeRepository;
import com.swkim.safetrip.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateService{

    private final StateRepository stateRepository;
    private final ReportNativeRepository reportNativeRepository;

    public State findStateByIdWithCountry(Long id) {
        return stateRepository.findByIdWithCountry(id).orElseThrow(StateNotFoundException::new);
    }

    public StatesResponse getStates(Long countryId) {
        List<StateDto> states = stateRepository.findByCountryId(countryId).stream()
                .map(s -> new StateDto(s.getId(), s.getName()))
                .toList();

        return new StatesResponse(states);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamStatisticsItem> getStateStatistics(Long countryId, Pageable pageable) {
        return reportNativeRepository.findStateStatisticsSliceByCountryId(countryId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByState(Long stateId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByStateId(stateId, pageable);
    }
}
