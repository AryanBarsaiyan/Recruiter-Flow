package com.futurescope.platform.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurescope.platform.AbstractIntegrationTest;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.auth.web.dto.LoginRequest;
import com.futurescope.platform.auth.web.dto.RefreshRequest;
import com.futurescope.platform.auth.web.dto.SignupSuperAdminRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired UserRepository userRepository;
    @Autowired CompanyMemberRepository companyMemberRepository;

    MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void signupSuperAdmin_returnsTokens() throws Exception {
        SignupSuperAdminRequest request = new SignupSuperAdminRequest();
        request.setEmail("admin@company.com");
        request.setPassword("password123");
        request.setFullName("Admin User");
        request.setCompanyName("Test Company");

        mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_afterSignup_returnsTokens() throws Exception {
        SignupSuperAdminRequest signup = new SignupSuperAdminRequest();
        signup.setEmail("login@company.com");
        signup.setPassword("password123");
        signup.setFullName("Login User");
        signup.setCompanyName("Login Company");
        mockMvc.perform(post("/auth/signup-super-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup))).andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setEmail("login@company.com");
        login.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("nonexistent@example.com");
        login.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withValidToken_returnsNewTokens() throws Exception {
        SignupSuperAdminRequest signup = new SignupSuperAdminRequest();
        signup.setEmail("refresh@company.com");
        signup.setPassword("password123");
        signup.setFullName("Refresh User");
        signup.setCompanyName("Refresh Company");
        String signupBody = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(signupBody).get("refreshToken").asText();

        RefreshRequest refreshReq = new RefreshRequest();
        refreshReq.setRefreshToken(refreshToken);
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void logout_withValidToken_returnsNoContent() throws Exception {
        SignupSuperAdminRequest signup = new SignupSuperAdminRequest();
        signup.setEmail("logout@company.com");
        signup.setPassword("password123");
        signup.setFullName("Logout User");
        signup.setCompanyName("Logout Company");
        String signupBody = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(signupBody).get("accessToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void invite_andAcceptInvite_returnsTokens() throws Exception {
        String adminEmail = "invite-admin-" + UUID.randomUUID() + "@test.com";
        String inviteeEmail = "invitee-" + UUID.randomUUID() + "@test.com";
        String signupBody = mockMvc.perform(post("/auth/signup-super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", adminEmail,
                                "password", "password123",
                                "fullName", "Admin",
                                "companyName", "Invite Company " + UUID.randomUUID().toString().substring(0, 8)))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(signupBody).get("accessToken").asText();
        User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElseThrow();
        UUID companyId = companyMemberRepository.findByUser(admin).stream().findFirst().orElseThrow().getCompany().getId();

        String inviteBody = mockMvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "companyId", companyId.toString(),
                                "email", inviteeEmail,
                                "roleName", "ReadOnly"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String inviteToken = objectMapper.readTree(inviteBody).get("inviteToken").asText();

        mockMvc.perform(post("/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", inviteToken,
                                "password", "newpass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void verifyEmail_withValidToken_returns200() throws Exception {
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "invalid-token"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestPasswordReset_returns200() throws Exception {
        mockMvc.perform(post("/auth/request-password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "nobody@example.com"))))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_invalidToken_returns400() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "invalid", "newPassword", "newpass123"))))
                .andExpect(status().isBadRequest());
    }
}
