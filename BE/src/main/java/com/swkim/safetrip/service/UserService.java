package com.swkim.safetrip.service;

import com.swkim.safetrip.dto.request.UpdateNicknameRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.entity.Image;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    // 트랜잭션 커밋 후 OCI 삭제를 위해 프록시를 통한 self-call 필요
    @Lazy
    @Autowired
    private UserService self;

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

    public void deleteAccount(String email, HttpServletResponse response) {
        List<Image> images = self.deleteAccountInTransaction(email, response);
        imageService.deleteImagesFromOci(images);
    }

    @Transactional
    public List<Image> deleteAccountInTransaction(String email, HttpServletResponse response) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        List<UserReport> userReports = userReportRepository.findAllByUserWithImages(user);
        List<Image> images = userReports.stream()
                .flatMap(report -> report.getImages().stream())
                .toList();
        userReportRepository.deleteAll(userReports);

        commentRepository.anonymizeAndSoftDeleteByUser(user, LocalDateTime.now());

        likesRepository.deleteAllByUser(user);
        reportInaccuracyRepository.deleteAllByUser(user);

        tokenService.deleteRefreshToken(email);

        userRepository.delete(user);

        response.addCookie(CookieUtils.makeExpiredRefreshTokenCookie());

        log.info("User account deleted: {}", email);
        return images;
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
