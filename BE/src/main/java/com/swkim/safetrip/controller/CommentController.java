package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.CommentCreateRequest;
import com.swkim.safetrip.dto.request.CommentUpdateRequest;
import com.swkim.safetrip.dto.response.CommentItem;
import com.swkim.safetrip.dto.response.LikeToggleResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.CommentService;
import com.swkim.safetrip.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    // ── UserReport 댓글 ──────────────────────────────────────────

    @Operation(summary = "UserReport 댓글 작성", security = @SecurityRequirement(name = "BearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/user-reports/{reportId}/comments")
    public ApiResult<Long> createForUserReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {
        Long id = commentService.createForUserReport(reportId, userDetails.getUsername(), request);
        return ApiResult.of(HttpStatus.CREATED.value(), "Comment created", id);
    }

    @Operation(summary = "UserReport 댓글 목록 조회")
    @GetMapping("/v1/user-reports/{reportId}/comments")
    public ApiResult<List<CommentItem>> getCommentsForUserReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ApiResult.of(HttpStatus.OK.value(), "Comments retrieved",
                commentService.getCommentsForUserReport(reportId, email));
    }

    @Operation(summary = "UserReport 댓글 수정", security = @SecurityRequirement(name = "BearerAuth"))
    @PatchMapping("/v1/user-reports/{reportId}/comments/{commentId}")
    public ApiResult<Void> updateForUserReport(
            @PathVariable Long reportId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentUpdateRequest request) {
        commentService.updateForUserReport(reportId, commentId, userDetails.getUsername(), request);
        return ApiResult.of(HttpStatus.OK.value(), "Comment updated", null);
    }

    @Operation(summary = "UserReport 댓글 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    @DeleteMapping("/v1/user-reports/{reportId}/comments/{commentId}")
    public ApiResult<Void> deleteForUserReport(
            @PathVariable Long reportId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteForUserReport(reportId, commentId, userDetails.getUsername());
        return ApiResult.of(HttpStatus.OK.value(), "Comment deleted", null);
    }

    // ── ExternalReport 댓글 ──────────────────────────────────────

    @Operation(summary = "ExternalReport 댓글 작성", security = @SecurityRequirement(name = "BearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/external-reports/{reportId}/comments")
    public ApiResult<Long> createForExternalReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {
        Long id = commentService.createForExternalReport(reportId, userDetails.getUsername(), request);
        return ApiResult.of(HttpStatus.CREATED.value(), "Comment created", id);
    }

    @Operation(summary = "ExternalReport 댓글 목록 조회")
    @GetMapping("/v1/external-reports/{reportId}/comments")
    public ApiResult<List<CommentItem>> getCommentsForExternalReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ApiResult.of(HttpStatus.OK.value(), "Comments retrieved",
                commentService.getCommentsForExternalReport(reportId, email));
    }

    @Operation(summary = "ExternalReport 댓글 수정", security = @SecurityRequirement(name = "BearerAuth"))
    @PatchMapping("/v1/external-reports/{reportId}/comments/{commentId}")
    public ApiResult<Void> updateForExternalReport(
            @PathVariable Long reportId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentUpdateRequest request) {
        commentService.updateForExternalReport(reportId, commentId, userDetails.getUsername(), request);
        return ApiResult.of(HttpStatus.OK.value(), "Comment updated", null);
    }

    @Operation(summary = "ExternalReport 댓글 삭제", security = @SecurityRequirement(name = "BearerAuth"))
    @DeleteMapping("/v1/external-reports/{reportId}/comments/{commentId}")
    public ApiResult<Void> deleteForExternalReport(
            @PathVariable Long reportId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteForExternalReport(reportId, commentId, userDetails.getUsername());
        return ApiResult.of(HttpStatus.OK.value(), "Comment deleted", null);
    }

    // ── 좋아요 ────────────────────────────────────────────────────

    @Operation(summary = "댓글 좋아요 토글", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/v1/comments/{commentId}/like")
    public ApiResult<LikeToggleResponse> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResult.of(HttpStatus.OK.value(), "Like toggled",
                likeService.toggleCommentLike(commentId, userDetails.getUsername()));
    }
}
