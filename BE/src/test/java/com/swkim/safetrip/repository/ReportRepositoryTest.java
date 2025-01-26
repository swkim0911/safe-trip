package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import com.swkim.safetrip.entity.Report;
import org.assertj.core.api.Assertions;
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

    @Test
    @DisplayName("QueryDsl 정상 동작 확인")
    public void QueryDsl_Basic_Test() {

        Report report = Report.builder().title("title").category("THEFT").description("description").advice("advice").build();

        Report saveReport = reportRepository.save(report);

        Report findReport = reportRepository.findByTitle(saveReport.getTitle());

        Assertions.assertThat(report.getCategory()).isEqualTo(findReport.getCategory());
    }
}