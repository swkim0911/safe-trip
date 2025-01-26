package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import com.swkim.safetrip.entity.Report;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class})
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    public void before() {
        Report report = Report.builder()
                .title("title")
                .category("THEFT")
                .description("description")
                .advice("advice")
                .build();

        Report saveReport = reportRepository.save(report);
    }
    @Test
    @DisplayName("QueryDsl 정상 동작 확인")
    public void QueryDsl_Basic_Test() {

    }
}