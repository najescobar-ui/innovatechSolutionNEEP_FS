package com.duoc.bff.web;

import com.duoc.bff.service.KpisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpisControllerTest {

    @Mock KpisService service;
    @InjectMocks KpisController controller;

    @Test
    void get_delegates() {
        when(service.get()).thenReturn(Map.of("status", "ok"));
        assertThat(controller.get()).containsEntry("status", "ok");
    }

    @Test
    void history_delegates() {
        when(service.history(12, null, null)).thenReturn(Map.of("status", "ok"));
        assertThat(controller.history(12, null, null)).containsEntry("status", "ok");
    }
}
