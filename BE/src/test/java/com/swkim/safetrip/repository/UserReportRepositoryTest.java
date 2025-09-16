package com.swkim.safetrip.repository;

import com.swkim.safetrip.config.QuerydslConfig;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.*;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.entity.enums.Source;
import com.swkim.safetrip.entity.world.Country;
import com.swkim.safetrip.entity.world.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Import({QuerydslConfig.class})
class UserReportRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScamRepository scamRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @BeforeEach
    void setUp() {
        userReportRepository.deleteAll();
        stateRepository.deleteAll();
        countryRepository.deleteAll();
        scamRepository.deleteAll();
        userRepository.deleteAll();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("상세 유저 리포트 정보를 조회하면 DTO를 올바르게 매핑하고 리턴한다")
    void findReportDetailById_Success() {
        // given
        User savedUser = saveUser();
        ScamAction savedScamAction = saveScam();
        Country savedCountry = saveCountry();
        State savedState = saveState(savedCountry);
        UserReport savedUserReport = saveUserReport(savedUser, savedScamAction, savedCountry, savedState);

        // when
        Optional<UserReportDetailResponse> result =
                userReportRepository.findReportDetailById(savedUserReport.getId());

        // then
        assertThat(result).isPresent();

        UserReportDetailResponse dto = result.get();
        assertAll(
                () -> assertThat(dto.getNickname()).isEqualTo("nickname"),
                () -> assertThat(dto.getTitle()).isEqualTo("title"),
                () -> assertThat(dto.getScamType()).isEqualTo("Pickpocket"),
                () -> assertThat(dto.getCountryName()).isEqualTo("France"),
                () -> assertThat(dto.getStateName()).isEqualTo("Paris"),
                () -> assertThat(dto.getContent()).isEqualTo("description")
        );
    }

    private UserReport saveUserReport(User user, ScamAction scamAction, Country country, State state) {
        UserReport userReport = UserReport.builder()
                .source(Source.SAFETRIP)
                .title("title")
                .description("description")
                .build();

        // 양방향 관계 설정
        userReport.setUser(user);
        userReport.setScamAction(scamAction);
        userReport.setCountry(country);
        userReport.setState(state);

        return userReportRepository.save(userReport);
    }

    private State saveState(Country country) {
        State state = State.builder()
                .datasetId(2L)
                .name("Paris")
                .koreanName("파리")
                .lat(48.856614)
                .lng(121.3522972)
                .build();

        state.setCountry(country);
        return stateRepository.save(state);
    }

    private Country saveCountry() {
        Country country = Country.builder()
                .datasetId(1L)
                .name("France")
                .koreanName("프랑스")
                .lat(63.34234)
                .lng(119.13842)
                .build();

        return countryRepository.save(country);
    }

    private ScamAction saveScam() {
        ScamAction scamAction = ScamAction.builder()
                .name("Pickpocket")
                .build();
        return scamRepository.save(scamAction);
    }

    private User saveUser() {
        User user = User.builder()
                .email("email")
                .password("password")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

}