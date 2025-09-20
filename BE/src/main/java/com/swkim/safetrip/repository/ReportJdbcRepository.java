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
        JOIN countries c ON r.country_id = c.id
        GROUP BY c.id, c.name, c.lat, c.lng
    """;

    private static final String findStateSummariesSQL = """
        SELECT s.id, s.name, s.lat, s.lng, COUNT(*) AS scam_cnt
        FROM (
            SELECT state_id FROM user_report
            UNION ALL
            SELECT state_id FROM external_report
        ) r
        JOIN states s ON r.state_id = s.id
        Where s.lat IS NOT NULL AND s.lng IS NOT NULL
        GROUP BY s.id, s.name, s.lat, s.lng
    """;

    private static final String findCitySummariesSQL = """
        SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
        FROM (
            SELECT city_id FROM user_report
            UNION ALL
            SELECT city_id FROM external_report
        ) r
        JOIN cities c ON r.city_id = c.id
        Where c.lat IS NOT NULL AND c.lng IS NOT NULL
        GROUP BY c.id, c.name, c.lat, c.lng
    """;

    public List<LocationScamSummaryItem> findCountrySummaries() {
        return jdbc.query(findCountrySummariesSQL, Map.of(), (rs, i) -> new LocationScamSummaryItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt")
                ));
    }

    public List<LocationScamSummaryItem> findStateSummaries() {
        return jdbc.query(findStateSummariesSQL, Map.of(), (rs, i) -> new LocationScamSummaryItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt")
        ));
    }

    public List<LocationScamSummaryItem> findCitySummaries() {
        return jdbc.query(findCitySummariesSQL, Map.of(), (rs, i) -> new LocationScamSummaryItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt")
        ));
    }



}
