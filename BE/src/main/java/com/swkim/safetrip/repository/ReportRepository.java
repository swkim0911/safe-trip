package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.ReportMapSummaryItem;
import com.swkim.safetrip.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    @Query("SELECT r FROM Report r JOIN FETCH r.location WHERE r.id = :id")
    Optional<Report> findReportWithLocationById(@Param("id")Long id);


    @Query("""
            SELECT new com.swkim.safetrip.dto.response.ReportMapSummaryItem(
                c.id,
                c.name,
                COUNT(l),
                c.lat,
                c.lng
            )
            FROM Location l
            JOIN l.country c
            GROUP BY c.id, c.name, c.lat, c.lng
            """)
    List<ReportMapSummaryItem> findScamCountGroupedByCountry();

    @Query("""
            SELECT new com.swkim.safetrip.dto.response.ReportMapSummaryItem(
                c.id,
                c.name,
                COUNT(l),
                c.lat,
                c.lng
            )
            FROM Location l
            JOIN l.city c
            GROUP BY c.id, c.name, c.lat, c.lng
            """)
    List<ReportMapSummaryItem> findScamCountGroupedByCity();
}
