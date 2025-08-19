package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.LocationScamSummaryItem;
import com.swkim.safetrip.dto.response.LocationScamSummaryResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.StateCountryMismatchException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.swkim.safetrip.dto.response.LocationScamSummaryResponse.LocationType.COUNTRY;
import static com.swkim.safetrip.dto.response.LocationScamSummaryResponse.LocationType.STATE;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserService userService;
    private final ScamService scamService;
    private final ImageService imageService;
    private final StateService stateService;
    private final CountryService countryService;

    private final ReportRepository reportRepository;
    private final ReportJdbcRepository reportJdbcRepository;

    public Long saveReport(String email, ReportSaveRequest reportSaveRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        UserReport userReport = ReportMapper.toReport(reportSaveRequest);

        // 2. User 객체 report에 추가
        User findUser = userService.findUserByEmail(email).orElseThrow(UserNotFoundException::new);
        userReport.setUser(findUser);

        // 3. scam 객체 report에 추가
        Scam findScam = scamService.findScamById(reportSaveRequest.getScamId());
        userReport.setScam(findScam);

        // CONSIDER: 이미지 업로드 방식 개선 (pre-signed URL 도입 검토)
        // 4. 이미지 S3에 전송하고 report에 추가
        List<Image> savedImageList = imageService.saveImagesInS3Bucket(files);
        savedImageList.forEach(userReport::addImage);

        // 5. state 객체, country 객체 set
        Country findCountry = countryService.findCountryById(reportSaveRequest.getCountryId());
        State findState = stateService.findStateByIdWithCountry(reportSaveRequest.getStateId());

        if (!isStateOfCountry(findState, findCountry)) {
            throw new StateCountryMismatchException();
        }
        userReport.setCountry(findCountry);
        userReport.setState(findState);

        // 7. report 저장
        return save(userReport);
    }

    @Transactional(readOnly = true)
    public LocationScamSummaryResponse getCountrySummary(){
        List<LocationScamSummaryItem> countrySummariesItems = reportJdbcRepository.findCountrySummaries();
        return new LocationScamSummaryResponse(COUNTRY, countrySummariesItems);
    }

    @Transactional(readOnly = true)
    public LocationScamSummaryResponse getStateSummary(){
        List<LocationScamSummaryItem> stateSummariesItems = reportJdbcRepository.findStateSummaries();
        return new LocationScamSummaryResponse(STATE, stateSummariesItems);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamSummaryItem> getCountrySummaryPage(Pageable pageable) {
        return reportRepository.findCountrySummarySlice(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<LocationScamSummaryItem> getCitySummaryPage(Long countryId, Pageable pageable) {
        return reportRepository.findCitySummarySlice(countryId, pageable);
    }

    @Transactional
    public Slice<ReportSummaryItem> getReportSummaryPage(Long countryId, Long cityId, Pageable pageable) {
        return reportRepository.findReportSummarySliceByCountryAndCity(countryId, cityId, pageable);
    }

    @Transactional
    public ReportFindByIdResponse getReport(Long id){

        UserReport userReport = reportRepository.findReportWithLocationById(id).orElseThrow(ReportNotFoundException::new);
        List<Image> images = imageService.findImagesByReportId(id);
        List<String> URLs = images.stream()
                .map(Image::getAccessURL)
                .toList();

        return ReportMapper.toReportFindByIdResponse(userReport, URLs);
    }

    @Transactional
    private Long save(UserReport userReport) {
        UserReport savedUserReport = reportRepository.save(userReport);
        return savedUserReport.getId();
    }

    private boolean isStateOfCountry(State findState, Country findCountry) {
        return Objects.equals(findState.getCountry().getId(), findCountry.getId());
    }
}
