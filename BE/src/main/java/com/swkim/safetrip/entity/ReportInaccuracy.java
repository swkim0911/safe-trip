package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.ReportInaccuracyReason;
import com.swkim.safetrip.entity.enums.ReportInaccuracyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_inaccuracy")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportInaccuracy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_report_id", nullable = false)
    private ExternalReport externalReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportInaccuracyReason reason;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ReportInaccuracyStatus status = ReportInaccuracyStatus.PENDING;

    @Column
    private LocalDateTime resolvedAt;

    public void resolve() {
        this.status = ReportInaccuracyStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }
}
