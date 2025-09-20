package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.Source;
import com.swkim.safetrip.entity.world.City;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.entity.world.State;
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
    @JoinColumn(name = "scam_action_id")
    private ScamAction scamAction;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scam_context_id")
    private ScamContext scamContext;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "content", nullable = false, length = 1000)
    protected String content;
}