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
      setBookingId(booking.getId());
    }}, 500L);

    assertEquals(1, checkin.getStatus());
  }
}
