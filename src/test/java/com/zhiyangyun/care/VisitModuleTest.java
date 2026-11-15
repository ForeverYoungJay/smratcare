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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VisitModuleTest {
  @Autowired
  private VisitService visitService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private VisitBookRequest bookingRequest(LocalDateTime visitTime, String timeSlot) {
    VisitBookRequest request = new VisitBookRequest();
    request.setOrgId(1L);
    request.setElderId(200L);
    request.setFamilyUserId(1L);
    request.setVisitorName("测试家属");
    request.setVisitorPhone("13000000012");
    request.setVisitorRelation("子女");
    request.setVisitTime(visitTime);
    request.setVisitTimeSlot(timeSlot);
    return request;
  }

  @Test
  void visit_code_unique() {
    VisitBookRequest r1 = bookingRequest(LocalDateTime.now().plusHours(1), "AM");
    VisitBookRequest r2 = bookingRequest(LocalDateTime.now().plusHours(2), "PM");

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
    VisitBookRequest r1 = bookingRequest(LocalDateTime.now().plusHours(1), "AM");
    var booking = visitService.book(r1);

    var checkin = visitService.checkIn(new com.zhiyangyun.care.visit.model.VisitCheckInRequest() {{
      setOrgId(1L);
      setVisitCode(booking.getVisitCode());
    }}, 500L);

    assertEquals(1, checkin.getStatus());
  }

  @Test
  void duplicate_slot_for_same_elder_is_rejected() {
    VisitBookRequest first = bookingRequest(LocalDateTime.now().plusHours(3), "AM");
    visitService.book(first);

    VisitBookRequest duplicate = bookingRequest(first.getVisitTime().plusMinutes(20), "AM");

    assertThrows(IllegalStateException.class, () -> visitService.book(duplicate));
  }

  @Test
  void checkin_requires_valid_visit_code_when_provided() {
    VisitBookRequest request = bookingRequest(LocalDateTime.now().plusHours(4), "PM");
    var booking = visitService.book(request);

    assertThrows(IllegalArgumentException.class, () -> visitService.checkIn(new com.zhiyangyun.care.visit.model.VisitCheckInRequest() {{
      setOrgId(1L);
      setBookingId(booking.getId());
      setVisitCode("WRONG-CODE");
    }}, 500L));
  }
}
