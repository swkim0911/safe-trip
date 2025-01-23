package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swkim.safetrip.dto.request.ReportRequest;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.CountryRepository;
import com.swkim.safetrip.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    private final ReportRepository reportRepository;
    private final CountryRepository countryRepository;
    private final AmazonS3Client amazonS3Client;

    @Transactional
    public Long write(ReportRequest reportRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        Report report = ReportMapper.toReport(reportRequest);

        // 2. 이미지 S3에 전송하고 report에 저장
        saveImagesInS3Bucket(files).forEach(report::addImage);

        // 4. Country, City 엔티티 생성

        String locationInfo = getLocationInfo(reportRequest.getLatitude(), reportRequest.getLongitude());

        JsonObject locationObject = JsonParser.parseString(locationInfo).getAsJsonObject();
        JsonObject addressObject = locationObject.getAsJsonObject("address");
        String countryName = addressObject.get("country").getAsString();
        String cityName = addressObject.get("city").getAsString();

        String cityInfo = getCityInfo(cityName);

        JsonObject cityObject = JsonParser.parseString(cityInfo).getAsJsonArray().get(0).getAsJsonObject();
        String latitude = cityObject.get("lat").getAsString();
        String longitude = cityObject.get("lon").getAsString();

        City city = City.builder()
                .name(cityName)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        Country country = Country.builder()
                .name(countryName)
                .build();

        country.addCity(city);
        countryRepository.save(country);

        // 4. address에 대한 location 객체 생성
        Location location = Location.builder()
                .country(country)
                .city(city)
                .latitude(reportRequest.getLatitude())
                .longitude(reportRequest.getLongitude())
                .build();
        // 5. report 저장

        report.setLocation(location);
        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
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
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    throw new RuntimeException(); // todo
                }))
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
                .onStatus(HttpStatusCode :: is4xxClientError, ((request, response) -> {
                    throw new RuntimeException(); // todo
                }))
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
