package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReportRepositoryCustom {

    Page<ReportFindAllResponse> findAllByCountryAndCity(String country, String city, Pageable pageable);

    Slice<LocationSummaryItem> findCountrySummaryPage(Pageable pageable);

    Slice<LocationSummaryItem> findCitySummaryPage(Long countryId, Pageable pageable);
}
