package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@Table(name = "report")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report  extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private String category;

    private String url;

    private String description;

    private String advice;

    private String location;
}
