package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.ReportFindAllResponse;
import com.swkim.safetrip.dto.response.ReportFindByIdResponse;
import com.swkim.safetrip.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.create.success", null, null)))
                .andExpect(jsonPath("$.result").value(1L))
                .andReturn();
    }

    @Test
    @DisplayName("[GET] /reports?page=0&size=10 요청시 저장된 모든 report 들을 반환한다")
    void get_report_list_with_no_condition() throws Exception {
        //given
        List<ReportFindAllResponse> listOfResponse = Arrays.asList(
                new ReportFindAllResponse("this is 1 title", "THEFT", 10),
                new ReportFindAllResponse("this is 2 title", "THEFT", 20),
                new ReportFindAllResponse("this is 3 title", "THEFT", 30)
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReportFindAllResponse> mockPage = new PageImpl<>(listOfResponse, pageable, listOfResponse.size());

        //when
        when(reportService.getReports(nullable(String.class), nullable(String.class), any(Pageable.class))).thenReturn(mockPage);

        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reports")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.list.get.success", null, null)))
                .andExpect(jsonPath("$.result.numberOfElements").value(3));
    }

    @Test
    @DisplayName("[GET] /reports?page=0&size=10&sort=likes,asc 요청시 저장된 모든 report 들을 likes 내림차순으로 반환한다")
    void get_report_list_with_Country_And_City() throws Exception {
        //given
        List<ReportFindAllResponse> listOfResponse = Arrays.asList(
                new ReportFindAllResponse("this is 1 title", "THEFT", 10),
                new ReportFindAllResponse("this is 2 title", "THEFT", 20),
                new ReportFindAllResponse("this is 3 title", "THEFT", 30)
        );
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("likes")));
        Page<ReportFindAllResponse> mockPage = new PageImpl<>(listOfResponse, pageable, listOfResponse.size());

        //when
        when(reportService.getReports(nullable(String.class), nullable(String.class), any(Pageable.class)))
                .thenAnswer(invocation -> { // 정렬 조건을 동적으로 적용하기 위해 thenReturn() 함수가 아닌 thenAnswer() 함수 사용.
                    Pageable pageableArg = invocation.getArgument(2);
                    List<ReportFindAllResponse> sortedList = listOfResponse.stream().sorted((o1, o2) -> {
                        Sort.Order sortOrder = pageableArg.getSort().getOrderFor("likes");
                        if (sortOrder != null && sortOrder.isAscending()) {
                            return Integer.compare(o1.getLikes(), o2.getLikes());
                        }
                        return Integer.compare(o2.getLikes(), o1.getLikes());
                    }).toList();
                    return new PageImpl<>(sortedList, pageableArg, sortedList.size());
                });

        //then
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/reports")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("sort", "likes,desc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(messageSource.getMessage("report.list.get.success", null, null)))
                .andExpect(jsonPath("$.result.content[0].likes").value(30))
                .andExpect(jsonPath("$.result.content[1].likes").value(20))
                .andExpect(jsonPath("$.result.content[2].likes").value(10))
                .andReturn();
    }

    @Test
    @DisplayName("[GET] /reports/{id} 요청시 저장된 report 정보를 보인다.")
    void get_report() throws Exception {

        // given
        Long id = 0L;
        ReportFindByIdResponse response = ReportFindByIdResponse.builder()
                .title("this is title")
                .category("THEFT")
                .description("this is description")
                .advice("this is my advice")
                .URLs(new ArrayList<>())
                .latitude("51.231")
                .longitude("129.141")
                .likes(13)
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