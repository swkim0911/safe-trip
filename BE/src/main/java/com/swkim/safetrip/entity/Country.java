package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "country", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", unique = true, nullable = false)
    private Long datasetId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ko", nullable = false)
    private String koreanName;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향
    private List<State> states = new ArrayList<>();
}
