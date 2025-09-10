package com.swkim.safetrip.entity.world;

import com.swkim.safetrip.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "state")
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

    @Column(name = "name_ko")
    private String koreanName;

    @Column(name = "lat")
    private Double lat; //consider: state에는 lat, lng 정보가 없을 수도 있는데, 그럴 때는 어떻게 할지 정하기(ex: 안도라) -> 아마 그냥 null저장하고, summary 조회할 때는 생략

    @Column(name = "lng")
    private Double lng;

    @Builder
    public State(Long datasetId, String name, String koreanName, Double lat, Double lng) {
        this.datasetId = datasetId;
        this.name = name;
        this.koreanName = koreanName;
        this.lat = lat;
        this.lng = lng;
    }

    public void setCountry(Country country) {
        this.country = country;
        country.getStates().add(this);
    }
}
