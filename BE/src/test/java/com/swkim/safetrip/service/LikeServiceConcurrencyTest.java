package com.swkim.safetrip.service;

import com.swkim.safetrip.entity.Comment;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.repository.CommentRepository;
import com.swkim.safetrip.repository.LikesRepository;
import com.swkim.safetrip.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LikeServiceConcurrencyTest {

    @Autowired LikeService likeService;
    @Autowired CommentRepository commentRepository;
    @Autowired UserRepository userRepository;
    @Autowired LikesRepository likesRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    Long commentId;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .email("a@test.com").nickname("userA").password("pw").role(Role.USER).build());
        userRepository.save(User.builder()
                .email("b@test.com").nickname("userB").password("pw").role(Role.USER).build());

        // UserReport의 NOT NULL 제약(source, country_id)을 피해 comment만 직접 삽입
        jdbcTemplate.update(
                "INSERT INTO comment (content, likes, depth, is_deleted, created_at, updated_at) " +
                "VALUES ('test comment', 0, 0, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
        );
        commentId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM comment", Long.class);
    }

    @AfterEach
    void tearDown() {
        likesRepository.deleteAll();
        commentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 두_유저가_동시에_좋아요를_누르면_likeCnt가_2여야_한다() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            likeService.toggleCommentLike(commentId, "a@test.com");
        });
        Thread t2 = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            likeService.toggleCommentLike(commentId, "b@test.com");
        });

        t1.start();
        t2.start();
        latch.countDown(); // 동시 출발
        t1.join();
        t2.join();

        Comment comment = commentRepository.findById(commentId).orElseThrow();
        assertThat(comment.getLikeCnt()).isEqualTo(2);
    }
}
