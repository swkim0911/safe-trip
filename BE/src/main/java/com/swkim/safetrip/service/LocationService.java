package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.Country;
import com.swkim.safetrip.entity.Location;
import com.swkim.safetrip.entity.State;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.repository.StateRepository;
import com.swkim.safetrip.service.command.CreateLocationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;

    @Transactional
    public Location createLocationWithCityAndCountry(CreateLocationCommand createLocationCommand, String locationAddress, String locationLat, String locationLng) {

        String countryName = createLocationCommand.getCountryName();
        String cityName = createLocationCommand.getCityName();

        Optional<Country> findCountry = countryRepository.findByName(countryName);

        Country country;
        State state;

        if (findCountry.isEmpty()) { // country가 없는 경우. country에 있는 city도 당연히 없으니 두 객체 모두 생성
            country = Country.builder()
                    .name(countryName)
                    .build();

            state = State.builder()
                    .name(createLocationCommand.getCityName())
                    .lat(createLocationCommand.getCityLat())
                    .lng(createLocationCommand.getCityLng())
                    .build();

            country.addCity(state);
            countryRepository.save(country);
        } else {
            country = findCountry.get();
            Optional<State> findCity = stateRepository.findByNameAndCountryId(cityName, country.getId());

            if (findCity.isPresent()) { // country도 있고 city도 있는 경우
                state = findCity.get();
            } else {
                state = State.builder()
                        .name(createLocationCommand.getCityName())
                        .lat(createLocationCommand.getCityLat())
                        .lng(createLocationCommand.getCityLng())
                        .build();

                country.addCity(state);
                stateRepository.save(state);
            }
        }

        return Location.builder()
                .country(country)
                .city(state)
                .address(locationAddress)
                .lat(locationLat)
                .lng(locationLng)
                .build();
    }
}
