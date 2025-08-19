package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@MappedSuperclass
public abstract class BaseReport extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    protected Source source;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scam_id")
    private Scam scam;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private State state;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "description", nullable = false, length = 1000)
    protected String description;
}