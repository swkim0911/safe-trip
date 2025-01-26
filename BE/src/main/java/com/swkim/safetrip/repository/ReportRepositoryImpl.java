package com.swkim.safetrip.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.swkim.safetrip.entity.QReport.report;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ReportFindAllResponse> findAllByCountryAndCity(String country, String city, Pageable pageable) {

        BooleanBuilder conditions = new BooleanBuilder();
        if (country != null) {
            conditions.and(report.location.country.name.eq(country));
        }
        if (city != null) {
            conditions.and(report.location.city.name.eq(city));
        }

        List<ReportFindAllResponse> returnReport = jpaQueryFactory.select(Projections.fields(
                        ReportFindAllResponse.class,
                        report.title,
                        report.category,
                        report.likeCnt
                ))
                .from(report)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(returnReport, pageable, returnReport.size());
    }
}
