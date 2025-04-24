package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import com.swkim.safetrip.entity.Location;
import com.swkim.safetrip.entity.Report;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mockingDetails;

@DataJpaTest
@Import({QuerydslConfig.class})
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    @DisplayName("findReportWithLocationById 함수 지연로딩 확인")
    void lazy_loading_test() {
        //given
        Location location = Location.builder()
                .latitude("31.123")
                .longitude("102.13925")
                .build();

        Report report = Report.builder()
                .title("title")
                .description("description")
                .advice("advice")
                .build();

        report.setLocation(location);
        Report saveReport = reportRepository.save(report);

        //when
        Report findReport = reportRepository.findReportWithLocationById(saveReport.getId()).get();

        //then
        boolean isProxy = mockingDetails(findReport.getLocation()).isMock(); // fetch join이면 location 객체는 proxy가 아니다.
        Assertions.assertThat(isProxy).isFalse();
    }
}