package com.swkim.safetrip.repository;

public interface ReportRepositoryCustom {

    Page<ReportResponse> findByCountryAndCity(String country, String city, String pageable)
}
