package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.SafetripApplication;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("[Post] /reports 요청시 저장된 report의 id를 반환한다")
    @SuppressWarnings("unchecked")
    void scam_보고서_등록_성공시_id를_반환해야한다() throws Exception {

        // given
        String title = "this is title";
        Long scamId = 1L;
        String address = "대한민국 서울시 남산타워";
        String lat = "37.56711260434211";
        String lng = "126.97911625963219";
        String country = "Korea";
        String city = "Seoul";
        String description = "this is description";
        String advice = "this is advice";

        ReportSaveRequest reportSaveRequest = ReportSaveRequest.builder()
                .title(title)
                .scamId(scamId)
                .address(address)
                .lat(lat)
                .lng(lng)
                .country(country)
                .city(city)
                .description(description)
                .advice(advice).build();

        MockMultipartFile jsonRequest = new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(reportSaveRequest));
        MockMultipartFile image = new MockMultipartFile("image", "my_image.jpg", MediaType.IMAGE_JPEG_VALUE, "this is image".getBytes());

        // when
        when(reportService.saveReport(any(), nullable(List.class))).thenReturn(1L);

        // then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/reports")
                        .file(image)
                        .file(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.create.success", null, null)))
                .andExpect(jsonPath("$.result").value(1L))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        System.out.println("contentAsString = " + contentAsString);
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
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reports/" + "{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.get.success", null, null)));
    }
}