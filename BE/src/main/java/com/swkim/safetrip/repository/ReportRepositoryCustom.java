package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {

    Page<ReportFindAllResponse> findAllByCountryAndCity(String country, String city, Pageable pageable);

    Page<LocationSummaryItem> findCountrySummaryPage(Pageable pageable);
}
