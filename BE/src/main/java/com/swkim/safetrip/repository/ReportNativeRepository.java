package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
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

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReportNativeRepository {

    private final EntityManager em;

    public Slice<RegionScamStatisticsItem> findCountryStatisticsSlice(Pageable pageable) {
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT c.id, c.name, c.lat, c.lng, c.iso2,
                   COALESCE(es.scam_cnt, 0) + COALESCE(ur.scam_cnt, 0) AS scam_cnt
            FROM countries c
            LEFT JOIN ext_country_stats es ON es.country_id = c.id
            LEFT JOIN (
                SELECT country_id, COUNT(*) AS scam_cnt
                FROM user_report WHERE deleted_at IS NULL
                GROUP BY country_id
            ) ur ON ur.country_id = c.id
            WHERE c.lat IS NOT NULL AND c.lng IS NOT NULL
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
        List<RegionScamStatisticsItem> items = getRegionScamStatisticsItems(results);

        boolean hasNext = items.size() > pageSize;
        List<RegionScamStatisticsItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<RegionScamStatisticsItem> findStateStatisticsSliceByCountryId(Long countryId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT s.id, s.name, s.lat, s.lng,
                   COALESCE(es.scam_cnt, 0) + COALESCE(ur.scam_cnt, 0) AS scam_cnt
            FROM states s
            LEFT JOIN ext_state_stats es ON es.state_id = s.id
            LEFT JOIN (
                SELECT state_id, COUNT(*) AS scam_cnt
                FROM user_report WHERE country_id = :countryId AND deleted_at IS NULL AND state_id IS NOT NULL
                GROUP BY state_id
            ) ur ON ur.state_id = s.id
            WHERE s.country_id = :countryId AND s.lat IS NOT NULL AND s.lng IS NOT NULL
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
        List<RegionScamStatisticsItem> items = getRegionScamStatisticsItems(results);

        boolean hasNext = items.size() > pageSize;
        List<RegionScamStatisticsItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<RegionScamStatisticsItem> findCityStatisticsSliceByStateId(Long stateId, Pageable pageable) {
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
            SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
            FROM (
                SELECT city_id FROM user_report WHERE state_id = :stateId AND deleted_at IS NULL
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
        List<RegionScamStatisticsItem> items = getRegionScamStatisticsItems(results);

        boolean hasNext = items.size() > pageSize;
        List<RegionScamStatisticsItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<ReportSummaryItem> findReportSummarySliceByCountryId(Long countryId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
                SELECT r.id, r.source, r.title, sa.name as scam_action_name, sc.name as scam_context_name, r.created_at
                FROM (
                    SELECT id, source, title, scam_action_id, scam_context_id, created_at FROM user_report WHERE country_id = :countryId AND deleted_at IS NULL
                    UNION ALL
                    SELECT id, source, title, scam_action_id, scam_context_id, posted_at as created_at FROM external_report WHERE country_id = :countryId
                ) r
                JOIN scam_action sa on r.scam_action_id = sa.id
                JOIN scam_context sc on r.scam_context_id = sc.id
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
        List<ReportSummaryItem> items = getReportSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<ReportSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<ReportSummaryItem> findReportSummarySliceByStateId(Long stateId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
                SELECT r.id, r.source, r.title, sa.name as scam_action_name, sc.name as scam_context_name, r.created_at
                FROM (
                    SELECT id, source, title, scam_action_id, scam_context_id, created_at FROM user_report WHERE state_id = :stateId AND deleted_at IS NULL
                    UNION ALL
                    SELECT id, source, title, scam_action_id, scam_context_id, posted_at as created_at FROM external_report WHERE state_id = :stateId
                ) r
                JOIN scam_action sa on r.scam_action_id = sa.id
                JOIN scam_context sc on r.scam_context_id = sc.id
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
        List<ReportSummaryItem> items = getReportSummaryItems(results);

        boolean hasNext = items.size() > pageSize;
        List<ReportSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public Slice<ReportSummaryItem> findReportSummarySliceByCityId(Long cityId, Pageable pageable){
        String orderBy = getOrderBy(pageable);

        String sql = String.format("""
                SELECT r.id, r.source, r.title, sa.name as scam_action_name, sc.name as scam_context_name, r.created_at
                FROM (
                    SELECT id, source, title, scam_action_id, scam_context_id, created_at FROM user_report WHERE city_id = :cityId AND deleted_at IS NULL
                    UNION ALL
                    SELECT id, source, title, scam_action_id, scam_context_id, posted_at as created_at FROM external_report WHERE city_id = :cityId
                ) r
                JOIN scam_action sa on r.scam_action_id = sa.id
                JOIN scam_context sc on r.scam_context_id = sc.id
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """, orderBy);
        Query query = em.createNativeQuery(sql);
        query.setParameter("cityId", cityId);

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
                        (String)row[3],
                        (String)row[4],
                        ((Timestamp) row[5]).toLocalDateTime()
                ))
                .toList();
    }

    private List<RegionScamStatisticsItem> getRegionScamStatisticsItems(List<Object[]> results) {
        return results.stream()
                .map(row -> {
                    // 국가 통계(row 길이 6)일 경우 iso2 포함, 그 외(null)
                    if (row.length == 6) {
                        return new RegionScamStatisticsItem(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                ((Number) row[2]).doubleValue(),
                                ((Number) row[3]).doubleValue(),
                                ((Number) row[5]).longValue(),
                                (String) row[4],
                                null
                        );
                    } else {
                        return new RegionScamStatisticsItem(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                ((Number) row[2]).doubleValue(),
                                ((Number) row[3]).doubleValue(),
                                ((Number) row[4]).longValue(),
                                null,
                                null
                        );
                    }
                })
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
                "name", "name",
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
            orderBy = "r.created_at DESC"; // 기본값
        }
        
        // 정렬 안정성을 위해 고유 ID 추가 (페이징 중복 방지)
        if (!orderBy.contains("id")) {
            orderBy += ", id ASC";
        }
        
        return orderBy;
    }
}
