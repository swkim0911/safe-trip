package com.swkim.safetrip.repository;

public class ReportRepositoryImpl implements ReportRepositoryCustom {

    @Override
    public Page<ReportResponse> findByCountryAndCity(String country, String city, String pageable) {
        return null;
    }
}
