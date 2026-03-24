package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long>{

    @Query("""
    select new com.swkim.safetrip.dto.response.UserReportDetailResponse(
        ur.source,
        u.nickname,
        sa.name,
        sc.name,
        co.name,
        st.name,
        ci.name,
        ur.title,
        ur.content,
        ur.createdAt
    )
    from UserReport ur
    join ur.user u
    join ur.scamAction sa
    join ur.scamContext sc
    join ur.country co
    left join ur.state st
    left join ur.city ci
    where ur.id = :id and ur.deletedAt is null
    """)
    Optional<UserReportDetailResponse> findReportDetailById(@Param("id") Long id);

    @Query("""
    select ur from UserReport ur
    join fetch ur.scamAction
    join fetch ur.scamContext
    join fetch ur.country
    left join fetch ur.state
    left join fetch ur.city
    left join fetch ur.images
    where ur.user.email = :email and ur.deletedAt is null
    order by ur.createdAt desc
    """)
    List<UserReport> findMyReports(@Param("email") String email);
}
