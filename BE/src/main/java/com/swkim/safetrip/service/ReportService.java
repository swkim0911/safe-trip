package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.LocationScamSummaryResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.swkim.safetrip.dto.response.LocationScamSummaryResponse.LocationType.COUNTRY;
import static com.swkim.safetrip.dto.response.LocationScamSummaryResponse.LocationType.STATE;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJdbcRepository reportJdbcRepository;
    private final ReportNativeRepository reportNativeRepository;

    @Transactional(readOnly = true)
    public LocationScamSummaryResponse getCountrySummaries(){
        List<LocationScamSummaryItem> countrySummariesItems = reportJdbcRepository.findCountrySummaries();
        return new LocationScamSummaryResponse(COUNTRY, countrySummariesItems);
    }

    @Transactional(readOnly = true)
    public LocationScamSummaryResponse getStateSummaries(){
        List<LocationScamSummaryItem> stateSummariesItems = reportJdbcRepository.findStateSummaries();
        return new LocationScamSummaryResponse(STATE, stateSummariesItems);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamSummaryItem> getCountrySummaryPages(Pageable pageable) {
        return reportNativeRepository.findCountrySummarySlice(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamSummaryItem> getStateSummaryPages(Long countryId, Pageable pageable) {
        return reportNativeRepository.findStateSummarySliceByCountryId(countryId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamSummaryItem> getCitySummaryPages(Long stateId, Pageable pageable) {
        return reportNativeRepository.findCitySummarySliceByStateId(stateId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportSummaryPages(Long countryId, Long stateId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByCountryIdAndStateId(countryId, stateId, pageable);
    }
}
