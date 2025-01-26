package com.swkim.safetrip.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReportResponse> findByCountryAndCity(String country, String city, Pageable pageable) {
        return null;
    }
}
