package com.swkim.safetrip.repository;

import com.swkim.safetrip.dto.response.MyReportItem;
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
    join ur.state st
    join ur.city ci
    where ur.id = :id
    """)
    Optional<UserReportDetailResponse> findReportDetailById(@Param("id") Long id);

    @Query("""
    select new com.swkim.safetrip.dto.response.MyReportItem(
        ur.id,
        ur.title,
        sa.id,
        sa.name,
        sc.id,
        sc.name,
        co.id,
        co.name,
        st.id,
        st.name,
        ci.id,
        ci.name,
        ur.content,
        ur.createdAt
    )
    from UserReport ur
    join ur.scamAction sa
    join ur.scamContext sc
    join ur.country co
    left join ur.state st
    left join ur.city ci
    where ur.user.email = :email
    order by ur.createdAt desc
    """)
    List<MyReportItem> findMyReports(@Param("email") String email);
}
