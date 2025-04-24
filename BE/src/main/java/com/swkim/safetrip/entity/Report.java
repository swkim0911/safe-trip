package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private Category category;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "advice", nullable = false)
    private String advice;

    @Builder
    public Report(String title, String category, User user, Location location, String description, String advice) {
        this.user = user;
        this.location = location;
        this.title = title;
        this.description = description;
        this.advice = advice;
    }

    // 양방향 연관관계 편의 메서드
    public void addImage(Image image){
        images.add(image);
        image.setReport(this);
    }
}
