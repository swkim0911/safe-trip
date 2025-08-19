package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import com.swkim.safetrip.entity.Location;
import com.swkim.safetrip.entity.UserReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class})
class UserReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    @DisplayName("findReportWithLocationById 함수 지연로딩 확인")
    void lazy_loading_test() {
        //given
        Location location = Location.builder()
                .address("downtown")
                .lat("31.123")
                .lng("102.13925")
                .build();

        UserReport userReport = UserReport.builder()
                .title("title")
                .description("description")
                .build();

        UserReport saveUserReport = reportRepository.save(userReport);

        //when

        //then
//        boolean isProxy = mockingDetails(findUserReport.getLocation()).isMock(); // fetch join이면 location 객체는 proxy가 아니다.
//        Assertions.assertThat(isProxy).isFalse();
    }
}