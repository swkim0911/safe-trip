package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReport extends BaseReport{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Builder
    public UserReport(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // 양방향 연관관계 편의 메서드
    public void addImage(Image image){
        images.add(image);
        image.setUserReport(this);
    }
}
