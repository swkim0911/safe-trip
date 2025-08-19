package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<UserReport, Long>, ReportRepositoryCustom {

    @Query("SELECT r FROM Report r JOIN FETCH r.location WHERE r.id = :id")
    Optional<UserReport> findReportWithLocationById(@Param("id")Long id);
}
