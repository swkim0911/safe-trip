package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
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
        Sort sort = pageable.getSort();

        // 안전한 매핑
        Map<String, String> SORT_MAPPING = Map.of(
                "name", "c.name",
                "scamCnt", "scam_cnt"
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
            orderBy = "c.name ASC"; // 기본값
        }


        String sql = """
            SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
            FROM (
                SELECT country_id FROM user_report
                UNION ALL
                SELECT country_id FROM external_report
            ) r
            JOIN country c ON r.country_id = c.id
            WHERE c.lat IS NOT NULL AND c.lng IS NOT NULL
            GROUP BY c.id, c.name, c.lat, c.lng
            ORDER BY """ + orderBy + """
            LIMIT :limit OFFSET :offset
            """;

        Query query = em.createNativeQuery(sql);

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        query.setParameter("limit", pageSize + 1);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // DTO 매핑
        List<LocationScamSummaryItem> items = results.stream()
                .map(row -> new LocationScamSummaryItem(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).doubleValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();

        boolean hasNext = items.size() > pageSize;
        List<LocationScamSummaryItem> content = hasNext ? items.subList(0, pageSize) : items;

        return new SliceImpl<>(content, pageable, hasNext);
    }
}

