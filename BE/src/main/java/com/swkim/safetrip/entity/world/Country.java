package com.swkim.safetrip.entity.world;

import com.swkim.safetrip.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "country")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", unique = true, nullable = false)
    private Long datasetId;

    @Column(name = "iso2", length = 2, unique = true, nullable = false)
    private String iso2;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ko")
    private String nameKo;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향
    private List<State> states = new ArrayList<>();

    @Builder
    public Country(Long datasetId, String name, String nameKo, Double lat, Double lng) {
        this.datasetId = datasetId;
        this.name = name;
        this.nameKo = nameKo;
        this.lat = lat;
        this.lng = lng;
    }
}
