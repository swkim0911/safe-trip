package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.Image;
import com.swkim.safetrip.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public List<Image> findImagesByReportId(Long id) {
        return imageRepository.findImagesByReportId(id);
    }

}
