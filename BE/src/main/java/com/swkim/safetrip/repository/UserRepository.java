package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
