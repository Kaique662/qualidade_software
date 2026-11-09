package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.auth.PortalUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortalDashboardController.class)
class PortalDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void professorSeesPersona02Workspace() throws Exception {
        MockHttpSession session = session("professor", "Marcos Vinícius Lima", PortalUser.Role.PROFESSOR);

        mockMvc.perform(get("/professor").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Marcos Vinícius Lima")))
                .andExpect(content().string(containsString("Sua agenda de aulas")))
                .andExpect(content().string(containsString("Seus horários registrados")))
                .andExpect(content().string(containsString("Solicitações de ajuste")))
                .andExpect(content().string(containsString("PERSONA 02")));
    }

    @Test
    void resourceManagerSeesPersona03Workspace() throws Exception {
        MockHttpSession session = session(
                "gestora", "Renata Costa Nascimento", PortalUser.Role.RESOURCE_MANAGER);

        mockMvc.perform(get("/gestora").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recursos cadastrados")))
                .andExpect(content().string(containsString("Cadastrar recurso")))
                .andExpect(content().string(containsString("Solicitar bloqueio")))
                .andExpect(content().string(containsString("Agenda do recurso")))
                .andExpect(content().string(containsString("PERSONA 03")));
    }

    @Test
    void adminCanOpenAcademicPlanningWorkspace() throws Exception {
        MockHttpSession session = session("admin", "Administrador", PortalUser.Role.ADMIN);

        mockMvc.perform(get("/admin/planning").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Planejamento acadêmico")))
                .andExpect(content().string(containsString("Salvar como rascunho")));
    }

    @Test
    void unauthenticatedVisitorIsSentToLogin() throws Exception {
        mockMvc.perform(get("/professor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void professorCannotOpenResourceManagerWorkspace() throws Exception {
        MockHttpSession session = session("professor", "Marcos Vinícius Lima", PortalUser.Role.PROFESSOR);

        mockMvc.perform(get("/gestora").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professor"));
    }

            @Test
            void resourceManagerCannotOpenAdminPlanningWorkspace() throws Exception {
            MockHttpSession session = session(
                "gestora", "Renata Costa Nascimento", PortalUser.Role.RESOURCE_MANAGER);

            mockMvc.perform(get("/admin/planning").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gestora"));
            }

    private MockHttpSession session(String username, String displayName, PortalUser.Role role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("portalUser", new PortalUser(username, displayName, role));
        return session;
    }
}