package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.Source;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @OneToMany(mappedBy = "userReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public UserReport(Source source, String title, String description) {
        this.source = source;
        this.title = title;
        this.content = description;
    }

    // 양방향 연관관계 편의 메서드
    public void addImage(Image image){
        images.add(image);
        image.setUserReport(this);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
