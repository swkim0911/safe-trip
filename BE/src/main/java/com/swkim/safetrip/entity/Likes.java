package com.swkim.safetrip.entity;

import com.swkim.safetrip.entity.enums.Status;
import com.swkim.safetrip.entity.enums.TargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    private Status status;
}
