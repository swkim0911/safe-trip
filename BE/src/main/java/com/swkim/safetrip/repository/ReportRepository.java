package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    @Query("SELECT r FROM Report r JOIN FETCH r.location WHERE r.id = :id")
    Report findReportWithLocationById(@Param("id")Long id);
}
