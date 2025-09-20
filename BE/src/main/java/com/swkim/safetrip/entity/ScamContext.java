package com.swkim.safetrip.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scam_context")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScamContext extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public ScamContext(String name) {
        this.name = name;
    }
}
