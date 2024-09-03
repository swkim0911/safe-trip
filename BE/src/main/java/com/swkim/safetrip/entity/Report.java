package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "report")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report  extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private String category;

    private String location;

    private String url;

    private String description;

    private String advice;

    @Builder
    public Report(String title, String category, String url, String description, String advice, String location) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.url = url;
        this.description = description;
        this.advice = advice;
    }
}
