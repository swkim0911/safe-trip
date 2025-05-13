package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepositoryCustom {

    Page<ReportFindAllResponse> findAllByCountryAndCity(String country, String city, Pageable pageable);

    Page<LocationSummaryItem> findCountrySummary(Pageable pageable);
}
