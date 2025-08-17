package com.swkim.safetrip.entity.enums;

import lombok.Getter;

@Getter
public enum SourceType {

    REDDIT("Reddit");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }

}
