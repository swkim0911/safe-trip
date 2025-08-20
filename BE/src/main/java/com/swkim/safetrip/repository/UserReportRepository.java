package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long>{

    @Query("""
    select new com.swkim.safetrip.dto.response.UserReportDetailResponse(
        u.nickname,
        ur.title,
        scam.name,
        c.name,
        st.name,
        ur.description,
        ur.createdAt
    )
    from UserReport ur
    join ur.user u
    join ur.scam scam
    join ur.country c
    join ur.state st
    where ur.id = :id
    """)
    Optional<UserReportDetailResponse> findReportDetailById(@Param("id") Long id); // todo test 코드 작성
}
