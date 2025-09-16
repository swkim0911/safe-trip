package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.ScamAction;
import com.swkim.safetrip.entity.ScamContext;
import com.swkim.safetrip.global.exception.custom.ScamNotFoundException;
import com.swkim.safetrip.repository.ScamActionRepository;
import com.swkim.safetrip.repository.ScamContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScamService {

    private final ScamActionRepository scamActionRepository;
    private final ScamContextRepository scamContextRepository;

    public ScamAction findScamActionById(Long scamActionId) {
        return scamActionRepository.findById(scamActionId).orElseThrow(ScamNotFoundException::new);
    }

    public ScamContext findScamContextById(Long scamContextId) {
        return scamContextRepository.findById(scamContextId).orElseThrow(ScamNotFoundException::new);
    }
}
