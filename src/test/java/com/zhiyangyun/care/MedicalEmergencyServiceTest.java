package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.medicalcare.entity.MedicalEmergencyEvent;
import com.zhiyangyun.care.medicalcare.entity.MedicalRescueRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyEventRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalEmergencyTransitionRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRescueRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalEmergencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MedicalEmergencyServiceTest {

  private static final Long ORG_ID = 1L;
  private static final Long ELDER_ID = 4001L;

  @Autowired
  private MedicalEmergencyService emergencyService;

  @Test
  void transition_followsStateMachine_andRejectsInvalidAction() {
    MedicalEmergencyEventRequest createRequest = new MedicalEmergencyEventRequest();
    createRequest.setElderId(ELDER_ID);
    createRequest.setLocation("2号楼301");
    createRequest.setSymptom("突发胸痛");
    MedicalEmergencyEvent event = emergencyService.createEvent(ORG_ID, createRequest);
    assertEquals("INITIATED", event.getStatus());

    // 未呼叫 120 前不允许直接送医
    MedicalEmergencyTransitionRequest depart = new MedicalEmergencyTransitionRequest();
    depart.setAction("DEPART");
    depart.setHospitalName("市第一医院");
    assertThrows(IllegalArgumentException.class,
        () -> emergencyService.transition(ORG_ID, event.getId(), depart));

    MedicalEmergencyTransitionRequest call = new MedicalEmergencyTransitionRequest();
    call.setAction("CALL");
    assertEquals("CALLED", emergencyService.transition(ORG_ID, event.getId(), call).getStatus());

    assertEquals("TRANSFERRED", emergencyService.transition(ORG_ID, event.getId(), depart).getStatus());

    MedicalEmergencyTransitionRequest outcome = new MedicalEmergencyTransitionRequest();
    outcome.setAction("OUTCOME");
    outcome.setOutcome("HOSPITALIZED");
    outcome.setOutcomeNote("入院观察");
    MedicalEmergencyEvent afterOutcome = emergencyService.transition(ORG_ID, event.getId(), outcome);
    assertEquals("HOSPITALIZED", afterOutcome.getStatus());
    assertEquals("HOSPITALIZED", afterOutcome.getOutcome());

    MedicalEmergencyTransitionRequest close = new MedicalEmergencyTransitionRequest();
    close.setAction("CLOSE");
    assertEquals("CLOSED", emergencyService.transition(ORG_ID, event.getId(), close).getStatus());
  }

  @Test
  void saveRescueRecord_upsertsSingleRecordPerEvent() {
    MedicalEmergencyEventRequest createRequest = new MedicalEmergencyEventRequest();
    createRequest.setElderId(ELDER_ID);
    createRequest.setSymptom("意识不清");
    MedicalEmergencyEvent event = emergencyService.createEvent(ORG_ID, createRequest);

    MedicalRescueRecordRequest rescueRequest = new MedicalRescueRecordRequest();
    rescueRequest.setEventId(event.getId());
    rescueRequest.setTimelineJson("[{\"time\":\"10:02\",\"action\":\"胸外按压\"}]");
    rescueRequest.setParticipants("张医生、李护士");
    rescueRequest.setMeasures("胸外按压、吸氧");
    rescueRequest.setResult("SUCCESS");
    MedicalRescueRecord first = emergencyService.saveRescueRecord(ORG_ID, rescueRequest);
    assertNotNull(first.getId());

    rescueRequest.setDrugsUsed("肾上腺素 1mg");
    MedicalRescueRecord second = emergencyService.saveRescueRecord(ORG_ID, rescueRequest);
    assertEquals(first.getId(), second.getId());
    assertEquals("肾上腺素 1mg", second.getDrugsUsed());
  }
}
