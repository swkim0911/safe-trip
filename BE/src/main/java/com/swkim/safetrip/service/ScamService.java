package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.ScamAction;
import com.swkim.safetrip.global.exception.custom.ScamNotFoundException;
import com.swkim.safetrip.repository.ScamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScamService {

    private final ScamRepository scamRepository;

    public ScamAction findScamById(Long scamId) {
        return scamRepository.findById(scamId).orElseThrow(ScamNotFoundException::new);
    }
}
