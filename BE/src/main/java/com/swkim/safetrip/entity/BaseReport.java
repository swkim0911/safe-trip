package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.Setter;

@MappedSuperclass
public abstract class BaseReport extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scam_id")
    private Scam scam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "description", nullable = false, length = 1000)
    protected String description;
}