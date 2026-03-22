package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ScamActionStatItem;
import com.swkim.safetrip.dto.response.SearchResultItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String findScamActionStatsSQL = """
        SELECT sa.name, COUNT(*) AS cnt
        FROM (
            SELECT scam_action_id FROM user_report   WHERE scam_action_id IS NOT NULL
            UNION ALL
            SELECT scam_action_id FROM external_report WHERE scam_action_id IS NOT NULL
        ) r
        JOIN scam_action sa ON r.scam_action_id = sa.id
        GROUP BY sa.id, sa.name
        ORDER BY cnt DESC
    """;

    private static final String findScamActionStatsByCountrySQL = """
        SELECT sa.name, COUNT(*) AS cnt
        FROM (
            SELECT scam_action_id FROM user_report   WHERE scam_action_id IS NOT NULL AND country_id = :countryId
            UNION ALL
            SELECT scam_action_id FROM external_report WHERE scam_action_id IS NOT NULL AND country_id = :countryId
        ) r
        JOIN scam_action sa ON r.scam_action_id = sa.id
        GROUP BY sa.id, sa.name
        ORDER BY cnt DESC
    """;

    private static final String searchSQL = """
        SELECT id, name, 'COUNTRY' AS type, lat, lng, NULL AS country_id
        FROM countries WHERE name LIKE :q
          AND EXISTS (
            SELECT 1 FROM user_report WHERE country_id = countries.id
            UNION ALL
            SELECT 1 FROM external_report WHERE country_id = countries.id
          )
        UNION ALL
        SELECT id, name, 'STATE', lat, lng, country_id
        FROM states WHERE name LIKE :q AND lat IS NOT NULL
          AND EXISTS (
            SELECT 1 FROM user_report WHERE state_id = states.id
            UNION ALL
            SELECT 1 FROM external_report WHERE state_id = states.id
          )
        UNION ALL
        SELECT id, name, 'CITY', lat, lng, country_id
        FROM cities WHERE name LIKE :q AND lat IS NOT NULL
          AND EXISTS (
            SELECT 1 FROM user_report WHERE city_id = cities.id
            UNION ALL
            SELECT 1 FROM external_report WHERE city_id = cities.id
          )
        LIMIT 10
    """;

    private static final String findScamContextStatsSQL = """
        SELECT sc.name, COUNT(*) AS cnt
        FROM (
            SELECT scam_context_id FROM user_report   WHERE scam_context_id IS NOT NULL
            UNION ALL
            SELECT scam_context_id FROM external_report WHERE scam_context_id IS NOT NULL
        ) r
        JOIN scam_context sc ON r.scam_context_id = sc.id
        GROUP BY sc.id, sc.name
        ORDER BY cnt DESC
    """;

    private static final String findScamContextStatsByCountrySQL = """
        SELECT sc.name, COUNT(*) AS cnt
        FROM (
            SELECT scam_context_id FROM user_report   WHERE scam_context_id IS NOT NULL AND country_id = :countryId
            UNION ALL
            SELECT scam_context_id FROM external_report WHERE scam_context_id IS NOT NULL AND country_id = :countryId
        ) r
        JOIN scam_context sc ON r.scam_context_id = sc.id
        GROUP BY sc.id, sc.name
        ORDER BY cnt DESC
    """;

    private static final String findCountryInfoSQL = """
        SELECT c.id, c.name, NULL AS lat, NULL AS lng,
          (SELECT COUNT(*) FROM user_report WHERE country_id = c.id) +
          (SELECT COUNT(*) FROM external_report WHERE country_id = c.id) AS scam_cnt
        FROM countries c WHERE c.id = :id
    """;

    private static final String findStateInfoSQL = """
        SELECT s.id, s.name, NULL AS lat, NULL AS lng,
          (SELECT COUNT(*) FROM user_report WHERE state_id = s.id) +
          (SELECT COUNT(*) FROM external_report WHERE state_id = s.id) AS scam_cnt
        FROM states s WHERE s.id = :id
    """;

    private static final String findCountryStatisticsSQL = """
        SELECT c.id, c.name, c.lat, c.lng, COUNT(*) AS scam_cnt
        FROM (
            SELECT country_id FROM user_report
            UNION ALL
            SELECT country_id FROM external_report
        ) r
        JOIN countries c ON r.country_id = c.id
        GROUP BY c.id, c.name, c.lat, c.lng
    """;

    private static final String findStateStatisticsSQL = """
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

    private static final String findCityStatisticsSQL = """
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

    public List<SearchResultItem> search(String keyword) {
        return jdbc.query(searchSQL, Map.of("q", keyword), (rs, i) -> SearchResultItem.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .lat(rs.getDouble("lat"))
                .lng(rs.getDouble("lng"))
                .countryId(rs.getObject("country_id", Long.class))
                .build());
    }

    public List<ScamActionStatItem> findScamActionStats() {
        return jdbc.query(findScamActionStatsSQL, Map.of(), (rs, i) ->
                new ScamActionStatItem(rs.getString("name"), rs.getLong("cnt")));
    }

    public List<ScamActionStatItem> findScamActionStatsByCountry(Long countryId) {
        return jdbc.query(findScamActionStatsByCountrySQL, Map.of("countryId", countryId), (rs, i) ->
                new ScamActionStatItem(rs.getString("name"), rs.getLong("cnt")));
    }

    public List<ScamActionStatItem> findScamContextStats() {
        return jdbc.query(findScamContextStatsSQL, Map.of(), (rs, i) ->
                new ScamActionStatItem(rs.getString("name"), rs.getLong("cnt")));
    }

    public List<ScamActionStatItem> findScamContextStatsByCountry(Long countryId) {
        return jdbc.query(findScamContextStatsByCountrySQL, Map.of("countryId", countryId), (rs, i) ->
                new ScamActionStatItem(rs.getString("name"), rs.getLong("cnt")));
    }

    public List<RegionScamStatisticsItem> findCountryStatistics() {
        return jdbc.query(findCountryStatisticsSQL, Map.of(), (rs, i) -> new RegionScamStatisticsItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt"),
                null,
                null
                ));
    }

    public List<RegionScamStatisticsItem> findStateStatistics() {
        return jdbc.query(findStateStatisticsSQL, Map.of(), (rs, i) -> new RegionScamStatisticsItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt"),
                null,
                null
        ));
    }

    public List<RegionScamStatisticsItem> findCityStatistics() {
        return jdbc.query(findCityStatisticsSQL, Map.of(), (rs, i) -> new RegionScamStatisticsItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getLong("scam_cnt"),
                null,
                null
        ));
    }

    public RegionScamStatisticsItem findCountryInfo(Long id) {
        return jdbc.queryForObject(findCountryInfoSQL, Map.of("id", id), (rs, i) -> new RegionScamStatisticsItem(
                rs.getLong("id"),
                rs.getString("name"),
                null,
                null,
                rs.getLong("scam_cnt"),
                null,
                null
        ));
    }

    public RegionScamStatisticsItem findStateInfo(Long id) {
        return jdbc.queryForObject(findStateInfoSQL, Map.of("id", id), (rs, i) -> new RegionScamStatisticsItem(
                rs.getLong("id"),
                rs.getString("name"),
                null,
                null,
                rs.getLong("scam_cnt"),
                null,
                null
        ));
    }

}
