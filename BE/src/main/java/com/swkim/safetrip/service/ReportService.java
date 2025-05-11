package com.swkim.safetrip.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.exception.CoordinatesNotValidException;
import com.swkim.safetrip.exception.ReportNotFoundException;
import com.swkim.safetrip.exception.S3UploadException;
import com.swkim.safetrip.exception.ScamNotFoundException;
import com.swkim.safetrip.mapper.ReportMapper;
import com.swkim.safetrip.repository.*;
import com.swkim.safetrip.vo.CountryCityData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ScamRepository scamRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ImageRepository imageRepository;

    private final AmazonS3Client amazonS3Client;

    public Long saveReport(ReportSaveRequest reportSaveRequest, List<MultipartFile> files) {

        // 1. reportRequest -> Report Mapping
        Report report = ReportMapper.toReport(reportSaveRequest);

        // 2. scam 객체 report에 추가
        Scam findScam = scamRepository.findById(reportSaveRequest.getScamId()).orElseThrow(ScamNotFoundException::new);
        report.setScam(findScam);

        // 3. 이미지 S3에 전송하고 report에 추가
        List<Image> savedImageList = saveImagesInS3Bucket(files);
        savedImageList.forEach(report::addImage);

        // 4. Country, City 정보 Get
        CountryCityData countryCityData = getCountryCityData(reportSaveRequest);

        // 5. Country, City 엔티티 저장. address에 대한 location 객체 생성
        Location location = saveLocationData(countryCityData, reportSaveRequest.getAddress(), reportSaveRequest.getLat(), reportSaveRequest.getLng());

        // 6. report 저장
        return save(report, location);
    }

    @Transactional
    public Page<ReportFindAllResponse> getReports(String country, String city, Pageable pageable) {
        return reportRepository.findAllByCountryAndCity(country, city, pageable);
    }

    @Transactional
    public ReportFindByIdResponse getReport(Long id){

        Report report = reportRepository.findReportWithLocationById(id).orElseThrow(ReportNotFoundException::new);
        List<Image> images = imageRepository.findImagesByReportId(id);
        List<String> URLs = images.stream()
                .map(Image::getAccessURL)
                .toList();

        return ReportMapper.toReportFindByIdResponse(report, URLs);
    }

    @Transactional
    private Long save(Report report, Location location) {
        report.setLocation(location);
        Report savedReport = reportRepository.save(report);
        return savedReport.getId();
    }

    @Transactional
    private Location saveLocationData(CountryCityData countryCityData, String address, String locationLat, String locationLng){

        String countryName = countryCityData.getCountryName();
        String cityName = countryCityData.getCityName();

        Optional<Country> findCountry = countryRepository.findByName(countryName);

        Country country;
        City city;

        if(findCountry.isEmpty()){ // country가 없는 경우. country에 있는 city도 당연히 없으니 두 객체 모두 생성
            country = Country.builder()
                    .name(countryName)
                    .build();

            city = City.builder()
                    .name(countryCityData.getCityName())
                    .lat(countryCityData.getCityLat())
                    .lng(countryCityData.getCityLng())
                    .build();

            country.addCity(city);
            countryRepository.save(country);
        }else{
            country = findCountry.get();
            Optional<City> findCity = cityRepository.findByNameAndCountryId(cityName, country.getId());

            if(findCity.isPresent()){ // country도 있고 city도 있는 경우
                city = findCity.get();
            }else{
                city = City.builder()
                        .name(countryCityData.getCityName())
                        .lat(countryCityData.getCityLat())
                        .lng(countryCityData.getCityLng())
                        .build();

                country.addCity(city);
                cityRepository.save(city);
            }
        }

        return Location.builder()
                .country(country)
                .city(city)
                .address(address)
                .lat(locationLat)
                .lng(locationLng)
                .build();

    }

    private CountryCityData getCountryCityData(ReportSaveRequest reportSaveRequest) {
        String locationInfo = getLocationInfo(reportSaveRequest.getLat(), reportSaveRequest.getLng());
        JsonObject locationObject = JsonParser.parseString(locationInfo).getAsJsonObject();
        JsonObject addressObject = locationObject.getAsJsonObject("address");

        String cityName = addressObject.get("city").getAsString();

        String cityInfo = getCityInfo(cityName);
        JsonObject cityObject = JsonParser.parseString(cityInfo).getAsJsonArray().get(0).getAsJsonObject();

        String countryName = cityObject.getAsJsonObject("address").get("country").getAsString();
        cityName = cityObject.getAsJsonObject("address").get("city").getAsString();

        String cityLat = cityObject.get("lat").getAsString();
        String cityLng = cityObject.get("lon").getAsString();


        return new CountryCityData(countryName, cityName, cityLat, cityLng);
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

    /**
     * @return 도시 이름 -> 도시 위도, 경도
     */
    private String getCityInfo(String city){
        RestClient restClient = RestClient.create();
        String uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .path("/search")
                .queryParam("city",city)
                .queryParam("format", "json")
                .queryParam("addressdetails", "1")
                .queryParam("accept-language", "ko")
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    throw new CoordinatesNotValidException();
                }))
                .body(String.class);
    }

    /**
     * @return 위도 경도 -> 국가, 도시 정보
     */
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
                    throw new CoordinatesNotValidException();
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
            throw new S3UploadException();
        }

        String accessURL = amazonS3Client.getUrl(bucketName, fileName).toString();
        image.setAccessURL(accessURL);
        return image;
    }
}
