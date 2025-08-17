package com.swkim.safetrip.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.entity.UserReport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.swkim.safetrip.entity.QCity.city;
import static com.swkim.safetrip.entity.QCountry.country;
import static com.swkim.safetrip.entity.QLocation.location;
import static com.swkim.safetrip.entity.QReport.report;
import static com.swkim.safetrip.entity.QScam.scam;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<ReportSummaryItem> findReportSummarySliceByCountryAndCity(Long countryId, Long cityId, Pageable pageable) {

        int pageSize = pageable.getPageSize();

        List<ReportSummaryItem> reportSummaryItems = jpaQueryFactory.select(Projections.fields(
                        ReportSummaryItem.class,
                        report.id.as("reportId"),
                        report.title,
                        scam.name.as("scam")))
                .from(report)
                .join(report.location, location)
                .join(report.scam, scam)
                .where(
                        location.country.id.eq(countryId),
                        location.city.id.eq(cityId)
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1) // 다음 페이지가 있는지 확인
                .fetch();

        boolean hasNext = reportSummaryItems.size() > pageSize;
        List<ReportSummaryItem> content = hasNext ? reportSummaryItems.subList(0, pageSize) : reportSummaryItems;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<LocationSummaryItem> findCountrySummarySlice(Pageable pageable) {
        int pageSize = pageable.getPageSize();

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
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = locationSummaryItems.size() > pageSize;
        List<LocationSummaryItem> content = hasNext ? locationSummaryItems.subList(0, pageSize) : locationSummaryItems;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<LocationSummaryItem> findCitySummarySlice(Long countryId, Pageable pageable) {
        int pageSize = pageable.getPageSize();

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
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = locationSummaryItems.size() > pageSize;
        List<LocationSummaryItem> content = hasNext ? locationSummaryItems.subList(0, pageSize) : locationSummaryItems;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> list = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            String property = sortOrder.getProperty();
            PathBuilder<?> orderByExpression = new PathBuilder<>(UserReport.class, "report");
            list.add(new OrderSpecifier(direction, orderByExpression.get(property)));
        }
        return list;
    }
}
