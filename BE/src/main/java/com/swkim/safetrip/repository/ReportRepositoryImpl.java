package com.swkim.safetrip.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.swkim.safetrip.entity.QCity.city;
import static com.swkim.safetrip.entity.QCountry.country;
import static com.swkim.safetrip.entity.QLocation.location;
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

        List<ReportFindAllResponse> reportFindAllResponses = jpaQueryFactory.select(Projections.fields(
                        ReportFindAllResponse.class,
                        report.id,
                        report.title,
                        report.scam.name.as("scam")))
                .from(report)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .fetch();

        return new PageImpl<>(reportFindAllResponses, pageable, reportFindAllResponses.size());
    }

    @Override
    public Page<LocationSummaryItem> findCountrySummaryPage(Pageable pageable) {
        List<LocationSummaryItem> locationSummaryItems = jpaQueryFactory.select(Projections.fields(
                        LocationSummaryItem.class,
                        country.id,
                        country.name,
                        location.count().as("scamCnt"),
                        country.lat,
                        country.lng))
                .from(location)
                .join(location.country, country)
                .groupBy(country.id, country.name, country.lat, country.lng)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(jpaQueryFactory
                .select(country.id.countDistinct()) // 전체 그룹 수
                .from(location)
                .join(location.country, country)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(locationSummaryItems, pageable, total);
    }

    @Override
    public Page<LocationSummaryItem> findCitySummaryPage(Long countryId, Pageable pageable) {

        List<LocationSummaryItem> locationSummaryItems = jpaQueryFactory.select(Projections.fields(
                        LocationSummaryItem.class,
                        city.id,
                        city.name,
                        location.count().as("scamCnt"),
                        city.lat,
                        city.lng))
                .from(location)
                .join(location.city, city)
                .groupBy(city.id, city.name, city.lat, city.lng)
                .where(location.country.id.eq(countryId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(jpaQueryFactory
                .select(city.id.countDistinct()) // 전체 그룹 수
                .from(location)
                .join(location.city, city)
                .where(location.country.id.eq(countryId))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(locationSummaryItems, pageable, total);
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
