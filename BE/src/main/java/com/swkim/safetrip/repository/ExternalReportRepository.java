package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.ExternalReportDetailResponse;
import com.swkim.safetrip.entity.ExternalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExternalReportRepository extends JpaRepository<ExternalReport, Long> {

    @Query("""
    select new com.swkim.safetrip.dto.response.ExternalReportDetailResponse(
        er.source,
        er.sourceUrl,
        er.title,
        s.name,
        c.name,
        st.name,
        er.description,
        er.originalCreatedAt,
        er.collectedAt
    )
    from ExternalReport er
    join er.scam s
    join er.country c
    join er.state st
    where er.id = :id
    """)
    Optional<ExternalReportDetailResponse> findReportDetailById(Long id);
}
