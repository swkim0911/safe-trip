package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.response.LikeToggleResponse;
import com.swkim.safetrip.entity.Comment;
import com.swkim.safetrip.entity.Likes;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Status;
import com.swkim.safetrip.entity.enums.TargetType;
import com.swkim.safetrip.global.exception.custom.CommentNotFoundException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.repository.CommentRepository;
import com.swkim.safetrip.repository.LikesRepository;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeToggleResponse toggleCommentLike(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(CommentNotFoundException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Optional<Likes> existing = likesRepository.findByUser_EmailAndTargetIdAndTargetType(
                email, commentId, TargetType.COMMENT);

        boolean liked;
        if (existing.isEmpty()) {
            likesRepository.save(Likes.of(user, commentId, TargetType.COMMENT));
            commentRepository.incrementLikeCnt(commentId);
            liked = true;
        } else {
            Likes like = existing.get();
            if (like.getStatus() == Status.ACTIVE) {
                like.cancel();
                commentRepository.decrementLikeCnt(commentId);
                liked = false;
            } else {
                like.activate();
                commentRepository.incrementLikeCnt(commentId);
                liked = true;
            }
        }

        int likeCnt = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new).getLikeCnt();
        return new LikeToggleResponse(liked, likeCnt);
    }
}
