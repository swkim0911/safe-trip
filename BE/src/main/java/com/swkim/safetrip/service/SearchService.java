package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.SearchResultItem;
import com.swkim.safetrip.repository.ReportJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ReportJdbcRepository reportJdbcRepository;

    @Transactional(readOnly = true)
    public List<SearchResultItem> search(String q) {
        return reportJdbcRepository.search("%" + q + "%");
    }
}
