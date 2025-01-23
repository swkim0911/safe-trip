package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final ReportRepository reportRepository;
    private final AmazonS3Client amazonS3Client;

    @Transactional
    public Long write(ReportRequest reportRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        Report report = ReportMapper.toReport(reportRequest);

        // 2. 이미지 S3에 전송
        Optional.ofNullable(files)
                .orElse(Collections.emptyList())
                .forEach(file -> {
                    Image image = saveImage(file);
                    report.addImage(image);
                });

        // 3. address에 대한 location 객체 생성
        String locationInfo = getLocationInfo(reportRequest.getLatitude(), reportRequest.getLongitude());

        JsonObject locationObject = JsonParser.parseString(locationInfo).getAsJsonObject();
        JsonObject addressObject = locationObject.getAsJsonObject("address");
        String country = addressObject.get("country").getAsString();
        String city = addressObject.get("city").getAsString();

        String cityInfo = getCityInfo(city);

        JsonObject cityObject = JsonParser.parseString(cityInfo).getAsJsonArray().get(0).getAsJsonObject();
        String lat = cityObject.get("lat").getAsString();
        String lon = cityObject.get("lon").getAsString();

        // Location location = new Location
        // Country country = new Country
        // City city = new City

        // report.setLocation(location)
        // 4. report 저장
        return 1L;
//        Report savedReport = reportRepository.save(report);
//        return savedReport.getId();
    }

    private String getCityInfo(String city){
        RestClient restClient = RestClient.create();
        String uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .path("/search")
                .queryParam("city",city)
                .queryParam("format", "json")
                .queryParam("addressdetails", "1")
                .queryParam("accept-language", "en")
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }

    private String getLocationInfo(String lat, String lon) {
        RestClient restClient = RestClient.create();
        String uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .path("/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("format", "json")
                .queryParam("addressdetails", "1")
                .queryParam("accept-language", "en")
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
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
            throw new RuntimeException(e);
        }

        String accessURL = amazonS3Client.getUrl(bucketName, fileName).toString();
        image.setAccessURL(accessURL);
        return image;
    }
}
