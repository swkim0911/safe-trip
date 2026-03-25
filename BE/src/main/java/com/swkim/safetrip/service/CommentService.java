package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.CommentCreateRequest;
import com.swkim.safetrip.dto.request.CommentUpdateRequest;
import com.swkim.safetrip.dto.response.CommentItem;
import com.swkim.safetrip.entity.Comment;
import com.swkim.safetrip.entity.ExternalReport;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.UserReport;
import com.swkim.safetrip.global.exception.custom.CommentNotFoundException;
import com.swkim.safetrip.global.exception.custom.ForbiddenCommentAccessException;
import com.swkim.safetrip.global.exception.custom.InvalidParentCommentException;
import com.swkim.safetrip.global.exception.custom.ReportNotFoundException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.repository.CommentRepository;
import com.swkim.safetrip.repository.ExternalReportRepository;
import com.swkim.safetrip.repository.UserReportRepository;
import com.swkim.safetrip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserReportRepository userReportRepository;
    private final ExternalReportRepository externalReportRepository;
    private final UserRepository userRepository;

    // ── UserReport 댓글 ──────────────────────────────────────────

    @Transactional
    public Long createForUserReport(Long reportId, String email, CommentCreateRequest request) {
        UserReport report = userReportRepository.findById(reportId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(ReportNotFoundException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Comment comment;
        if (request.parentCommentId() != null) {
            Comment parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(CommentNotFoundException::new);
            if (parent.getDepth() != 0) throw new InvalidParentCommentException();
            comment = Comment.createReply(report, user, request.content(), parent);
        } else {
            comment = Comment.create(report, user, request.content());
        }

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentItem> getCommentsForUserReport(Long reportId) {
        userReportRepository.findById(reportId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(ReportNotFoundException::new);

        return buildCommentTree(commentRepository.findTopLevelByUserReportId(reportId));
    }

    @Transactional
    public void updateForUserReport(Long reportId, Long commentId, String email, CommentUpdateRequest request) {
        validateUserReportExists(reportId);
        Comment comment = findActiveComment(commentId);
        validateOwner(comment, email);
        comment.update(request.content());
    }

    @Transactional
    public void deleteForUserReport(Long reportId, Long commentId, String email) {
        validateUserReportExists(reportId);
        Comment comment = findActiveComment(commentId);
        validateOwner(comment, email);
        comment.softDelete();
    }

    // ── ExternalReport 댓글 ──────────────────────────────────────

    @Transactional
    public Long createForExternalReport(Long reportId, String email, CommentCreateRequest request) {
        ExternalReport report = externalReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Comment comment;
        if (request.parentCommentId() != null) {
            Comment parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(CommentNotFoundException::new);
            if (parent.getDepth() != 0) throw new InvalidParentCommentException();
            comment = Comment.createReplyForExternal(report, user, request.content(), parent);
        } else {
            comment = Comment.createForExternal(report, user, request.content());
        }

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentItem> getCommentsForExternalReport(Long reportId) {
        externalReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        return buildCommentTree(commentRepository.findTopLevelByExternalReportId(reportId));
    }

    @Transactional
    public void updateForExternalReport(Long reportId, Long commentId, String email, CommentUpdateRequest request) {
        validateExternalReportExists(reportId);
        Comment comment = findActiveComment(commentId);
        validateOwner(comment, email);
        comment.update(request.content());
    }

    @Transactional
    public void deleteForExternalReport(Long reportId, Long commentId, String email) {
        validateExternalReportExists(reportId);
        Comment comment = findActiveComment(commentId);
        validateOwner(comment, email);
        comment.softDelete();
    }

    // ── 공통 ────────────────────────────────────────────────────

    private List<CommentItem> buildCommentTree(List<Comment> topLevel) {
        if (topLevel.isEmpty()) return List.of();

        List<Long> topLevelIds = topLevel.stream().map(Comment::getId).toList();
        List<Comment> replies = commentRepository.findRepliesByParentIds(topLevelIds);

        Map<Long, List<Comment>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return topLevel.stream()
                .map(c -> toItem(c, repliesByParentId.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    private Comment findActiveComment(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(CommentNotFoundException::new);
    }

    private void validateUserReportExists(Long reportId) {
        userReportRepository.findById(reportId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(ReportNotFoundException::new);
    }

    private void validateExternalReportExists(Long reportId) {
        externalReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);
    }

    private void validateOwner(Comment comment, String email) {
        if (comment.getUser() == null || !email.equals(comment.getUser().getEmail())) {
            throw new ForbiddenCommentAccessException();
        }
    }

    private CommentItem toItem(Comment comment, List<Comment> replies) {
        String authorNickname = comment.getUser() != null
                ? comment.getUser().getNickname()
                : "탈퇴한 사용자";
        String content = comment.getIsDeleted() ? null : comment.getContent();

        List<CommentItem> replyItems = replies.stream()
                .map(r -> toItem(r, List.of()))
                .toList();

        return new CommentItem(
                comment.getId(),
                authorNickname,
                content,
                comment.getLikeCnt(),
                false, // likedByMe — 좋아요 기능 구현 시 반영
                comment.getCreatedAt(),
                comment.getIsDeleted(),
                replyItems
        );
    }
}
