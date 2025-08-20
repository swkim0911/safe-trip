package com.swkim.safetrip.entity.enums;

import lombok.Getter;

@Getter
public enum Source {

    SAFETRIP("User Report"),REDDIT("External");

    private final String label;

    Source(String label) {
        this.label = label;
    }

}
