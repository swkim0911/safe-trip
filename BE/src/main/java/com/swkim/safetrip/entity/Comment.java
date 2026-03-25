package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "comment",
    uniqueConstraints = {},
    indexes = {}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // 탈퇴한 사용자 대비 nullable
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_report_id", nullable = true)
    private UserReport userReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_report_id", nullable = true)
    private ExternalReport externalReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "likes", nullable = false)
    private Integer likeCnt = 0;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Comment create(UserReport userReport, User user, String content) {
        Comment comment = new Comment();
        comment.userReport = userReport;
        comment.user = user;
        comment.content = content;
        comment.depth = 0;
        comment.likeCnt = 0;
        comment.isDeleted = false;
        return comment;
    }

    public static Comment createReply(UserReport userReport, User user, String content, Comment parent) {
        Comment comment = new Comment();
        comment.userReport = userReport;
        comment.user = user;
        comment.content = content;
        comment.parentComment = parent;
        comment.depth = 1;
        comment.likeCnt = 0;
        comment.isDeleted = false;
        return comment;
    }

    public static Comment createForExternal(ExternalReport externalReport, User user, String content) {
        Comment comment = new Comment();
        comment.externalReport = externalReport;
        comment.user = user;
        comment.content = content;
        comment.depth = 0;
        comment.likeCnt = 0;
        comment.isDeleted = false;
        return comment;
    }

    public static Comment createReplyForExternal(ExternalReport externalReport, User user, String content, Comment parent) {
        Comment comment = new Comment();
        comment.externalReport = externalReport;
        comment.user = user;
        comment.content = content;
        comment.parentComment = parent;
        comment.depth = 1;
        comment.likeCnt = 0;
        comment.isDeleted = false;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementLikeCnt() {
        this.likeCnt++;
    }

    public void decrementLikeCnt() {
        if (this.likeCnt > 0) this.likeCnt--;
    }
}
