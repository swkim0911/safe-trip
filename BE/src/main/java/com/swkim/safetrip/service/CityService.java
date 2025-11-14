package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.CitiesResponse;
import com.swkim.safetrip.dto.response.world.CitiesResponse.CityDto;
import com.swkim.safetrip.dto.response.world.StatesResponse;
import com.swkim.safetrip.entity.world.City;
import com.swkim.safetrip.global.exception.custom.CityNotFoundException;
import com.swkim.safetrip.repository.CityRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final ReportNativeRepository reportNativeRepository;

    public City findCityByIdWithState(Long cityId) {
        return cityRepository.findByIdWithState(cityId).orElseThrow(CityNotFoundException::new);
    }

    public CitiesResponse getCities(Long stateId) {
        List<CityDto> cities = cityRepository.findByStateId(stateId).stream()
                .map(s -> new CityDto(s.getId(), s.getName()))
                .toList();

        return new CitiesResponse(cities);
    }

    @Transactional(readOnly = true)
    public Slice<RegionScamStatisticsItem> getCityStatistics(Long stateId, Pageable pageable) {
        return reportNativeRepository.findCityStatisticsSliceByStateId(stateId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByCity(Long cityId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByCityId(cityId, pageable);
    }
}
