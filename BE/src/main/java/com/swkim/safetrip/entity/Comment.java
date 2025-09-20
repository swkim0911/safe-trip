package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // todo baseReport를 어떻게 구현하느냐에 따라 달라짐
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "report_id")
//    private BaseReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "likes", nullable = false)
    private Integer likeCnt = 0;

    @Column(name = "depth", nullable = false)
    private Integer depth; // 댓글 깊이

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at", updatable = false, nullable = true)
    private LocalDateTime deletedAt;
}
