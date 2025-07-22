package com.swkim.safetrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swkim.safetrip.config.SecurityConfig;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.jwt.JwtUtils;
import com.swkim.safetrip.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void 회원가입_요청_성공시_201_응답을_반환한다() throws Exception {
        //given
        UserSignUpRequest signUpRequest = UserSignUpRequest.builder()
                .email("swkim@gmail.com")
                .password("pass!word1")
                .nickname("nickname")
                .build();

        given(userService.signup(signUpRequest)).willReturn(1L);

        //when
        ResultActions resultActions = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(1L));
    }

    @Test
    void 회원가입_요청시_비밀번호_검증에_통과하지_못하면_예외가_발생한다() throws Exception {
        //given
        UserSignUpRequest signUpRequest = UserSignUpRequest.builder()
                .email("swkim@gmail.com")
                .password("invalid123")
                .nickname("nickname")
                .build();

        //when
        ResultActions resultActions = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    @Test
    void 회원가입_요청시_닉네임이_검증에_통과하지_못하면_예외가_발생한다() throws Exception {
        //given
        UserSignUpRequest signUpRequest = UserSignUpRequest.builder()
                .email("swkim@gmail.com")
                .password("pass@word1")
                .nickname("~!")
                .build();

        //when
        ResultActions resultActions = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        //then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    @Test
    void 존재하지_않는_URL_요청시_404응답이_반환된다() throws Exception {

        mockMvc.perform(post("/wrong-url"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("API does not exist"));

    }
}
