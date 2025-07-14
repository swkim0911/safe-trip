package com.swkim.safetrip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.config.SecurityConfig;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import com.swkim.safetrip.global.exception.custom.InvalidAccessTokenException;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.ReportService;
import com.swkim.safetrip.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("[Post] /reports 요청시 저장된 report의 id를 반환한다")
    @WithMockUser(username = "testuser")
    @SuppressWarnings("unchecked")
    void scam_보고서_등록_성공시_id를_반환해야한다() throws Exception {

        // given
        ReportSaveRequest reportSaveRequest = getMockReportSaveRequest();

        MockMultipartFile request = getMockMultipartFile(reportSaveRequest);
        MockMultipartFile images = new MockMultipartFile("images", "my_image.jpg", MediaType.IMAGE_JPEG_VALUE, "this is image".getBytes());

        String accessToken = "im.access.token";
        String email = "test@gmail.com";
        User mockUser = User.builder()
                .email(email)
                .password("password")
                .nickname("nickname")
                .role(Role.USER).build();

        given(jwtUtils.extractAccessToken(any())).willReturn(Optional.of(accessToken));
        doNothing().when(jwtUtils).validateAccessToken(accessToken);
        given(jwtUtils.extractEmail(eq(accessToken))).willReturn(Optional.of(email));
        given(userService.getUserByEmail(email)).willReturn(mockUser);
        given(reportService.saveReport(any(ReportSaveRequest.class), anyList())).willReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/reports")
                .file(images)
                .file(request)
                .header("Authorization", "Bearer valid.token.here"));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.create.success", null, null)))
                .andExpect(jsonPath("$.result").value(1L));

    }

    @Test
    @DisplayName("[GET] /reports/{id} 요청시 저장된 report 정보를 보인다.")
    void 보고서_id_조회시_보고서_정보를_보인다() throws Exception {

        // given
        Long id = 0L;
        ReportFindByIdResponse response = ReportFindByIdResponse.builder()
                .title("this is title")
                .scam("THEFT")
                .lat("51.231")
                .lng("129.141")
                .address("대한민국 서울시 남산타워")
                .URLs(new ArrayList<>())
                .description("this is description")
                .advice("this is my advice")
                .build();

        // when
        when(reportService.getReport(id)).thenReturn(response);
        // then
        mockMvc.perform(get("/reports/" + "{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.get.success", null, null)));
    }

    @Test
    void 글_등록_요청에_액세스_토큰이_없는_경우_401_에러가_발생한다() throws Exception {
        // given
        ReportSaveRequest reportSaveRequest = getMockReportSaveRequest();
        MockMultipartFile request = getMockMultipartFile(reportSaveRequest);

        // when & then
        mockMvc.perform(multipart("/reports")
                .file(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Access token is missing"));
    }


    @Test
    void 글_등록_요청에_액세스_토큰이_invalid한_경우_401_에러가_발생한다() throws Exception {
        // given
        ReportSaveRequest reportSaveRequest = getMockReportSaveRequest();
        MockMultipartFile request = getMockMultipartFile(reportSaveRequest);
        String invalidToken = "Bearer im.invalid.token";

        given(jwtUtils.extractAccessToken(any(HttpServletRequest.class)))
                .willReturn(Optional.of(invalidToken));

        doThrow(new InvalidAccessTokenException()).when(jwtUtils).validateAccessToken(invalidToken);

        // when & then
        mockMvc.perform(multipart("/reports")
                        .file(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Access token is invalid"));
    }

    private ReportSaveRequest getMockReportSaveRequest() {
        String title = "this is title";
        Long scamId = 1L;
        String address = "대한민국 서울시 남산타워";
        String lat = "37.56711260434211";
        String lng = "126.97911625963219";
        String country = "Korea";
        String city = "Seoul";
        String description = "this is description";
        String advice = "this is advice";

        return ReportSaveRequest.builder()
                .title(title)
                .scamId(scamId)
                .address(address)
                .lat(lat)
                .lng(lng)
                .country(country)
                .city(city)
                .description(description)
                .advice(advice).build();
    }

    private MockMultipartFile getMockMultipartFile(ReportSaveRequest reportSaveRequest) throws JsonProcessingException {
        return new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(reportSaveRequest));
    }
}