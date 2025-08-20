package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class})
class UserReportRepositoryTest {

}