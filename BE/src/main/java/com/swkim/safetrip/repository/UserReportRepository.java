package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.User;
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
    select ur from UserReport ur
    join fetch ur.user
    join fetch ur.scamAction
    join fetch ur.scamContext
    join fetch ur.country
    left join fetch ur.state
    left join fetch ur.city
    where ur.id = :id and ur.deletedAt is null
    """)
    Optional<UserReport> findDetailById(@Param("id") Long id);

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

    @Query("select ur from UserReport ur left join fetch ur.images where ur.user = :user")
    List<UserReport> findAllByUserWithImages(@Param("user") User user);
}
