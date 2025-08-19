package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.LocationSummaryItem;
import com.swkim.safetrip.dto.response.LocationSummaryResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.dto.response.ReportSummaryItem;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.S3UploadException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final UserService userService;
    private final ScamService scamService;
    private final ImageService imageService;
    private final StateService stateService;
    private final CountryService countryService;
    private final ReportRepository reportRepository;

    private final AmazonS3Client amazonS3Client;

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
        List<Image> savedImageList = saveImagesInS3Bucket(files);
        savedImageList.forEach(userReport::addImage);

        // 5. state 객체, country 객체 set
        Country findCountry = countryService.findCountryById(reportSaveRequest.getCountryId());
        State findState = stateService.findStateByIdWithCountry(reportSaveRequest.getStateId());
        if (!isMatch(findState, findCountry)) {

        }
        userReport.setCountry(findCountry);
        userReport.setState(findState);

        // 7. report 저장
        return save(userReport);
    }

    private boolean isMatch(State findState, Country findCountry) {
        return Objects.equals(findState.getCountry().getId(), findCountry.getId());
    }

    @Transactional(readOnly = true)
    public LocationSummaryResponse getCountrySummary(){
        List<LocationSummaryItem> reportMapSummaryItems = reportRepository.findCountrySummary();
        return new LocationSummaryResponse("country", reportMapSummaryItems);
    }

    @Transactional(readOnly = true)
    public LocationSummaryResponse getCitySummary(){
        List<LocationSummaryItem> reportMapSummaryItems = reportRepository.findCitySummary();
        return new LocationSummaryResponse("city", reportMapSummaryItems);
    }

    @Transactional(readOnly = true)
    public Slice<LocationSummaryItem> getCountrySummaryPage(Pageable pageable) {
        return reportRepository.findCountrySummarySlice(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<LocationSummaryItem> getCitySummaryPage(Long countryId, Pageable pageable) {
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


    private List<Image> saveImagesInS3Bucket(List<MultipartFile> files) {
        ArrayList<Image> imageList = new ArrayList<>();

        Optional.ofNullable(files)
                .orElse(Collections.emptyList())
                .forEach(file -> {
                    Image image = saveImage(file);
                    imageList.add(image);
                });

        return imageList;
    }

    private Image saveImage(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        Image image = Image.builder()
                .originalName(originalFilename)
                .build();
        String fileName = image.getStoredName();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), objectMetadata);
        } catch (IOException e) {
            throw new S3UploadException();
        }

        String accessURL = amazonS3Client.getUrl(bucketName, fileName).toString();
        image.setAccessURL(accessURL);
        return image;
    }
}
