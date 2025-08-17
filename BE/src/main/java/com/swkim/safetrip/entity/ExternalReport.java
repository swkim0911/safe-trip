package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.SourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "external_report",
        uniqueConstraints = @UniqueConstraint(columnNames = {"externalId", "source"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalReport extends BaseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private SourceType source;

    @Column(length = 500, nullable = false)
    private String link;

    private LocalDateTime originalCreatedAt;

    private LocalDateTime collectedAt;
}
