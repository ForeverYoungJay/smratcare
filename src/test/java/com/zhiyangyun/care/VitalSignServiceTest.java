package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.vital.model.VitalRecordRequest;
import com.zhiyangyun.care.vital.service.VitalSignService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VitalSignServiceTest {
  @Autowired
  private VitalSignService vitalSignService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void abnormal_detection() throws Exception {
    VitalRecordRequest request = new VitalRecordRequest();
    request.setElderId(200L);
    request.setType("TEMP");
    request.setValueJson(objectMapper.readTree("{\"temp\":38.0}"));
    request.setMeasuredAt(LocalDateTime.now());

    var response = vitalSignService.record(request);
    assertTrue(response.isAbnormalFlag());
    assertNotNull(response.getAbnormalReason());
  }

  @Test
  void query_filter_by_type() throws Exception {
    VitalRecordRequest request = new VitalRecordRequest();
    request.setElderId(200L);
    request.setType("HR");
    request.setValueJson(objectMapper.readTree("{\"hr\":80}"));
    request.setMeasuredAt(LocalDateTime.now());
    vitalSignService.record(request);

    List<?> list = vitalSignService.listByElder(200L, null, null, "HR");
    assertTrue(list.size() >= 1);
  }

  @Test
  void latest_correctness() throws Exception {
    VitalRecordRequest r1 = new VitalRecordRequest();
    r1.setElderId(200L);
    r1.setType("HR");
    r1.setValueJson(objectMapper.readTree("{\"hr\":70}"));
    r1.setMeasuredAt(LocalDateTime.now().minusMinutes(10));
    vitalSignService.record(r1);

    VitalRecordRequest r2 = new VitalRecordRequest();
    r2.setElderId(200L);
    r2.setType("HR");
    r2.setValueJson(objectMapper.readTree("{\"hr\":75}"));
    r2.setMeasuredAt(LocalDateTime.now());
    var latest = vitalSignService.record(r2);

    var fetched = vitalSignService.latest(200L);
    assertEquals(latest.getId(), fetched.getId());
  }
}
