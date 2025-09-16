package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.ScamContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScamContextRepository extends JpaRepository<ScamContext, Long> {
}
