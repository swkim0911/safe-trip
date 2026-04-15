package com.swkim.safetrip.service;

import com.swkim.safetrip.repository.ReportJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsBatchService {

    private final ReportJdbcRepository reportJdbcRepository;

    // 매일 새벽 3시 실제 COUNT로 user_country_stats 정합성 보정
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void reconcileUserCountryStats() {
        log.info("user_country_stats reconciliation started");
        reportJdbcRepository.reconcileUserCountryStats();
        log.info("user_country_stats reconciliation completed");
    }

}
