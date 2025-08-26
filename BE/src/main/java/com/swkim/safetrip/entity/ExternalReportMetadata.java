package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.StateMatchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "external_report_metadata",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"report_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalReportMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private ExternalReport report;

    @Column(name = "extracted_state")
    private String extractedState;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_match_status", nullable = false)
    private StateMatchStatus stateMatchStatus;
}
