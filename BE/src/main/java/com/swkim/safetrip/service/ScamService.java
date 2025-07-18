package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.Scam;
import com.swkim.safetrip.global.exception.custom.ScamNotFoundException;
import com.swkim.safetrip.repository.ScamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScamService {

    private final ScamRepository scamRepository;

    public Scam findScamById(Long scamId) {
        return scamRepository.findById(scamId).orElseThrow(ScamNotFoundException::new);
    }
}
