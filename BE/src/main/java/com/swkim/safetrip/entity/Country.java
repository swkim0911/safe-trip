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
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    private Long externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_ko", nullable = false)
    private String koreanName;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향
    private List<City> cities = new ArrayList<>();

    // 양방향 연관관계 편의 메서드
    public void addCity(City city){
        cities.add(city);
        city.setCountry(this);
    }
}
