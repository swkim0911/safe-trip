package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report  extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "likes", nullable = false)
    private Integer likeCnt;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "advice", nullable = false)
    private String advice;

    @Builder
    public Report(String category, User user, Location location, String title, String description, String advice) {
        this.category = Category.valueOf(category);
        this.user = user;
        this.location = location;
        this.title = title;
        this.likeCnt = 0;
        this.description = description;
        this.advice = advice;
    }

    // 양방향 연관관계 편의 메서드
    public void addImage(Image image){
        images.add(image);
        image.setReport(this);
    }
}
