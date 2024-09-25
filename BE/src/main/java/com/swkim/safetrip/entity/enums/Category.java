package com.swkim.safetrip.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    MONEY_LOST("소매치기"),THEFT("강도");

    private final String name;
}
