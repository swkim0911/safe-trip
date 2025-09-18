package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.entity.enums.Source;
import com.swkim.safetrip.global.exception.custom.InvalidSortKeyException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReportNativeRepository {

    private final EntityManager em;

    public Slice<LocationScamSummaryItem> findCountrySummarySlice(Pageable pageable) {
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
            FROM (
                SELECT country_id FROM user_report
                UNION ALL
                SELECT country_id FROM external_report
            ) r
            JOIN countries c ON r.country_id = c.id
            WHERE c.lat IS NOT NULL AND c.lng IS NOT NULL
            GROUP BY c.id, c.name, c.lat, c.lng
            ORDER BY %s
            LIMIT :limit OFFSET :offset
            """, orderBy);


        Query query = em.createNativeQuery(sql);

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        query.setParameter("limit", pageSize + 1);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // DTO 매핑
        List<LocationScamSummaryItem> items = getLocationScamSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<LocationScamSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<LocationScamSummaryItem> findStateSummarySliceByCountryId(Long countryId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT s.id, s.name, s.lat, s.lng, COUNT(*) AS scam_cnt
            FROM (
                SELECT state_id FROM user_report WHERE country_id = :countryId
                UNION ALL
                SELECT state_id FROM external_report WHERE country_id = :countryId
            ) r
            JOIN states s ON r.state_id = s.id
            WHERE s.lat IS NOT NULL AND s.lng IS NOT NULL
            GROUP BY s.id, s.name, s.lat, s.lng
            ORDER BY %s
            LIMIT :limit OFFSET :offset
            """, orderBy);

        Query query = em.createNativeQuery(sql);
        query.setParameter("countryId", countryId);

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        query.setParameter("limit", pageSize + 1);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // DTO 매핑
        List<LocationScamSummaryItem> items = getLocationScamSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<LocationScamSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<LocationScamSummaryItem> findCitySummarySliceByStateId(Long stateId, Pageable pageable) {
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
            FROM (
                SELECT city_id FROM user_report WHERE state_id = :stateId
                UNION ALL
                SELECT city_id FROM external_report WHERE state_id = :stateId
            ) r
            JOIN cities c ON r.city_id = c.id
            WHERE c.lat IS NOT NULL AND c.lng IS NOT NULL
            GROUP BY c.id, c.name, c.lat, c.lng
            ORDER BY %s
            LIMIT :limit OFFSET :offset
            """, orderBy);

        Query query = em.createNativeQuery(sql);
        query.setParameter("stateId", stateId);

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        query.setParameter("limit", pageSize + 1);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // DTO 매핑
        List<LocationScamSummaryItem> items = getLocationScamSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<LocationScamSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<ReportSummaryItem> findReportSummarySliceByCountryIdAndStateId(Long countryId, Long stateId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
                SELECT r.report_id, r.source, r.title, s.name as scam_name
                FROM (
                    SELECT report_id, source, title, scam_id FROM user_report WHERE countryId = :countryId AND stateId = :stateId
                    UNION ALL
                    SELECT report_id, source, title, scam_id FROM external_report WHERE countryId = :countryId AND stateId = :stateId
                ) r
                JOIN scamAction s on r.scam_id = s.id
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """, orderBy);

        Query query = em.createNativeQuery(sql);
        query.setParameter("countryId", countryId);
        query.setParameter("stateId", stateId);

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        query.setParameter("limit", pageSize + 1);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // DTO 매핑
        List<ReportSummaryItem> items = getReportSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<ReportSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private List<ReportSummaryItem> getReportSummaryItems(List<Object[]> results) {
        return results.stream()
                .map(row -> new ReportSummaryItem(
                        ((Number) row[0]).longValue(),
                        toSource(row[1]), // DB 문자열 → Enum 변환
                        (String)row[2],
                        (String)row[3]
                ))
                .toList();
    }

    private List<LocationScamSummaryItem> getLocationScamSummaryItems(List<Object[]> results) {
        return results.stream()
                .map(row -> new LocationScamSummaryItem(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }

    private Source toSource(Object source) {
        return Source.valueOf(((String) (source)).toUpperCase());
    }

    private String getOrderBy(Pageable pageable) {
        Sort sort = pageable.getSort();

        // 안전한 매핑
        Map<String, String> SORT_MAPPING = Map.of(
                "countryName", "c.name",
                "stateName", "s.name",
                "scamCnt", "scam_cnt",
                "createdAt", "created_at"
        );
        String orderBy = sort.stream()
                .map(order -> {
                    String column = SORT_MAPPING.get(order.getProperty());
                    if (column == null) {
                        throw new InvalidSortKeyException();
                    }
                    return column + " " + order.getDirection().name();
                })
                .collect(Collectors.joining(", "));

        if (orderBy.isBlank()) {
            orderBy = "created_at DESC"; // 기본값
        }
        return orderBy;
    }
}
