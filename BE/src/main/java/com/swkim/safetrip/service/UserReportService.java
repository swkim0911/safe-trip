package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserReportSaveRequest;
import com.swkim.safetrip.dto.request.UserReportUpdateRequest;
import com.swkim.safetrip.dto.response.MyReportItem;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.entity.world.City;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.entity.world.State;
import com.swkim.safetrip.global.exception.custom.CityStateMismatchException;
import com.swkim.safetrip.global.exception.custom.ForbiddenReportAccessException;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.StateCountryMismatchException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.UserCountryStatsRepository;
import com.swkim.safetrip.repository.UserReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportService {

    private final UserService userService;
    private final ScamService scamService;
    private final ImageService imageService;
    private final StateService stateService;
    private final CountryService countryService;
    private final CityService cityService;

    private final UserReportRepository userReportRepository;
    private final UserCountryStatsRepository userCountryStatsRepository;

    @Caching(evict = {
            @CacheEvict(cacheNames = "map-countries", allEntries = true),
            @CacheEvict(cacheNames = "map-states", allEntries = true),
            @CacheEvict(cacheNames = "map-cities", allEntries = true),
            @CacheEvict(cacheNames = "scam-action-stats", allEntries = true),
            @CacheEvict(cacheNames = "scam-context-stats", allEntries = true)
    })
    @Transactional
    public Long saveUserReport(String email, UserReportSaveRequest userReportSaveRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        UserReport userReport = ReportMapper.toReport(userReportSaveRequest);

        // 2. User 객체 report에 추가
        User findUser = userService.findUserByEmail(email).orElseThrow(UserNotFoundException::new);
        userReport.setUser(findUser);

        // 3.1 scamAction 객체 report에 추가
        ScamAction findScamAction = scamService.findScamActionById(userReportSaveRequest.scamActionId());
        userReport.setScamAction(findScamAction);

        // 3.1 scamAction 객체 report에 추가
        ScamContext findSCamContext = scamService.findScamContextById(userReportSaveRequest.scamContextId());
        userReport.setScamContext(findSCamContext);

        // CONSIDER: 이미지 업로드 방식 개선 (pre-signed URL 도입 검토)
        // 4. 이미지 S3에 전송하고 report에 추가
        List<Image> savedImageList = imageService.saveImagesInS3Bucket(files);
        savedImageList.forEach(userReport::addImage);

        // 5. country, state, city 데이터 정합성 확인 후 set
        Country findCountry = countryService.findCountryById(userReportSaveRequest.countryId());

        State findState = Optional.ofNullable(userReportSaveRequest.stateId())
                .map(stateService::findStateByIdWithCountry)
                .orElse(null);

        if (findState != null && !isStateOfCountry(findState, findCountry)) {
            throw new StateCountryMismatchException();
        }

        City findCity = Optional.ofNullable(userReportSaveRequest.cityId())
                .map(cityService::findCityByIdWithState)
                .orElse(null);

        if (findCity != null && findState != null && !isCityOfState(findCity, findState)) {
            throw new CityStateMismatchException();
        }
        userReport.setCountry(findCountry);
        userReport.setState(findState);
        userReport.setCity(findCity);

        // 6. report 저장 후 집계 테이블 갱신
        UserReport savedUserReport = userReportRepository.save(userReport);
        userCountryStatsRepository.increment(findCountry.getId());

        log.info("User report created: id={}, user={}", savedUserReport.getId(), email);
        return savedUserReport.getId();
    }

    @Transactional(readOnly = true)
    public UserReportDetailResponse getUserReport(Long id){
        UserReport ur = userReportRepository.findDetailById(id).orElseThrow(ReportNotFoundException::new);

        UserReportDetailResponse response = UserReportDetailResponse.builder()
                .source(ur.getSource())
                .nickname(ur.getUser().getNickname())
                .scamAction(ur.getScamAction().getName())
                .scamContext(ur.getScamContext().getName())
                .countryName(ur.getCountry().getName())
                .stateName(ur.getState() != null ? ur.getState().getName() : null)
                .cityName(ur.getCity() != null ? ur.getCity().getName() : null)
                .title(ur.getTitle())
                .description(ur.getContent())
                .createdAt(ur.getCreatedAt())
                .build();

        response.setUrls(getImageUrlsById(id));
        return response;
    }

    private List<String> getImageUrlsById(Long id) {
        List<Image> images = imageService.findImagesByReportId(id);
        return images.stream()
                .map(imageService::createPresignedImageUrl)
                .toList();
    }

    private boolean isStateOfCountry(State findState, Country findCountry) {
        return Objects.equals(findState.getCountry().getId(), findCountry.getId());
    }

    private boolean isCityOfState(City findCity, State findState) {
        return Objects.equals(findCity.getState().getId(), findState.getId());
    }

    @Transactional(readOnly = true)
    public List<MyReportItem> getMyReports(String email) {
        return userReportRepository.findMyReports(email).stream()
                .map(ur -> new MyReportItem(
                        ur.getId(),
                        ur.getTitle(),
                        ur.getScamAction().getId(),
                        ur.getScamAction().getName(),
                        ur.getScamContext().getId(),
                        ur.getScamContext().getName(),
                        ur.getCountry().getId(),
                        ur.getCountry().getName(),
                        ur.getState() != null ? ur.getState().getId() : null,
                        ur.getState() != null ? ur.getState().getName() : null,
                        ur.getCity() != null ? ur.getCity().getId() : null,
                        ur.getCity() != null ? ur.getCity().getName() : null,
                        ur.getContent(),
                        ur.getCreatedAt(),
                        ur.getImages().isEmpty() ? null : imageService.createPresignedImageUrl(ur.getImages().get(0))
                ))
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "map-countries", allEntries = true),
            @CacheEvict(cacheNames = "map-states", allEntries = true),
            @CacheEvict(cacheNames = "map-cities", allEntries = true),
            @CacheEvict(cacheNames = "scam-action-stats", allEntries = true),
            @CacheEvict(cacheNames = "scam-context-stats", allEntries = true)
    })
    @Transactional
    public void updateUserReport(Long reportId, String email, UserReportUpdateRequest request, List<MultipartFile> images) {
        UserReport report = userReportRepository.findById(reportId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(ReportNotFoundException::new);

        if (!email.equals(report.getUser().getEmail())) {
            throw new ForbiddenReportAccessException();
        }

        report.update(request.title(), request.description());

        ScamAction scamAction = scamService.findScamActionById(request.scamActionId());
        report.setScamAction(scamAction);

        ScamContext scamContext = scamService.findScamContextById(request.scamContextId());
        report.setScamContext(scamContext);

        Long oldCountryId = report.getCountry().getId();
        Country country = countryService.findCountryById(request.countryId());
        report.setCountry(country);

        if (!oldCountryId.equals(country.getId())) {
            userCountryStatsRepository.decrement(oldCountryId);
            userCountryStatsRepository.increment(country.getId());
        }

        State state = Optional.ofNullable(request.stateId())
                .map(stateService::findStateByIdWithCountry)
                .orElse(null);

        if (state != null && !isStateOfCountry(state, country)) {
            throw new StateCountryMismatchException();
        }
        report.setState(state);

        City city = Optional.ofNullable(request.cityId())
                .map(cityService::findCityByIdWithState)
                .orElse(null);

        if (city != null && state != null && !isCityOfState(city, state)) {
            throw new CityStateMismatchException();
        }
        report.setCity(city);

        boolean hasNewImages = images != null && !images.isEmpty();
        boolean shouldRemoveImages = Boolean.TRUE.equals(request.removeImage());

        if (hasNewImages || shouldRemoveImages) {
            List<Image> oldImages = new ArrayList<>(report.getImages());
            List<Image> newImages = hasNewImages
                    ? imageService.saveImagesInS3Bucket(images)
                    : Collections.emptyList();

            report.getImages().clear();
            newImages.forEach(report::addImage);
            imageService.deleteImagesFromOci(oldImages);
        }
        log.info("User report updated: id={}, user={}", reportId, email);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "map-countries", allEntries = true),
            @CacheEvict(cacheNames = "map-states", allEntries = true),
            @CacheEvict(cacheNames = "map-cities", allEntries = true),
            @CacheEvict(cacheNames = "scam-action-stats", allEntries = true),
            @CacheEvict(cacheNames = "scam-context-stats", allEntries = true)
    })
    @Transactional
    public void deleteUserReport(Long reportId, String email) {
        UserReport report = userReportRepository.findById(reportId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(ReportNotFoundException::new);

        if (!email.equals(report.getUser().getEmail())) {
            throw new ForbiddenReportAccessException();
        }

        report.softDelete();
        userCountryStatsRepository.decrement(report.getCountry().getId());
        log.info("User report deleted: id={}, user={}", reportId, email);
    }

}
