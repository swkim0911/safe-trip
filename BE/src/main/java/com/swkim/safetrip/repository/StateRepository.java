package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.world.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    @Query("SELECT s FROM State s JOIN FETCH s.country WHERE s.id = :id")
    Optional<State> findByIdWithCountry(@Param("id") Long id);
}
