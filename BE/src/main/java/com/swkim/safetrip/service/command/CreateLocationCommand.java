package com.swkim.safetrip.service.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CreateLocationCommand { // todo 삭제

    private final String countryName;
    private final String cityName;
    private final String cityLat;
    private final String cityLng;
}
