package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.RegionScamStatisticsItem;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.dto.response.world.CountriesResponse;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.global.exception.custom.CountryNotFoundException;
import com.swkim.safetrip.global.exception.custom.InvalidSortKeyException;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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
        List<RegionScamStatisticsItem> all = reportJdbcRepository.findCountryStatistics();
        List<RegionScamStatisticsItem> withRisk = riskLevelService.assignRiskLevels(all);

        List<RegionScamStatisticsItem> sorted = withRisk.stream()
                .sorted(buildComparator(pageable.getSort()))
                .toList();

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        int end = Math.min(offset + pageSize, sorted.size());
        List<RegionScamStatisticsItem> content = offset >= sorted.size() ? List.of() : sorted.subList(offset, end);

        return new SliceImpl<>(content, pageable, end < sorted.size());
    }

    private Comparator<RegionScamStatisticsItem> buildComparator(Sort sort) {
        if (!sort.iterator().hasNext()) {
            return Comparator.comparingLong(RegionScamStatisticsItem::scamCnt).reversed()
                    .thenComparingLong(RegionScamStatisticsItem::id);
        }
        Comparator<RegionScamStatisticsItem> result = null;
        for (Sort.Order order : sort) {
            Comparator<RegionScamStatisticsItem> c = switch (order.getProperty()) {
                case "scamCnt" -> Comparator.comparingLong(RegionScamStatisticsItem::scamCnt);
                case "name", "countryName" -> Comparator.comparing(RegionScamStatisticsItem::name);
                default -> throw new InvalidSortKeyException();
            };
            if (order.isDescending()) c = c.reversed();
            result = result == null ? c : result.thenComparing(c);
        }
        return result.thenComparingLong(RegionScamStatisticsItem::id);
    }

    public RegionScamStatisticsItem getCountryInfo(Long countryId) {
        return reportJdbcRepository.findCountryInfo(countryId);
    }

    @Transactional(readOnly = true)
    public Slice<ReportSummaryItem> getReportsByCountry(Long countryId, Pageable pageable) {
        return reportNativeRepository.findReportSummarySliceByCountryId(countryId, pageable);
    }

}
