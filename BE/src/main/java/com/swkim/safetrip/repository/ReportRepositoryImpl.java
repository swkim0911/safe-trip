package com.swkim.safetrip.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.ReportResponse;
import com.swkim.safetrip.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.swkim.safetrip.entity.QReport.report;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReportResponse> findByCountryAndCity(String country, String city, Pageable pageable) {
        return null;
    }

    @Override
    public Report findByTitle(String title) {
        List<Report> list = jpaQueryFactory.selectFrom(report)
                .where(report.title.eq(title))
                .fetch();
        return list.get(0);
    }
}
