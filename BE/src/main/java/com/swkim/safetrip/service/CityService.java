package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.world.City;
import com.swkim.safetrip.global.exception.custom.CityNotFoundException;
import com.swkim.safetrip.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public City findCityByIdWithState(Long cityId) {
        return cityRepository.findByIdWithState(cityId).orElseThrow(CityNotFoundException::new);
    }
}
