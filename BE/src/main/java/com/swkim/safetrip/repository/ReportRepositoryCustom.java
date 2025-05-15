package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReportRepositoryCustom {

    Slice<ReportSummaryItem> findReportSummarySliceByCountryAndCity(Long country, Long city, Pageable pageable);

    Slice<LocationSummaryItem> findCountrySummarySlice(Pageable pageable);

    Slice<LocationSummaryItem> findCitySummarySlice(Long countryId, Pageable pageable);
}
