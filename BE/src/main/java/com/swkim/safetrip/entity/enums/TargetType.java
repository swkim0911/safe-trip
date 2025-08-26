package com.swkim.safetrip.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    REPORT("report"), COMMENT("comment");

    private final String name;
}
