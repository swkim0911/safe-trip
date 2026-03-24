package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.ReportInaccuracyRequest;
import com.swkim.safetrip.dto.response.ReportInaccuracyItem;
import com.swkim.safetrip.entity.ExternalReport;
import com.swkim.safetrip.entity.ReportInaccuracy;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.global.exception.custom.DuplicateReportInaccuracyException;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.repository.ExternalReportRepository;
import com.swkim.safetrip.repository.ReportInaccuracyRepository;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportInaccuracyService {

    private final ReportInaccuracyRepository reportInaccuracyRepository;
    private final ExternalReportRepository externalReportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submit(Long externalReportId, String email, ReportInaccuracyRequest request) {
        ExternalReport externalReport = externalReportRepository.findById(externalReportId)
                .orElseThrow(ReportNotFoundException::new);
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (reportInaccuracyRepository.existsByExternalReportAndUser(externalReport, user)) {
            throw new DuplicateReportInaccuracyException();
        }

        ReportInaccuracy inaccuracy = ReportInaccuracy.builder()
                .externalReport(externalReport)
                .user(user)
                .reason(request.reason())
                .description(request.description())
                .build();

        reportInaccuracyRepository.save(inaccuracy);
    }

    @Transactional(readOnly = true)
    public boolean hasSubmitted(Long externalReportId, String email) {
        ExternalReport externalReport = externalReportRepository.findById(externalReportId)
                .orElseThrow(ReportNotFoundException::new);
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return reportInaccuracyRepository.existsByExternalReportAndUser(externalReport, user);
    }

    @Transactional(readOnly = true)
    public List<ReportInaccuracyItem> getInaccuracyReports() {
        return reportInaccuracyRepository.findAllWithDetails().stream()
                .map(ri -> new ReportInaccuracyItem(
                        ri.getId(),
                        ri.getExternalReport().getId(),
                        ri.getExternalReport().getTitle(),
                        ri.getUser().getNickname(),
                        ri.getReason().name(),
                        ri.getDescription(),
                        ri.getCreatedAt()
                ))
                .toList();
    }
}
