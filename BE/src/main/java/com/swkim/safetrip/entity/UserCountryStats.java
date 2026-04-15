package com.swkim.safetrip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_country_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCountryStats {

    @Id
    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "scam_cnt", nullable = false)
    private Long scamCnt;

}
