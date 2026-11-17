package com.zhiyangyun.care.logistics;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.security.TokenProvider;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import com.zhiyangyun.care.logistics.controller.LogisticsVideoController;
import com.zhiyangyun.care.logistics.entity.*;
import com.zhiyangyun.care.logistics.mapper.LogisticsVideoRecordingMapper;
import com.zhiyangyun.care.logistics.model.*;
import com.zhiyangyun.care.logistics.service.*;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LogisticsVideoTest {
  @TempDir static Path root;
  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("app.logistics.video.local-root", () -> root.toString());
    registry.add("app.logistics.video.cleanup-cron", () -> "0 0 0 1 1 ?");
  }
  @Autowired LogisticsVideoService service;
  @Autowired LogisticsVideoController controller;
  @Autowired LogisticsVideoRecordingMapper mapper;
  @Autowired VideoStorage storage;
  @Autowired Validator validator;
  @Autowired MockMvc mvc;
  @Autowired TokenProvider tokens;

  @BeforeEach void authenticate() { auth(91001L, "LOGISTICS_MINISTER"); }
  @AfterEach void clear() { SecurityContextHolder.clearContext(); }

  private void auth(Long orgId, String role) {
    var auth = new UsernamePasswordAuthenticationToken("1", null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    auth.setDetails(orgId == null ? Map.of("roleCodes", List.of(role)) : Map.of("orgId", orgId, "roleCodes", List.of(role)));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private VideoPolicyRequest policy(int days, boolean cleanup) {
    VideoPolicyRequest request = new VideoPolicyRequest();
    request.setProvider("LOCAL"); request.setRetentionDays(days); request.setAutoCleanup(cleanup);
    return request;
  }

  private LogisticsCamera camera() {
    VideoCameraRequest request = new VideoCameraRequest();
    request.setCameraCode("CAM-01"); request.setCameraName("入口"); request.setLocation("一号楼入口");
    request.setProtocol("NVR"); request.setChannelRef("nvr01/ch01"); request.setEnabled(1); request.setBitrateMbps(new BigDecimal("2"));
    return service.saveCamera(null, request);
  }

  private MockMultipartFile mp4() {
    return new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[]{0,0,0,20,'f','t','y','p','i','s','o','m',0,0,0,0});
  }

  private LogisticsVideoRecording ingest(Long cameraId, String key) {
    LocalDateTime end = LocalDateTime.now().withNano(0).minusSeconds(5);
    return service.ingest(cameraId, key, end.minusSeconds(10), end, mp4());
  }

  @Test void retentionFloorAndUndecidedStorageAreEnforced() {
    assertEquals("UNDECIDED", service.policy().getProvider());
    assertFalse(validator.validate(policy(89, false)).isEmpty());
    assertThrows(IllegalArgumentException.class, () -> service.savePolicy(policy(89, false)));
    Long id = camera().getId();
    assertThrows(IllegalArgumentException.class, () -> ingest(id, "no-storage"));
  }

  @Test void uploadsArePrivateIdempotentAndIsolatedByOrganization() throws Exception {
    service.savePolicy(policy(90, false));
    Long cameraId = camera().getId();
    LogisticsVideoRecording recording = ingest(cameraId, "segment-01");
    assertEquals(recording.getEndedAt().plusDays(90), recording.getExpiresAt());
    assertEquals(64, recording.getSha256().length());
    assertTrue(storage.localResource(recording).exists());
    assertEquals(recording.getId(), ingest(cameraId, "segment-01").getId());
    assertEquals(1, service.recordingPage(1, 20, cameraId, null, null, null).getTotal());
    auth(91002L, "LOGISTICS_MINISTER");
    assertEquals(0, service.cameraPage(1, 20, null).getTotal());
    assertThrows(ResourceNotFoundException.class, () -> service.playback(recording.getId()));
    assertThrows(ResourceNotFoundException.class, () -> ingest(cameraId, "cross-org"));
    auth(null, "SYS_ADMIN");
    assertThrows(AccessDeniedException.class, service::overview);
  }

  @Test void extendingPolicyProtectsOldRecordingsAndShorteningDoesNotUndoIt() {
    service.savePolicy(policy(90, false));
    LogisticsVideoRecording recording = ingest(camera().getId(), "extend");
    service.savePolicy(policy(180, false));
    service.savePolicy(policy(90, false));
    assertEquals(recording.getEndedAt().plusDays(180), service.recording(recording.getId(), false).getExpiresAt());
  }

  @Test void cleanupSkipsRecentAndHeldRecordingsAndRemovesReleasedExpiredFiles() {
    service.savePolicy(policy(90, true));
    LogisticsVideoRecording recording = ingest(camera().getId(), "held");
    assertEquals(0, service.cleanup(AuthContext.getOrgId()));
    LocalDateTime old = LocalDateTime.now().minusDays(100).withNano(0);
    mapper.update(null, Wrappers.<LogisticsVideoRecording>lambdaUpdate().eq(LogisticsVideoRecording::getId, recording.getId())
        .set(LogisticsVideoRecording::getStartedAt, old.minusSeconds(10)).set(LogisticsVideoRecording::getEndedAt, old)
        .set(LogisticsVideoRecording::getExpiresAt, old.plusDays(90)));
    VideoHoldRequest hold = new VideoHoldRequest(); hold.setHeld(true); hold.setReason("事故调查");
    service.hold(recording.getId(), hold);
    assertEquals(0, service.cleanup(AuthContext.getOrgId()));
    assertTrue(storage.localResource(recording).exists());
    hold.setHeld(false); hold.setReason("调查完成"); service.hold(recording.getId(), hold);
    assertEquals(1, service.cleanup(AuthContext.getOrgId()));
    assertEquals("DELETED", service.recording(recording.getId(), false).getStatus());
    assertThrows(IllegalArgumentException.class, () -> service.playback(recording.getId()));
    assertFalse(Files.exists(root.resolve(recording.getObjectKey())));
  }

  @Test void logisticsEmployeesCannotReadRecordingsOrChangePolicy() {
    auth(91001L, "LOGISTICS_EMPLOYEE");
    assertThrows(AccessDeniedException.class, () -> controller.recordings(1, 20, null, null, null, null));
    assertThrows(AccessDeniedException.class, () -> controller.savePolicy(policy(90, false)));
    auth(91001L, "NURSING_EMPLOYEE");
    assertThrows(AccessDeniedException.class, controller::overview);
  }

  @Test void invalidAndOverlongSegmentsAreRejected() {
    service.savePolicy(policy(90, false));
    Long id = camera().getId();
    LocalDateTime end = LocalDateTime.now().minusSeconds(10);
    assertThrows(IllegalArgumentException.class, () -> service.ingest(id, "long", end.minusMinutes(10).minusSeconds(1), end, mp4()));
    assertThrows(IllegalArgumentException.class, () -> storage.put(91001L, id, "LOCAL",
        new MockMultipartFile("file", "fake.mp4", "video/mp4", "not a video".getBytes())));
  }

  @Test void httpEndpointsEnforceRolesValidatePolicyAndServePrivateContent() throws Exception {
    service.savePolicy(policy(90, false));
    LogisticsVideoRecording recording = ingest(camera().getId(), "http");
    String minister = tokens.generateToken(1L, "video-test", 91001L, List.of("LOGISTICS_MINISTER"));
    String employee = tokens.generateToken(1L, "video-test", 91001L, List.of("LOGISTICS_EMPLOYEE"));
    mvc.perform(get("/api/logistics/video/recordings").header("Authorization", "Bearer " + employee))
        .andExpect(status().isForbidden());
    mvc.perform(put("/api/logistics/video/policy").header("Authorization", "Bearer " + minister)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"provider\":\"LOCAL\",\"retentionDays\":89,\"autoCleanup\":false}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/logistics/video/recordings/" + recording.getId() + "/content")
        .header("Authorization", "Bearer " + minister))
        .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().bytes(mp4().getBytes()));
    mvc.perform(get("/api/logistics/video/recordings").header("Authorization", "Bearer " + minister))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.records[0].provider").value("LOCAL"))
        .andExpect(jsonPath("$.data.records[0].objectKey").doesNotExist());
  }
}
