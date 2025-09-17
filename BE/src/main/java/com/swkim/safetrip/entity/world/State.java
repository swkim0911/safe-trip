package com.swkim.safetrip.entity.world;

import com.swkim.safetrip.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class State extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dataset_id", unique = true, nullable = false)
    private Long datasetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id") // 양방향
    private Country country;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Builder
    public State(Long datasetId, String name, Double lat, Double lng) {
        this.datasetId = datasetId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public void setCountry(Country country) {
        this.country = country;
        country.getStates().add(this);
    }
}
