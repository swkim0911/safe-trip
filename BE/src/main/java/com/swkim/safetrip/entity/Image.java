package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Setter
    @Column(name = "access_url", nullable = false)
    private String accessURL;

    @Builder
    public Image(String originalName) {
        this.originalName = originalName;
        this.storedName = getStoredName(originalName);
        this.accessURL = "";
    }

    private String getStoredName(String originalName) {
        return UUID.randomUUID() + extractExtension(originalName);
    }

    private String extractExtension(String originalName) {
        int lastIndex = originalName.lastIndexOf(".");
        return originalName.substring(lastIndex);
    }

}
