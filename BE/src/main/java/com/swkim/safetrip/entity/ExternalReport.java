package com.swkim.safetrip.entity;

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

    @Column(length = 500, nullable = false)
    private String link;

    @Column(updatable = false)
    private LocalDateTime originalCreatedAt;

    @Column(updatable = false)
    private LocalDateTime collectedAt;
}
