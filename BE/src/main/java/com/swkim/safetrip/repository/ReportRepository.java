package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<UserReport, Long>, ReportRepositoryCustom {

    @Query("SELECT r FROM Report r JOIN FETCH r.location WHERE r.id = :id")
    Optional<UserReport> findReportWithLocationById(@Param("id")Long id);


    @Query("""
            SELECT new com.swkim.safetrip.dto.response.LocationScamSummaryItem(
                c.id,
                c.name,
                COUNT(l),
                c.lat,
                c.lng
            )
            FROM UserReport ur
            JOIN ur.country c
            GROUP BY c.id, c.name, c.lat, c.lng
            """)
    List<LocationScamSummaryItem> findCountrySummary();

    @Query("""
            SELECT new com.swkim.safetrip.dto.response.LocationSummaryItem(
                c.id,
                c.name,
                COUNT(l),
                c.lat,
                c.lng
            )
            FROM Location l
            JOIN l.state c
            GROUP BY c.id, c.name, c.lat, c.lng
            """)
    List<LocationScamSummaryItem> findCitySummary();
}
