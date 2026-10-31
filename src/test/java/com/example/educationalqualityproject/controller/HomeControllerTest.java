package com.example.educationalqualityproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HomeControllerTest {

    @Test
    void shouldReturnHomeView() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HomeController()).build();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}