package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.LocationScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.global.exception.custom.CountryNotFoundException;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.swkim.safetrip.dto.response.world.CountriesResponse.*;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final ReportNativeRepository reportNativeRepository;

    public Country findCountryById(Long id) {
        return countryRepository.findById(id).orElseThrow(CountryNotFoundException::new);
    }

    public CountriesResponse getAllCountries() {
        List<CountryDto> countries = countryRepository.findAll().stream()
                .map(c -> new CountryDto(c.getId(), c.getName()))
                .toList();

        return new CountriesResponse(countries);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamStatisticsItem> getCountryStatistics(Pageable pageable) {
        return reportNativeRepository.findCountryStatisticsSlice(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByCountry(Long countryId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByCountryId(countryId, pageable);
    }

}
