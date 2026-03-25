package com.swkim.safetrip.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CommentItem(
        Long id,
        String authorNickname,
        String content,       // null if isDeleted=true
        int likeCnt,
        boolean likedByMe,
        LocalDateTime createdAt,
        boolean isDeleted,
        List<CommentItem> replies
) {}
