package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.State;
import com.swkim.safetrip.global.exception.custom.StateNotFoundException;
import com.swkim.safetrip.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateService{

    private final StateRepository stateRepository;

    public State findStateByIdWithCountry(Long id) {
        return stateRepository.findByIdWithCountry(id).orElseThrow(StateNotFoundException::new);
    }

}
