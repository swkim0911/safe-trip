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
        er.author,
        sa.name,
        sc.name,
        co.name,
        st.name,
        ci.name,
        er.title,
        er.content,
        er.postedAt
    )
    from ExternalReport er
    join er.scamAction sa
    join er.scamContext sc
    join er.country co
    left join er.state st
    left join er.city ci
    where er.id = :id
    """)
    Optional<ExternalReportDetailResponse> findReportDetailById(Long id);
}
