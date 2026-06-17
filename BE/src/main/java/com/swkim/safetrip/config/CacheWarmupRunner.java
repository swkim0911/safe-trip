package com.swkim.safetrip.config;

import com.swkim.safetrip.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class CacheWarmupRunner implements ApplicationRunner {

    private final ReportService reportService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("캐시 warm-up 시작");
            reportService.getCountryStatistics();
            reportService.getStateStatistics();
            reportService.getCityStatistics();
            reportService.getScamActionStats();
            reportService.getScamContextStats();
            log.info("캐시 warm-up 완료");
        } catch (Exception e) {
            log.warn("캐시 warm-up 실패 - 서버는 정상 기동됩니다: {}", e.getMessage());
        }
    }
}
