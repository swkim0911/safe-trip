package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "state",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "country_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dataset_id", unique = true, nullable = false)
    private Long datasetId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id") // 양방향
    private Country country;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ko", nullable = false)
    private String koreanName;

    @Column(name = "lat", nullable = false)
    private Double lat; //consider: state에는 lat, lng 정보가 없을 수도 있는데, 그럴 때는 어떻게 할지 정하기(ex: 안도라) -> 아마 그냥 null저장하고, summary 조회할 때는 생략

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Builder
    public State(String name, String lat, String lng) {
        this.name = name;
        this.lat = Double.parseDouble(lat);
        this.lng = Double.parseDouble(lng);
    }
}
