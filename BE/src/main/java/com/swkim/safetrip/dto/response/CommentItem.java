package com.swkim.safetrip.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record CommentItem(
        Long id,
        String authorNickname,
        String content,       // null if isDeleted=true
        int likeCnt,
        boolean likedByMe,
        OffsetDateTime createdAt,
        boolean isDeleted,
        List<CommentItem> replies
) {}
