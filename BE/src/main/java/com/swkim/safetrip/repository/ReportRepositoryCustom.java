package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.ReportResponse;
import com.swkim.safetrip.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {

    Page<ReportResponse> findByCountryAndCity(String country, String city, Pageable pageable);

    Report findByTitle(String title);
}
