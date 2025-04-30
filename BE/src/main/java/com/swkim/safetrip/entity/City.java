package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "city",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "country_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id") // 양방향
    private Country country;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Builder
    public City(String name, String lat, String lng) {
        this.name = name;
        this.lat = Double.parseDouble(lat);
        this.lng = Double.parseDouble(lng);
    }
}
