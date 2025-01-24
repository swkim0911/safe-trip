package com.swkim.safetrip.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CountryCityData {

    private final String countryName;
    private final String cityName;
    private final String cityLatitude;
    private final String cityLongitude;
}
