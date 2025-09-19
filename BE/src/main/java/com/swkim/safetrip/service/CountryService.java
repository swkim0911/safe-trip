package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.global.exception.custom.CountryNotFoundException;
import com.swkim.safetrip.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.swkim.safetrip.dto.response.world.CountriesResponse.*;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public Country findCountryById(Long id) {
        return countryRepository.findById(id).orElseThrow(CountryNotFoundException::new);
    }

    public CountriesResponse getAllCountries() {
        List<CountryDto> countries = countryRepository.findAll().stream()
                .map(c -> new CountryDto(c.getId(), c.getName()))
                .toList();

        return new CountriesResponse(countries);
    }

}
