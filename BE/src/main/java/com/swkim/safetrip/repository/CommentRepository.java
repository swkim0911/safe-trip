package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select c from Comment c
        left join fetch c.user
        where c.userReport.id = :reportId and c.depth = 0
        order by c.createdAt asc
    """)
    List<Comment> findTopLevelByUserReportId(@Param("reportId") Long reportId);

    @Query("""
        select c from Comment c
        left join fetch c.user
        where c.externalReport.id = :reportId and c.depth = 0
        order by c.createdAt asc
    """)
    List<Comment> findTopLevelByExternalReportId(@Param("reportId") Long reportId);

    @Query("""
        select c from Comment c
        left join fetch c.user
        where c.parentComment.id in :parentIds
        order by c.createdAt asc
    """)
    List<Comment> findRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCnt = c.likeCnt + 1 WHERE c.id = :id")
    void incrementLikeCnt(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCnt = c.likeCnt - 1 WHERE c.id = :id AND c.likeCnt > 0")
    void decrementLikeCnt(@Param("id") Long id);
}
