package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.ExternalReport;
import com.swkim.safetrip.entity.ReportInaccuracy;
import com.swkim.safetrip.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportInaccuracyRepository extends JpaRepository<ReportInaccuracy, Long> {

    boolean existsByExternalReportAndUser(ExternalReport externalReport, User user);

    @Query("select ri from ReportInaccuracy ri join fetch ri.externalReport join fetch ri.user order by ri.createdAt desc")
    List<ReportInaccuracy> findAllWithDetails();
}
