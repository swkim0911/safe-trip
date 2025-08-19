package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String findCountrySummariesSQL = """
        SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
        FROM (
            SELECT country_id FROM user_report
            UNION ALL
            SELECT country_id FROM external_report
        ) r
        JOIN country c ON r.country_id = c.id
        GROUP BY c.id, c.name, c.lat, c.lng
    """;

    public List<LocationScamSummaryItem> findCountrySummaries() {
        return jdbc.query(findCountrySummariesSQL, Map.of(), (rs, i) -> new LocationScamSummaryItem(
                        rs.getLong("id"),
                        rs.getString("name"),
                rs.getLong("scam_cnt"),
                rs.getDouble("lat"),
                rs.getDouble("lng")
                ));
    }



}
