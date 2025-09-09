package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class City extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dataset_id", unique = true, nullable = false)
    private Long datasetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ko")
    private String koreanName;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;
}