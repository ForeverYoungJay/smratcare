package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.nursing.controller.CareIncidentController;
import com.zhiyangyun.care.nursing.controller.NursingQualityController;
import com.zhiyangyun.care.nursing.controller.NursingRectificationController;
import com.zhiyangyun.care.nursing.controller.ServicePlanController;
import com.zhiyangyun.care.nursing.controller.ShiftHandoverController;
import com.zhiyangyun.care.nursing.entity.ServicePlan;
import com.zhiyangyun.care.nursing.mapper.ServicePlanMapper;
import com.zhiyangyun.care.nursing.model.CareIncidentActionRequest;
import com.zhiyangyun.care.nursing.model.CareIncidentDetailResponse;
import com.zhiyangyun.care.nursing.model.CareIncidentRequest;
import com.zhiyangyun.care.nursing.model.NursingQualityMetricsResponse;
import com.zhiyangyun.care.nursing.model.NursingRectificationRequest;
import com.zhiyangyun.care.nursing.model.NursingRectificationReviewRequest;
import com.zhiyangyun.care.nursing.model.ServicePlanRequest;
import com.zhiyangyun.care.nursing.model.ShiftHandoverRequest;
import com.zhiyangyun.care.nursing.model.ShiftHandoverResponse;
import com.zhiyangyun.care.nursing.service.ServiceBookingService;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** 护理部升级：异常事件流转、交接班双方确认、计划审核版本、整改复核、质控指标。 */
@SpringBootTest
@ActiveProfiles("test")
class NursingIncidentFlowTest {
  private static final Long ORG_ID = 9921L;
  private static final Long MINISTER_ID = 501L;
  private static final Long NURSE_ID = 502L;
  private static final Long OTHER_NURSE_ID = 503L;

  @Autowired private CareIncidentController incidentController;
  @Autowired private ShiftHandoverController handoverController;
  @Autowired private ServicePlanController planController;
  @Autowired private NursingRectificationController rectificationController;
  @Autowired private NursingQualityController qualityController;
  @Autowired private ServicePlanMapper planMapper;
  @Autowired private ServiceBookingService bookingService;
  // H2 测试库没有 service_item 表；计划/预约只用它补名称和时长
  @MockBean private ServiceItemMapper serviceItemMapper;
  @Autowired private JdbcTemplate jdbc;

  /** 护士长账号：用于验证待办通知和真实姓名 */
  private void seedMinister() {
    jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name) KEY(id) "
        + "VALUES (?, ?, 1, 'N501', 'u501', 'x', '周护士长')", MINISTER_ID, ORG_ID);
    jdbc.update("MERGE INTO role (id, org_id, role_name, role_code) KEY(id) VALUES (99210, ?, '护士长', 'NURSING_MINISTER')", ORG_ID);
    jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (99211, ?, ?, 99210)", ORG_ID, MINISTER_ID);
  }

  private List<Map<String, Object>> todos(String incidentNo) {
    return jdbc.queryForList("SELECT assignee_id, status FROM oa_todo WHERE title LIKE ?", "【护理异常】" + incidentNo + " %");
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  private void loginAs(Long staffId, String role) {
    loginAs(staffId, role, ORG_ID);
  }

  private void loginAs(Long staffId, String role, Long orgId) {
    var auth = new UsernamePasswordAuthenticationToken(String.valueOf(staffId), null,
        AuthorityUtils.createAuthorityList("ROLE_" + role));
    auth.setDetails(Map.of("orgId", orgId, "username", "u" + staffId, "roleCodes", List.of(role)));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private Long reportIncident(Long elderId) {
    CareIncidentRequest request = new CareIncidentRequest();
    request.setIncidentType("FALL");
    request.setSeverity("HIGH");
    request.setOccurredAt(LocalDateTime.now());
    request.setDescription("卫生间跌倒");
    request.setElderId(elderId);
    request.setElderName("测试长者");
    Result<CareIncidentDetailResponse> result = incidentController.create(request);
    assertTrue(result.isSuccess(), result.getMessage());
    assertTrue(result.getData().getIncident().getIncidentNo().matches("YC\\d{8}\\d{3}"));
    assertEquals("REPORT", result.getData().getActions().get(0).getAction());
    return result.getData().getIncident().getId();
  }

  private Result<CareIncidentDetailResponse> act(Long id, String action, String note) {
    CareIncidentActionRequest request = new CareIncidentActionRequest();
    request.setAction(action);
    request.setNote(note);
    return incidentController.act(id, request);
  }

  @Test
  void incident_notifies_minister_and_closes_todos() {
    seedMinister();
    loginAs(NURSE_ID, "NURSING_EMPLOYEE");
    Long id = reportIncident(88101L);
    String no = incidentController.get(id).getData().getIncident().getIncidentNo();
    // 上报 → 护士长收到待受理待办
    assertEquals(1, todos(no).size());
    assertEquals(MINISTER_ID, ((Number) todos(no).get(0).get("ASSIGNEE_ID")).longValue());
    act(id, "ACCEPT", null);
    act(id, "PROCESS", null);
    act(id, "SUBMIT_REVIEW", "已处理");
    assertEquals(2, todos(no).size());

    loginAs(MINISTER_ID, "NURSING_MINISTER");
    Result<CareIncidentDetailResponse> closed = act(id, "CLOSE", "复核通过");
    // 操作人显示真实姓名；结案后相关待办全部完成
    assertEquals("周护士长", closed.getData().getIncident().getReviewerName());
    assertTrue(todos(no).stream().allMatch(t -> "DONE".equals(t.get("STATUS"))));
  }

  @Test
  void incident_happy_path_and_rules() {
    loginAs(NURSE_ID, "NURSING_EMPLOYEE");
    long closedBefore = incidentController.summary(null).getData().getClosedCount();
    Long id = reportIncident(88001L);
    reportIncident(88002L);
    assertEquals(1, incidentController.page(1, 20, null, null, null, null, null, null, 88001L, null)
        .getData().getRecords().size());

    // 非法流转
    Result<CareIncidentDetailResponse> invalid = act(id, "SUBMIT_REVIEW", "x");
    assertEquals(400, invalid.getCode());
    assertEquals("当前状态不允许该操作", invalid.getMessage());

    assertEquals("ACCEPTED", act(id, "ACCEPT", null).getData().getIncident().getStatus());
    assertEquals("PROCESSING", act(id, "PROCESS", null).getData().getIncident().getStatus());
    assertEquals(400, act(id, "SUBMIT_REVIEW", null).getCode());

    // 更正只追加记录，不改状态
    Result<CareIncidentDetailResponse> corrected = act(id, "CORRECT", "发生时间更正为 10:05");
    assertEquals("PROCESSING", corrected.getData().getIncident().getStatus());
    assertEquals("CORRECT", corrected.getData().getActions().get(corrected.getData().getActions().size() - 1).getAction());

    assertEquals("PENDING_REVIEW", act(id, "SUBMIT_REVIEW", "已送医复查，无骨折").getData().getIncident().getStatus());

    // 护理员不能关闭
    assertEquals(403, act(id, "CLOSE", "ok").getCode());

    loginAs(MINISTER_ID, "NURSING_MINISTER");
    Result<CareIncidentDetailResponse> closed = act(id, "CLOSE", "处置到位");
    assertEquals("CLOSED", closed.getData().getIncident().getStatus());
    assertEquals(MINISTER_ID, closed.getData().getIncident().getReviewerId());
    assertNotNull(closed.getData().getIncident().getClosedAt());
    assertEquals(List.of("REPORT", "ACCEPT", "PROCESS", "CORRECT", "SUBMIT_REVIEW", "CLOSE"),
        incidentController.get(id).getData().getActions().stream().map(a -> a.getAction()).toList());
    assertEquals(closedBefore + 1, incidentController.summary(null).getData().getClosedCount());
  }

  @Test
  void handover_confirm_only_by_receiver() {
    loginAs(NURSE_ID, "NURSING_EMPLOYEE");
    ShiftHandoverRequest request = new ShiftHandoverRequest();
    request.setDutyDate(LocalDate.now());
    request.setShiftCode("DAY");
    request.setFromStaffId(NURSE_ID);
    request.setToStaffId(OTHER_NURSE_ID);
    request.setStatus("CONFIRMED");
    Result<ShiftHandoverResponse> rejected = handoverController.create(request);
    assertEquals(400, rejected.getCode());
    assertEquals("接班确认须由接班人本人点击确认", rejected.getMessage());

    request.setStatus("HANDED_OVER");
    ShiftHandoverResponse created = handoverController.create(request).getData();
    assertNotNull(created.getHandoverTime());
    request.setStatus("CONFIRMED");
    assertEquals(400, handoverController.update(created.getId(), request).getCode());

    assertEquals(403, handoverController.confirm(created.getId()).getCode());
    loginAs(OTHER_NURSE_ID, "NURSING_EMPLOYEE");
    ShiftHandoverResponse confirmed = handoverController.confirm(created.getId()).getData();
    assertEquals("CONFIRMED", confirmed.getStatus());
    assertNotNull(confirmed.getConfirmTime());

    request.setStatus("HANDED_OVER");
    assertThrows(IllegalStateException.class, () -> handoverController.update(created.getId(), request));
  }

  @Test
  void published_plan_update_creates_revision_and_needs_approval() {
    loginAs(MINISTER_ID, "NURSING_MINISTER");
    ServicePlanRequest request = new ServicePlanRequest();
    request.setElderId(77001L);
    request.setServiceItemId(77002L);
    request.setPlanName("翻身拍背");
    request.setStartDate(LocalDate.now().minusDays(1));
    Long planId = planController.create(request).getData().getId();
    assertEquals("PENDING", planController.get(planId).getData().getApprovalStatus());
    // 新建未审核的计划不生成预约
    assertEquals(0, bookingService.generateFromPlans(ORG_ID, LocalDate.now(), false));

    assertEquals("PUBLISHED", planController.approve(planId).getData().getApprovalStatus());
    assertEquals(400, planController.approve(planId).getCode());

    request.setPlanName("翻身拍背（加频）");
    request.setFrequency(3);
    request.setChangeReason("压疮风险升高");
    planController.update(planId, request);
    ServicePlan plan = planMapper.selectById(planId);
    assertEquals(2, plan.getRevision());
    assertEquals("PENDING", plan.getApprovalStatus());
    var revisions = planController.revisions(planId).getData();
    assertEquals(1, revisions.size());
    assertEquals(1, revisions.get(0).getRevision());
    assertTrue(revisions.get(0).getSnapshotJson().contains("翻身拍背"));
    assertEquals("压疮风险升高", revisions.get(0).getChangeReason());

    // 审核信息只属于当前版本，修改后清空
    assertNull(plan.getApprovedBy());
    assertNull(plan.getApprovedAt());

    // 修订待审期间按上一次发布的版本（每天 1 次）生成，不用修改后的每天 3 次
    bookingService.generateFromPlans(ORG_ID, LocalDate.now(), false);
    assertEquals(1, jdbc.queryForObject(
        "SELECT COUNT(*) FROM service_booking WHERE plan_id = ? AND is_deleted = 0", Integer.class, planId));

    assertEquals(400, planController.reject(planId, Map.of()).getCode());
    assertEquals("REJECTED", planController.reject(planId, Map.of("reason", "频次过高")).getData().getApprovalStatus());
    assertEquals("频次过高", planController.get(planId).getData().getRejectReason());
    planController.update(planId, request);
    assertEquals("PENDING", planController.get(planId).getData().getApprovalStatus());
    assertNull(planController.get(planId).getData().getRejectReason());
    assertEquals("PUBLISHED", planController.approve(planId).getData().getApprovalStatus());
  }

  @Test
  void rectification_review_pass_and_fail() {
    loginAs(MINISTER_ID, "NURSING_MINISTER");
    NursingRectificationRequest create = new NursingRectificationRequest();
    create.setTitle("夜班巡房记录缺失");
    create.setOwnerId(NURSE_ID);
    create.setOwnerName("护理员");
    create.setDueDate(LocalDate.now().plusDays(3));
    Long id = rectificationController.create(create).getData().getId();

    NursingRectificationReviewRequest review = new NursingRectificationReviewRequest();
    review.setPassed(true);
    assertEquals(400, rectificationController.review(id, review).getCode());

    // 负责人提交证据
    loginAs(NURSE_ID, "NURSING_EMPLOYEE");
    NursingRectificationRequest evidence = new NursingRectificationRequest();
    evidence.setEvidence("已补签");
    evidence.setStatus("PENDING_REVIEW");
    assertEquals("PENDING_REVIEW", rectificationController.update(id, evidence).getData().getStatus());
    loginAs(OTHER_NURSE_ID, "NURSING_EMPLOYEE");
    assertEquals(403, rectificationController.update(id, evidence).getCode());

    loginAs(MINISTER_ID, "NURSING_MINISTER");
    review.setPassed(false);
    review.setReviewResult("签字不全");
    assertEquals("IN_PROGRESS", rectificationController.review(id, review).getData().getStatus());

    loginAs(NURSE_ID, "NURSING_EMPLOYEE");
    rectificationController.update(id, evidence);
    loginAs(MINISTER_ID, "NURSING_MINISTER");
    review.setPassed(true);
    var done = rectificationController.review(id, review).getData();
    assertEquals("DONE", done.getStatus());
    assertNotNull(done.getClosedAt());
  }

  @Test
  void metrics_rate_null_when_no_data() {
    loginAs(MINISTER_ID, "NURSING_MINISTER", 99299L);
    NursingQualityMetricsResponse metrics = qualityController.metrics("2026-01-01", "2026-01-31").getData();
    assertEquals(5, metrics.getMetrics().size());
    metrics.getMetrics().forEach(m -> {
      assertEquals(0, m.getDenominator());
      assertNull(m.getRate());
    });
  }
}
