package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReportJdbcRepositoryTest {

    @Autowired
    private ReportJdbcRepository reportJdbcRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long koreaId;
    private Long seoulId;

    @BeforeEach
    void setUp() {
        // ext_* 집계 테이블은 JPA 엔티티가 아니므로 직접 생성
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ext_country_stats (
                    country_id BIGINT NOT NULL PRIMARY KEY,
                    scam_cnt   BIGINT NOT NULL DEFAULT 0
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ext_state_stats (
                    state_id BIGINT NOT NULL PRIMARY KEY,
                    scam_cnt BIGINT NOT NULL DEFAULT 0
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ext_city_stats (
                    city_id  BIGINT NOT NULL PRIMARY KEY,
                    scam_cnt BIGINT NOT NULL DEFAULT 0
                )
                """);

        // 국가: Korea(scam O), Japan(scam X)
        jdbcTemplate.update("INSERT INTO countries (dataset_id, iso2, name, lat, lng, created_at, updated_at) VALUES (1, 'KR', 'Korea', 37.5, 127.0, NOW(), NOW())");
        jdbcTemplate.update("INSERT INTO countries (dataset_id, iso2, name, lat, lng, created_at, updated_at) VALUES (2, 'JP', 'Japan', 35.6, 139.7, NOW(), NOW())");
        koreaId = jdbcTemplate.queryForObject("SELECT id FROM countries WHERE iso2 = 'KR'", Long.class);
        jdbcTemplate.update("INSERT INTO ext_country_stats (country_id, scam_cnt) VALUES (?, 5)", koreaId);

        // 주(state): Seoul(scam O), Busan(scam X) — 둘 다 Korea 소속
        jdbcTemplate.update("INSERT INTO states (dataset_id, country_id, name, lat, lng, created_at, updated_at) VALUES (1, ?, 'Seoul', 37.5, 127.0, NOW(), NOW())", koreaId);
        jdbcTemplate.update("INSERT INTO states (dataset_id, country_id, name, lat, lng, created_at, updated_at) VALUES (2, ?, 'Busan', 35.1, 129.0, NOW(), NOW())", koreaId);
        seoulId = jdbcTemplate.queryForObject("SELECT id FROM states WHERE name = 'Seoul'", Long.class);
        jdbcTemplate.update("INSERT INTO ext_state_stats (state_id, scam_cnt) VALUES (?, 3)", seoulId);

        // 도시: Gangnam(scam O), Seocho(scam X) — 둘 다 Seoul 소속
        jdbcTemplate.update("INSERT INTO cities (dataset_id, country_id, state_id, name, lat, lng, created_at, updated_at) VALUES (1, ?, ?, 'Gangnam', 37.52, 127.05, NOW(), NOW())", koreaId, seoulId);
        jdbcTemplate.update("INSERT INTO cities (dataset_id, country_id, state_id, name, lat, lng, created_at, updated_at) VALUES (2, ?, ?, 'Seocho', 37.48, 127.01, NOW(), NOW())", koreaId, seoulId);
        Long gangnamId = jdbcTemplate.queryForObject("SELECT id FROM cities WHERE name = 'Gangnam'", Long.class);
        jdbcTemplate.update("INSERT INTO ext_city_stats (city_id, scam_cnt) VALUES (?, 2)", gangnamId);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM ext_city_stats");
        jdbcTemplate.execute("DELETE FROM ext_state_stats");
        jdbcTemplate.execute("DELETE FROM ext_country_stats");
        jdbcTemplate.execute("DELETE FROM user_country_stats");
        jdbcTemplate.execute("DELETE FROM cities");
        jdbcTemplate.execute("DELETE FROM states");
        jdbcTemplate.execute("DELETE FROM countries");
    }

    @Test
    void scam_데이터가_없는_국가는_지도_통계에_포함되지_않는다() {
        List<RegionScamStatisticsItem> result = reportJdbcRepository.findCountryStatistics();

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(item -> item.scamCnt() > 0);
        assertThat(result).noneMatch(item -> item.name().equals("Japan"));
    }

    @Test
    void scam_데이터가_없는_주는_지도_통계에_포함되지_않는다() {
        List<RegionScamStatisticsItem> result = reportJdbcRepository.findStateStatistics();

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(item -> item.scamCnt() > 0);
        assertThat(result).noneMatch(item -> item.name().equals("Busan"));
    }

    @Test
    void scam_데이터가_없는_주는_국가별_통계에_포함되지_않는다() {
        List<RegionScamStatisticsItem> result = reportJdbcRepository.findStateStatisticsByCountryId(koreaId);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(item -> item.scamCnt() > 0);
        assertThat(result).noneMatch(item -> item.name().equals("Busan"));
    }

    @Test
    void scam_데이터가_없는_도시는_지도_통계에_포함되지_않는다() {
        List<RegionScamStatisticsItem> result = reportJdbcRepository.findCityStatistics();

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(item -> item.scamCnt() > 0);
        assertThat(result).noneMatch(item -> item.name().equals("Seocho"));
    }
}
