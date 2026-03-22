package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.entity.enums.RiskLevel;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.global.exception.custom.CountryNotFoundException;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.swkim.safetrip.dto.response.world.CountriesResponse.*;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final ReportNativeRepository reportNativeRepository;
    private final ReportJdbcRepository reportJdbcRepository;
    private final RiskLevelService riskLevelService;

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
    public Slice<RegionScamStatisticsItem> getCountryStatistics(Pageable pageable) {
        Slice<RegionScamStatisticsItem> slice = reportNativeRepository.findCountryStatisticsSlice(pageable);
        Map<Long, RiskLevel> riskLevels = riskLevelService.getCountryRiskLevels();
        List<RegionScamStatisticsItem> content = slice.getContent().stream()
                .map(item -> RegionScamStatisticsItem.builder()
                        .id(item.id()).name(item.name()).lat(item.lat()).lng(item.lng())
                        .scamCnt(item.scamCnt()).iso2(item.iso2())
                        .riskLevel(riskLevels.get(item.id()))
                        .build())
                .toList();
        return new SliceImpl<>(content, pageable, slice.hasNext());
    }

    public RegionScamStatisticsItem getCountryInfo(Long countryId) {
        return reportJdbcRepository.findCountryInfo(countryId);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByCountry(Long countryId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByCountryId(countryId, pageable);
    }

}
