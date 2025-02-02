package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ReportController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("[Post] /reports 요청시 저장된 report의 id를 반환한다")
    @SuppressWarnings("unchecked")
    void saving_report() throws Exception {

        // given
        String latitude = "37.56711260434211";
        String longitude = "126.97911625963219";
        String title = "this is title";
        String category = "THEFT";
        String description = "this is description";
        String advice = "this is advice";

        ReportSaveRequest reportSaveRequest = ReportSaveRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .title(title)
                .category(category)
                .description(description)
                .advice(advice).build();

        MockMultipartFile jsonRequest = new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(reportSaveRequest));
        MockMultipartFile image = new MockMultipartFile("image", "my_image.jpg", MediaType.IMAGE_JPEG_VALUE, "this is image".getBytes());

        // when
        when(reportService.saveReport(any(), nullable(List.class))).thenReturn(1L);

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/reports")
                        .file(image)
                        .file(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("report 등록이 완료되었습니다."))
                .andExpect(jsonPath("$.result").value(1L));
    }

    @Test
    @DisplayName("[GET] /reports 요청시 저장된 report li")
    void get_report_list() {

    }

}