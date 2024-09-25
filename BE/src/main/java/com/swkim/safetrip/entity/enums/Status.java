package com.swkim.safetrip.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACTIVE("active"), CANCELED("canceled");

    private final String name;
}
