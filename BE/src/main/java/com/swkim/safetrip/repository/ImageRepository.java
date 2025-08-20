package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.userReport.id = :reportId")
    List<Image> findImagesByReportId(@Param("reportId") Long reportId);
}
