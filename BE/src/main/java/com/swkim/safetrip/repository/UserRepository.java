package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndEmailNot(String nickname, String email);

    Optional<User> findByEmail(String email);
}
