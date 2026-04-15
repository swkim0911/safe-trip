package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.UserCountryStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCountryStatsRepository extends JpaRepository<UserCountryStats, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO user_country_stats (country_id, scam_cnt) VALUES (:countryId, 1)
        ON DUPLICATE KEY UPDATE scam_cnt = scam_cnt + 1
        """, nativeQuery = true)
    void increment(@Param("countryId") Long countryId);

    @Modifying
    @Query(value = """
        UPDATE user_country_stats SET scam_cnt = scam_cnt - 1 WHERE country_id = :countryId
        """, nativeQuery = true)
    void decrement(@Param("countryId") Long countryId);

}
