package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UserReportSaveRequest;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.entity.world.State;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.StateCountryMismatchException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.UserReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserReportService {

    private final UserService userService;
    private final ScamService scamService;
    private final ImageService imageService;
    private final StateService stateService;
    private final CountryService countryService;

    private final UserReportRepository userReportRepository;

    public Long saveUserReport(String email, UserReportSaveRequest userReportSaveRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        UserReport userReport = ReportMapper.toReport(userReportSaveRequest);

        // 2. User 객체 report에 추가
        User findUser = userService.findUserByEmail(email).orElseThrow(UserNotFoundException::new);
        userReport.setUser(findUser);

        // 3. scam 객체 report에 추가
        ScamAction findScamAction = scamService.findScamById(userReportSaveRequest.scamId());
        userReport.setScamAction(findScamAction);

        // CONSIDER: 이미지 업로드 방식 개선 (pre-signed URL 도입 검토)
        // 4. 이미지 S3에 전송하고 report에 추가
        List<Image> savedImageList = imageService.saveImagesInS3Bucket(files);
        savedImageList.forEach(userReport::addImage);

        // 5. state 객체, country 객체 set
        Country findCountry = countryService.findCountryById(userReportSaveRequest.countryId());
        State findState = stateService.findStateByIdWithCountry(userReportSaveRequest.stateId());

        if (!isStateOfCountry(findState, findCountry)) {
            throw new StateCountryMismatchException();
        }
        userReport.setCountry(findCountry);
        userReport.setState(findState);

        // 7. report 저장
        return save(userReport);
    }

    @Transactional
    private Long save(UserReport userReport) {
        UserReport savedUserReport = userReportRepository.save(userReport);
        return savedUserReport.getId();
    }

    @Transactional(readOnly = true)
    public UserReportDetailResponse getUserReport(Long id){
        UserReportDetailResponse userReportDetailResponse
                = userReportRepository.findReportDetailById(id).orElseThrow(ReportNotFoundException::new);// consider UserReport용 예외 만들까?

        List<String> URLs = getImageUrlsById(id);

        userReportDetailResponse.setURLs(URLs);

        return userReportDetailResponse;
    }

    private List<String> getImageUrlsById(Long id) {
        List<Image> images = imageService.findImagesByReportId(id);
        return images.stream()
                .map(Image::getAccessURL)
                .toList();
    }

    private boolean isStateOfCountry(State findState, Country findCountry) {
        return Objects.equals(findState.getCountry().getId(), findCountry.getId());
    }

}
