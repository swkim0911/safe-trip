package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.City;
import com.swkim.safetrip.entity.Country;
import com.swkim.safetrip.entity.Location;
import com.swkim.safetrip.repository.CityRepository;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.vo.CountryCityData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @Transactional
    public Location createLocationWithCityAndCountry(CountryCityData countryCityData, String locationAddress, String locationLat, String locationLng) {

        String countryName = countryCityData.getCountryName();
        String cityName = countryCityData.getCityName();

        Optional<Country> findCountry = countryRepository.findByName(countryName);

        Country country;
        City city;

        if (findCountry.isEmpty()) { // country가 없는 경우. country에 있는 city도 당연히 없으니 두 객체 모두 생성
            country = Country.builder()
                    .name(countryName)
                    .build();

            city = City.builder()
                    .name(countryCityData.getCityName())
                    .lat(countryCityData.getCityLat())
                    .lng(countryCityData.getCityLng())
                    .build();

            country.addCity(city);
            countryRepository.save(country);
        } else {
            country = findCountry.get();
            Optional<City> findCity = cityRepository.findByNameAndCountryId(cityName, country.getId());

            if (findCity.isPresent()) { // country도 있고 city도 있는 경우
                city = findCity.get();
            } else {
                city = City.builder()
                        .name(countryCityData.getCityName())
                        .lat(countryCityData.getCityLat())
                        .lng(countryCityData.getCityLng())
                        .build();

                country.addCity(city);
                cityRepository.save(city);
            }
        }

        return Location.builder()
                .country(country)
                .city(city)
                .address(locationAddress)
                .lat(locationLat)
                .lng(locationLng)
                .build();
    }
}
