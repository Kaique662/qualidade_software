package com.example.educationalqualityproject.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PortalLoginController.class)
@TestPropertySource(properties = "server.forward-headers-strategy=framework")
class PortalLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortalLoginService loginService;

    @Test
    void redirectsEachRoleToItsWorkspace() throws Exception {
        assertLoginRedirect("admin", PortalUser.Role.ADMIN, "/admin");
        assertLoginRedirect("professor", PortalUser.Role.PROFESSOR, "/professor");
        assertLoginRedirect("gestora", PortalUser.Role.RESOURCE_MANAGER, "/gestora");
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        when(loginService.authenticate("admin", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/login").param("username", "admin").param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

            @Test
            void redirectsAdminWithOriginRelativeLocation() throws Exception {
            when(loginService.authenticate("admin", "secret"))
                .thenReturn(Optional.of(new PortalUser("admin", "Administrador", PortalUser.Role.ADMIN)));

            mockMvc.perform(post("/login")
                    .header("X-Forwarded-Host", "portal.example.test")
                    .header("X-Forwarded-Proto", "https")
                    .header("X-Forwarded-Port", "443")
                    .param("username", "admin")
                    .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void logoutReturnsToLoginWithOriginRelativeLocation() throws Exception {
        mockMvc.perform(post("/logout")
                        .header("X-Forwarded-Host", "portal.example.test")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Port", "443"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
            }

    private void assertLoginRedirect(String username, PortalUser.Role role, String destination) throws Exception {
        PortalUser user = new PortalUser(username, username, role);
        when(loginService.authenticate(username, "secret")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/login").param("username", username).param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(destination));
    }
}