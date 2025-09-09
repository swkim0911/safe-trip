package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.global.exception.custom.CountryNotFoundException;
import com.swkim.safetrip.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public Country findCountryById(Long id) {
        return countryRepository.findById(id).orElseThrow(CountryNotFoundException::new);
    }
}
