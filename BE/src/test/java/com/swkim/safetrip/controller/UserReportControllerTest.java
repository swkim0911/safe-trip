package com.swkim.safetrip.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.request.UserReportSaveRequest;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.global.exception.custom.AccessTokenExpiredException;
import com.swkim.safetrip.global.exception.custom.InvalidAccessTokenException;
import com.swkim.safetrip.jwt.JwtProvider;
import com.swkim.safetrip.security.config.SecurityConfig;
import com.swkim.safetrip.service.UserReportService;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc()
@WebMvcTest(UserReportController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
class UserReportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserReportService userReportService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("[Post] /user-reports 요청시 저장된 report의 id를 반환한다")
    @SuppressWarnings("unchecked")
    void scam_보고서_등록_성공시_id를_반환해야한다() throws Exception {

        // given
        UserReportSaveRequest userReportSaveRequest = getMockUserReportSaveRequest();

        MockMultipartFile request = getMockMultipartFile(userReportSaveRequest);
        MockMultipartFile images = new MockMultipartFile("images", "my_image.jpg", MediaType.IMAGE_JPEG_VALUE, "this is image".getBytes());

        mockJwtAuthentication();

        given(userReportService.saveUserReport(any(String.class), any(UserReportSaveRequest.class), anyList())).willReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/user-reports")
                .file(images)
                .file(request)
                .header("Authorization", "Bearer valid.token.here"));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(1L));

    }

    @Test
    @DisplayName("[GET] /user-reports/{reportId} 요청시 저장된 user report 정보를 보인다.")
    void 보고서_id_조회시_보고서_정보를_보인다() throws Exception {

        // given
        Long reportId = 0L;
        UserReportDetailResponse response = UserReportDetailResponse
                .builder()
                .nickname("nickname")
                .title("this is title")
                .scamType("scam")
                .countryName("Korea")
                .stateName("Seoul")
                .content("hello world")
                .createdAt(LocalDateTime.now())
                .build();

        given(userReportService.getUserReport(reportId)).willReturn(response);
        // when
        ResultActions resultActions = mockMvc.perform(get("/user-reports/" + "{reportId}", reportId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.countryName").value("Korea"));
    }

    @Test
    void 글_등록_요청에_액세스_토큰이_없는_경우_401_에러가_발생한다() throws Exception {
        // given
        UserReportSaveRequest userReportSaveRequest = getMockUserReportSaveRequest();
        MockMultipartFile request = getMockMultipartFile(userReportSaveRequest);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/user-reports")
                .file(request));

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }


    @Test
    void 글_등록_요청에_액세스_토큰이_invalid한_경우_401_에러가_발생한다() throws Exception {
        // given
        UserReportSaveRequest userReportSaveRequest = getMockUserReportSaveRequest();
        MockMultipartFile request = getMockMultipartFile(userReportSaveRequest);
        String invalidAccessToken = "im.invalid.token";

        given(jwtProvider.extractAccessToken(any(HttpServletRequest.class)))
                .willReturn(Optional.of(invalidAccessToken));

        doThrow(new InvalidAccessTokenException()).when(jwtProvider).verifyAccessToken(invalidAccessToken);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/user-reports")
                .file(request));
        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void 액세스_토큰이_만료된_상태에서_글_등록_요청시_40101_응답을_반환한다() throws Exception {
        // given
        UserReportSaveRequest userReportSaveRequest = getMockUserReportSaveRequest();
        MockMultipartFile request = getMockMultipartFile(userReportSaveRequest);
        String expiredAccessToken = "im.expired.token";

        given(jwtProvider.extractAccessToken(any(HttpServletRequest.class)))
                .willReturn(Optional.of(expiredAccessToken));

        doThrow(new AccessTokenExpiredException()).when(jwtProvider).verifyAccessToken(expiredAccessToken);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/user-reports")
                .file(request));
        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.message").value("Access token is expired"));
    }

    private UserReportSaveRequest getMockUserReportSaveRequest() {
        String title = "this is title";
        Long scamId = 1L;
        Long countryId = 3L;
        Long stateId = 2L;
        String description = "this is description";

        return UserReportSaveRequest.builder()
                .title(title)
                .scamId(scamId)
                .countryId(countryId)
                .stateId(stateId)
                .description(description)
                .build();

    }

    private void mockJwtAuthentication() {
        String accessToken = "im.access.token";
        String email = "test@gmail.com";
        DecodedJWT decodedJwt = mock(DecodedJWT.class);
        User mockUser = User.builder()
                .email(email)
                .password("password")
                .nickname("nickname")
                .role(Role.USER).build();

        given(jwtProvider.extractAccessToken(any())).willReturn(Optional.of(accessToken));
        given(jwtProvider.verifyAccessToken(eq(accessToken))).willReturn(decodedJwt);
        given(jwtProvider.extractEmail(eq(decodedJwt))).willReturn(Optional.of(email));
        given(userService.findUserByEmail(email)).willReturn(Optional.of(mockUser));
    }

    private MockMultipartFile getMockMultipartFile(UserReportSaveRequest userReportSaveRequest) throws JsonProcessingException {
        return new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(userReportSaveRequest));
    }
}