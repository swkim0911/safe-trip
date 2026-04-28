package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.ExternalReport;
import com.swkim.safetrip.entity.ReportInaccuracy;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.ReportInaccuracyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportInaccuracyRepository extends JpaRepository<ReportInaccuracy, Long> {

    boolean existsByExternalReportAndUserAndStatus(ExternalReport externalReport, User user, ReportInaccuracyStatus status);

    @Query("select ri from ReportInaccuracy ri join fetch ri.externalReport join fetch ri.user order by ri.createdAt desc")
    List<ReportInaccuracy> findAllWithDetails();

    @Query("select ri from ReportInaccuracy ri join fetch ri.externalReport where ri.user = :user order by ri.createdAt desc")
    List<ReportInaccuracy> findAllByUser(User user);

    void deleteAllByUser(User user);
}
