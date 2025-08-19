package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    Optional<State> findByNameAndCountryId(String name, Long countryId);
}
