package com.swkim.safetrip.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
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

        List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(pageable); // 정렬 조건

        List<ReportFindAllResponse> returnReport = jpaQueryFactory.select(Projections.fields(
                        ReportFindAllResponse.class,
                        report.title,
                        report.title.stringValue().as("scam"),
                        report.likes.intValue().as("likes")))
                .from(report)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .fetch();

        return new PageImpl<>(returnReport, pageable, returnReport.size());
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> list = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            String property = sortOrder.getProperty();
            PathBuilder<?> orderByExpression = new PathBuilder<>(Report.class, "report");
            list.add(new OrderSpecifier(direction, orderByExpression.get(property)));
        }
        return list;
    }
}
