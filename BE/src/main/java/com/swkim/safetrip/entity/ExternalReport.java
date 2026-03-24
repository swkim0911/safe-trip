package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalReport extends BaseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String externalId;

    @Column(name = "author")
    private String author;

    @Column(length = 500, nullable = false)
    private String sourceUrl;

    @Column(updatable = false)
    private LocalDateTime postedAt;

    @Column(updatable = false)
    private LocalDateTime collectedAt;
}
