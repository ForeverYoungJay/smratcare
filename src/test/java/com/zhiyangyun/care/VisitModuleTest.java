package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.service.VisitService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VisitModuleTest {
  @Autowired
  private VisitService visitService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void visit_code_unique() {
    VisitBookRequest r1 = new VisitBookRequest();
    r1.setOrgId(1L);
    r1.setElderId(200L);
    r1.setFamilyUserId(1L);
    r1.setVisitTime(LocalDateTime.now().plusHours(1));
    r1.setVisitTimeSlot("AM");

    VisitBookRequest r2 = new VisitBookRequest();
    r2.setOrgId(1L);
    r2.setElderId(200L);
    r2.setFamilyUserId(1L);
    r2.setVisitTime(LocalDateTime.now().plusHours(2));
    r2.setVisitTimeSlot("PM");

    var b1 = visitService.book(r1);
    var b2 = visitService.book(r2);
    assertNotEquals(b1.getVisitCode(), b2.getVisitCode());
  }

  @Test
  void guard_requires_auth() throws Exception {
    mockMvc.perform(get("/api/guard/visit/today")
            )
        .andExpect(status().isUnauthorized());
  }

  @Test
  void status_flow_on_checkin() {
    VisitBookRequest r1 = new VisitBookRequest();
    r1.setOrgId(1L);
    r1.setElderId(200L);
    r1.setFamilyUserId(1L);
    r1.setVisitTime(LocalDateTime.now().plusHours(1));
    r1.setVisitTimeSlot("AM");
    var booking = visitService.book(r1);

    var checkin = visitService.checkIn(new com.zhiyangyun.care.visit.model.VisitCheckInRequest() {{
      setOrgId(1L);
      setVisitCode(booking.getVisitCode());
    }}, 500L);

    assertEquals(1, checkin.getStatus());
  }

  @Test
  void duplicate_slot_for_same_elder_is_rejected() {
    VisitBookRequest first = new VisitBookRequest();
    first.setOrgId(1L);
    first.setElderId(200L);
    first.setFamilyUserId(1L);
    first.setVisitTime(LocalDateTime.now().plusHours(3));
    first.setVisitTimeSlot("AM");
    visitService.book(first);

    VisitBookRequest duplicate = new VisitBookRequest();
    duplicate.setOrgId(1L);
    duplicate.setElderId(200L);
    duplicate.setFamilyUserId(1L);
    duplicate.setVisitTime(first.getVisitTime().plusMinutes(20));
    duplicate.setVisitTimeSlot("AM");

    assertThrows(IllegalStateException.class, () -> visitService.book(duplicate));
  }

  @Test
  void checkin_requires_valid_visit_code_when_provided() {
    VisitBookRequest request = new VisitBookRequest();
    request.setOrgId(1L);
    request.setElderId(200L);
    request.setFamilyUserId(1L);
    request.setVisitTime(LocalDateTime.now().plusHours(4));
    request.setVisitTimeSlot("PM");
    var booking = visitService.book(request);

    assertThrows(IllegalArgumentException.class, () -> visitService.checkIn(new com.zhiyangyun.care.visit.model.VisitCheckInRequest() {{
      setOrgId(1L);
      setBookingId(booking.getId());
      setVisitCode("WRONG-CODE");
    }}, 500L));
  }
}
