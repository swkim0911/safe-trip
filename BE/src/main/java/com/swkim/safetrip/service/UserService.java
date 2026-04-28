package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UpdateNicknameRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.UserReport;
import com.swkim.safetrip.global.exception.custom.DuplicateUserEmailException;
import com.swkim.safetrip.global.exception.custom.DuplicateUserNicknameException;
import com.swkim.safetrip.global.exception.custom.UserNotFoundException;
import com.swkim.safetrip.global.utils.CookieUtils;
import com.swkim.safetrip.global.validation.SignUpValidator;
import com.swkim.safetrip.mapper.UserMapper;
import com.swkim.safetrip.repository.CommentRepository;
import com.swkim.safetrip.repository.LikesRepository;
import com.swkim.safetrip.repository.ReportInaccuracyRepository;
import com.swkim.safetrip.repository.UserReportRepository;
import com.swkim.safetrip.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final CommentRepository commentRepository;
    private final LikesRepository likesRepository;
    private final ReportInaccuracyRepository reportInaccuracyRepository;
    private final ImageService imageService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final SignUpValidator signUpValidator;

    @Transactional
    public Long signup(UserSignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.email())){
            throw new DuplicateUserEmailException();
        }

        if (userRepository.existsByNickname(signUpRequest.nickname())) {
            throw new DuplicateUserNicknameException();
        }

        User user = UserMapper.toUser(signUpRequest);
        // 비밀번호 암호화
        user.passwordEncode(passwordEncoder);

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", signUpRequest.email());
        return savedUser.getId();
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateEmail(String email) {

        if(!signUpValidator.isValidEmail(email)) {
            return ValidationResponse.builder()
                    .isValidFormat(false)
                    .isAvailable(false)
                    .reason("Invalid email format")
                    .build();
        }

        boolean isDuplicated = userRepository.existsByEmail(email);

        return ValidationResponse.builder()
                .isValidFormat(true)
                .isAvailable(!isDuplicated)
                .reason(isDuplicated ? "Email already in use" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateNickname(String nickname) {

        if(!signUpValidator.isValidNickname(nickname)) {
            return ValidationResponse.builder()
                    .isValidFormat(false)
                    .isAvailable(false)
                    .reason("Invalid nickname format")
                    .build();
        }

        boolean isDuplicated = userRepository.existsByNickname(nickname);

        return ValidationResponse.builder()
                .isValidFormat(true)
                .isAvailable(!isDuplicated)
                .reason(isDuplicated ? "Nickname already in use" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteAccount(String email, HttpServletResponse response) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        // 1. UserReport에 연결된 OCI 이미지 삭제 후 UserReport hard delete (cascade로 Image row도 삭제)
        List<UserReport> userReports = userReportRepository.findAllByUserWithImages(user);
        userReports.forEach(report -> imageService.deleteImagesFromOci(report.getImages()));
        userReportRepository.deleteAll(userReports);

        // 2. Comment soft delete + 익명화 (대댓글 트리 보존)
        commentRepository.anonymizeAndSoftDeleteByUser(user, LocalDateTime.now());

        // 3. Likes, ReportInaccuracy 삭제
        likesRepository.deleteAllByUser(user);
        reportInaccuracyRepository.deleteAllByUser(user);

        // 4. Redis refresh token 삭제
        tokenService.deleteRefreshToken(email);

        // 5. User 삭제
        userRepository.delete(user);

        // 6. refreshToken 쿠키 만료 처리
        response.addCookie(CookieUtils.makeExpiredRefreshTokenCookie());

        log.info("User account deleted: {}", email);
    }

    @Transactional
    public void updateNickname(String email, UpdateNicknameRequest request) {
        if (userRepository.existsByNicknameAndEmailNot(request.nickname(), email)) {
            throw new DuplicateUserNicknameException();
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        user.updateNickname(request.nickname());
        log.info("User updated nickname: {}", email);
    }

}
