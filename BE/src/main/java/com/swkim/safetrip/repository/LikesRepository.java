package com.swkim.safetrip.repository;

import com.swkim.safetrip.entity.Likes;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByUser_EmailAndTargetIdAndTargetType(String email, Long targetId, TargetType targetType);

    List<Likes> findAllByUser_EmailAndTargetTypeAndTargetIdIn(String email, TargetType targetType, Collection<Long> targetIds);

    void deleteAllByUser(User user);
}
