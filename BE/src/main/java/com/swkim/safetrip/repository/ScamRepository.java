package com.swkim.safetrip.repository;


import com.swkim.safetrip.entity.ScamAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScamRepository extends JpaRepository<ScamAction, Long> {

}
