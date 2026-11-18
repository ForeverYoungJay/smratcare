package com.zhiyangyun.care.fire.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.file.service.FileStorageService;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import com.zhiyangyun.care.fire.entity.FireCheckTemplate;
import com.zhiyangyun.care.fire.entity.FireDutyHandover;
import com.zhiyangyun.care.fire.entity.FireLocation;
import com.zhiyangyun.care.fire.entity.FireReportSignature;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.entity.FireTrainingDrill;
import com.zhiyangyun.care.fire.mapper.FireCheckTemplateMapper;
import com.zhiyangyun.care.fire.mapper.FireDutyHandoverMapper;
import com.zhiyangyun.care.fire.mapper.FireLocationMapper;
import com.zhiyangyun.care.fire.mapper.FireReportSignatureMapper;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.fire.mapper.FireTrainingDrillMapper;
import com.zhiyangyun.care.fire.service.FireRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消防运营：楼栋点位与检查模板（主管配置）、控制室交接双方确认、维保报告签署、培训演练台账、楼栋风险。
 */
@RestController
@RequestMapping("/api/fire")
@PreAuthorize(FireOpsController.FIRE_ROLES)
public class FireOpsController {
  static final String FIRE_ROLES = "hasAnyRole('GUARD','LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  static final String FIRE_MANAGE = "hasAnyRole('LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final Set<String> TEMPLATE_TYPES = Set.of("DAY_PATROL", "NIGHT_PATROL", "MONTHLY_CHECK");
  private static final Set<String> RISK_LEVELS = Set.of("HIGH", "MEDIUM", "LOW");

  private final FireLocationMapper locations;
  private final FireCheckTemplateMapper templates;
  private final FireDutyHandoverMapper handovers;
  private final FireReportSignatureMapper signatures;
  private final FireTrainingDrillMapper drills;
  private final FireSafetyRecordMapper records;
  private final StaffMapper staffMapper;
  private final FileStorageService fileStorage;

  public FireOpsController(FireLocationMapper locations, FireCheckTemplateMapper templates, FireDutyHandoverMapper handovers,
      FireReportSignatureMapper signatures, FireTrainingDrillMapper drills, FireSafetyRecordMapper records,
      StaffMapper staffMapper, FileStorageService fileStorage) {
    this.locations = locations;
    this.templates = templates;
    this.handovers = handovers;
    this.signatures = signatures;
    this.drills = drills;
    this.records = records;
    this.staffMapper = staffMapper;
    this.fileStorage = fileStorage;
  }

  // ---------- 楼栋点位（配置） ----------

  @GetMapping("/locations")
  public Result<List<FireLocation>> locations() {
    return Result.ok(locations.selectList(Wrappers.lambdaQuery(FireLocation.class)
        .eq(FireLocation::getOrgId, org()).eq(FireLocation::getIsDeleted, 0)
        .orderByAsc(FireLocation::getBuilding).orderByAsc(FireLocation::getFloorName)));
  }

  @PostMapping("/locations") @PreAuthorize(FIRE_MANAGE)
  public Result<FireLocation> saveLocation(@RequestBody FireLocation body) {
    if (body.getBuilding() == null || body.getBuilding().isBlank()) throw new IllegalArgumentException("请填写楼栋");
    String risk = body.getRiskLevel() == null ? "MEDIUM" : body.getRiskLevel().trim().toUpperCase();
    if (!RISK_LEVELS.contains(risk)) throw new IllegalArgumentException("风险等级只支持 HIGH/MEDIUM/LOW");
    FireLocation target = body.getId() == null ? new FireLocation() : ownedLocation(body.getId());
    target.setOrgId(org());
    target.setBuilding(body.getBuilding().trim());
    target.setFloorName(body.getFloorName());
    target.setArea(body.getArea());
    target.setPointCode(body.getPointCode());
    target.setRiskLevel(risk);
    target.setKeyPart(Integer.valueOf(1).equals(body.getKeyPart()) ? 1 : 0);
    target.setEnabled(Integer.valueOf(0).equals(body.getEnabled()) ? 0 : 1);
    target.setRemark(body.getRemark());
    target.setUpdateTime(LocalDateTime.now());
    if (target.getId() == null) {
      target.setIsDeleted(0);
      target.setCreateTime(LocalDateTime.now());
      locations.insert(target);
    } else {
      locations.updateById(target);
    }
    return Result.ok(target);
  }

  @DeleteMapping("/locations/{id}") @PreAuthorize(FIRE_MANAGE)
  public Result<Void> deleteLocation(@PathVariable Long id) {
    FireLocation target = ownedLocation(id);
    target.setIsDeleted(1);
    target.setUpdateTime(LocalDateTime.now());
    locations.updateById(target);
    return Result.ok(null);
  }

  // ---------- 检查模板（配置） ----------

  @GetMapping("/templates")
  public Result<List<FireCheckTemplate>> templates(@RequestParam(required = false) String recordType,
      @RequestParam(required = false) Boolean enabledOnly) {
    return Result.ok(templates.selectList(Wrappers.lambdaQuery(FireCheckTemplate.class)
        .eq(FireCheckTemplate::getOrgId, org()).eq(FireCheckTemplate::getIsDeleted, 0)
        .eq(recordType != null && !recordType.isBlank(), FireCheckTemplate::getRecordType, recordType)
        .eq(Boolean.TRUE.equals(enabledOnly), FireCheckTemplate::getEnabled, 1)
        .orderByAsc(FireCheckTemplate::getRecordType)));
  }

  /** 改动检查项会升版本，旧记录保存的是当时的结果，不受影响 */
  @PostMapping("/templates") @PreAuthorize(FIRE_MANAGE)
  public Result<FireCheckTemplate> saveTemplate(@RequestBody FireCheckTemplate body) {
    if (!TEMPLATE_TYPES.contains(String.valueOf(body.getRecordType()))) throw new IllegalArgumentException("模板只适用于日巡、夜巡和月检");
    if (body.getTemplateName() == null || body.getTemplateName().isBlank()) throw new IllegalArgumentException("请填写模板名称");
    String items = body.getItems() == null ? "" : body.getItems().lines().map(String::trim).filter(l -> !l.isEmpty())
        .reduce((a, b) -> a + "\n" + b).orElse("");
    if (items.isEmpty()) throw new IllegalArgumentException("请至少填写一个检查项");
    FireCheckTemplate target = body.getId() == null ? new FireCheckTemplate() : ownedTemplate(body.getId());
    boolean itemsChanged = !items.equals(target.getItems());
    target.setOrgId(org());
    target.setRecordType(body.getRecordType());
    target.setTemplateName(body.getTemplateName().trim());
    target.setItems(items);
    target.setEnabled(Integer.valueOf(0).equals(body.getEnabled()) ? 0 : 1);
    target.setUpdatedByName(currentName());
    target.setUpdateTime(LocalDateTime.now());
    if (target.getId() == null) {
      target.setVersion(1);
      target.setIsDeleted(0);
      target.setCreateTime(LocalDateTime.now());
      templates.insert(target);
    } else {
      if (itemsChanged) target.setVersion((target.getVersion() == null ? 1 : target.getVersion()) + 1);
      templates.updateById(target);
    }
    return Result.ok(target);
  }

  // ---------- 控制室交接班 ----------

  @GetMapping("/handovers")
  public Result<IPage<FireDutyHandover>> handovers(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String status) {
    return Result.ok(handovers.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(FireDutyHandover.class)
        .eq(FireDutyHandover::getOrgId, org()).eq(FireDutyHandover::getIsDeleted, 0)
        .eq(status != null && !status.isBlank(), FireDutyHandover::getStatus, status)
        .orderByDesc(FireDutyHandover::getHandoverAt)));
  }

  /** 接班人候选：本机构在职人员 */
  @GetMapping("/handovers/staff-options")
  public Result<List<Map<String, Object>>> staffOptions(@RequestParam(required = false) String keyword) {
    List<StaffAccount> staff = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getOrgId, org()).eq(StaffAccount::getIsDeleted, 0).eq(StaffAccount::getStatus, 1)
        .like(keyword != null && !keyword.isBlank(), StaffAccount::getRealName, keyword)
        .orderByAsc(StaffAccount::getRealName).last("LIMIT 50"));
    return Result.ok(staff.stream().filter(s -> !Objects.equals(s.getId(), AuthContext.getStaffId()))
        .map(s -> Map.<String, Object>of("value", String.valueOf(s.getId()), "label", s.getRealName() == null ? s.getUsername() : s.getRealName()))
        .toList());
  }

  public record HandoverRequest(String shiftName, LocalDate shiftDate, Long takeoverStaffId, String checklist, String openIssues) {}

  @PostMapping("/handovers")
  public Result<FireDutyHandover> handover(@RequestBody HandoverRequest body) {
    Long me = AuthContext.getStaffId();
    FireRules.requireDifferentTakeover(me, body.takeoverStaffId());
    StaffAccount takeover = staffMapper.selectById(body.takeoverStaffId());
    if (takeover == null || !Objects.equals(takeover.getOrgId(), org()) || Integer.valueOf(1).equals(takeover.getIsDeleted())) {
      throw new IllegalArgumentException("接班人不存在");
    }
    if (body.shiftName() == null || body.shiftName().isBlank()) throw new IllegalArgumentException("请填写班次");
    FireDutyHandover handover = new FireDutyHandover();
    handover.setOrgId(org());
    handover.setShiftDate(body.shiftDate() == null ? LocalDate.now() : body.shiftDate());
    handover.setShiftName(body.shiftName().trim());
    handover.setHandoverStaffId(me);
    handover.setHandoverName(currentName());
    handover.setTakeoverStaffId(takeover.getId());
    handover.setTakeoverName(takeover.getRealName());
    handover.setChecklist(body.checklist());
    handover.setOpenIssues(body.openIssues());
    handover.setStatus("PENDING");
    handover.setHandoverAt(LocalDateTime.now());
    handover.setCreateTime(LocalDateTime.now());
    handover.setUpdateTime(LocalDateTime.now());
    handover.setIsDeleted(0);
    handovers.insert(handover);
    return Result.ok(handover);
  }

  public record HandoverConfirm(boolean accept, String note) {}

  @PostMapping("/handovers/{id}/confirm")
  @Transactional
  public Result<FireDutyHandover> confirmHandover(@PathVariable Long id, @RequestBody HandoverConfirm body) {
    FireDutyHandover handover = ownedHandover(id);
    FireRules.confirmHandover(handover, AuthContext.getStaffId(), body.accept(), body.note(), LocalDateTime.now());
    handover.setUpdateTime(LocalDateTime.now());
    handovers.updateById(handover);
    return Result.ok(handover);
  }

  // ---------- 维保报告签署 ----------

  @GetMapping("/report-signatures")
  public Result<List<FireReportSignature>> signatures(@RequestParam Long recordId) {
    return Result.ok(signatures.selectList(Wrappers.lambdaQuery(FireReportSignature.class)
        .eq(FireReportSignature::getOrgId, org()).eq(FireReportSignature::getRecordId, recordId)
        .eq(FireReportSignature::getIsDeleted, 0).orderByAsc(FireReportSignature::getSignedAt)));
  }

  public record SignRequest(Long recordId, String signType, String comment) {}

  /** 签署时服务端计算报告文件哈希留证；签署记录只增不改 */
  @PostMapping("/report-signatures")
  @Transactional
  public Result<FireReportSignature> sign(@RequestBody SignRequest body) {
    FireSafetyRecord record = records.selectById(body.recordId());
    if (record == null || !Objects.equals(record.getOrgId(), org()) || !"MAINTENANCE_REPORT".equals(record.getRecordType())) {
      throw new IllegalArgumentException("只能签署维护保养报告");
    }
    String fileUrl = record.getThirdPartyMaintenanceFileUrl();
    if (fileUrl == null || fileUrl.isBlank()) throw new IllegalArgumentException("请先在维保记录中上传报告文件");
    String type = body.signType() == null ? "" : body.signType().trim().toUpperCase();
    List<FireReportSignature> existing = signatures(body.recordId()).getData();
    FireRules.requireSignAllowed(existing, type, AuthContext.getStaffId(), AuthContext.isMinisterOrHigher());
    if ("REVIEW".equals(type)) {
      String submittedHash = existing.stream().filter(s -> "SUBMIT".equals(s.getSignType())).map(FireReportSignature::getFileSha256)
          .filter(Objects::nonNull).findFirst().orElse(null);
      String nowHash = fileStorage.sha256(fileUrl).orElse(null);
      if (submittedHash != null && nowHash != null && !submittedHash.equals(nowHash)) {
        throw new IllegalArgumentException("报告文件与提交签署时不一致，请重新提交签署");
      }
    }
    FireReportSignature signature = new FireReportSignature();
    signature.setOrgId(org());
    signature.setRecordId(record.getId());
    signature.setSignType(type);
    signature.setSignerId(AuthContext.getStaffId());
    signature.setSignerName(currentName());
    signature.setFileUrl(fileUrl);
    signature.setFileSha256(fileStorage.sha256(fileUrl).orElse(null));
    signature.setComment(body.comment());
    signature.setSignedAt(LocalDateTime.now());
    signature.setCreateTime(LocalDateTime.now());
    signature.setIsDeleted(0);
    signatures.insert(signature);
    return Result.ok(signature);
  }

  // ---------- 培训与演练 ----------

  @GetMapping("/drills")
  public Result<IPage<FireTrainingDrill>> drills(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String drillType) {
    return Result.ok(drills.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(FireTrainingDrill.class)
        .eq(FireTrainingDrill::getOrgId, org()).eq(FireTrainingDrill::getIsDeleted, 0)
        .eq(drillType != null && !drillType.isBlank(), FireTrainingDrill::getDrillType, drillType)
        .orderByDesc(FireTrainingDrill::getPlannedDate)));
  }

  /** 标记完成须有实际日期和总结复盘 */
  @PostMapping("/drills") @PreAuthorize(FIRE_MANAGE)
  public Result<FireTrainingDrill> saveDrill(@RequestBody FireTrainingDrill body) {
    if (!Set.of("DRILL", "TRAINING").contains(String.valueOf(body.getDrillType()))) throw new IllegalArgumentException("类型只支持演练或培训");
    if (body.getTopic() == null || body.getTopic().isBlank()) throw new IllegalArgumentException("请填写主题");
    String status = "DONE".equals(body.getStatus()) ? "DONE" : "PLANNED";
    if ("DONE".equals(status) && (body.getHeldDate() == null || body.getSummary() == null || body.getSummary().isBlank())) {
      throw new IllegalArgumentException("标记完成前请填写实际日期和总结复盘");
    }
    FireTrainingDrill target = body.getId() == null ? new FireTrainingDrill() : ownedDrill(body.getId());
    target.setOrgId(org());
    target.setDrillType(body.getDrillType());
    target.setTopic(body.getTopic().trim());
    target.setPlannedDate(body.getPlannedDate());
    target.setHeldDate(body.getHeldDate());
    target.setOrganizerName(body.getOrganizerName());
    target.setParticipantCount(body.getParticipantCount());
    target.setAttendees(body.getAttendees());
    target.setSummary(body.getSummary());
    target.setNextPlan(body.getNextPlan());
    target.setFileUrl(body.getFileUrl());
    target.setStatus(status);
    target.setUpdateTime(LocalDateTime.now());
    if (target.getId() == null) {
      target.setIsDeleted(0);
      target.setCreateTime(LocalDateTime.now());
      drills.insert(target);
    } else {
      drills.updateById(target);
    }
    return Result.ok(target);
  }

  // ---------- 隐患统计与楼栋风险 ----------

  private List<FireSafetyRecord> activeIssues() {
    return records.selectList(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getOrgId, org()).eq(FireSafetyRecord::getIsDeleted, 0)
        .in(FireSafetyRecord::getStatus, List.of("OPEN", "RUNNING"))
        .isNotNull(FireSafetyRecord::getIssueDescription).ne(FireSafetyRecord::getIssueDescription, ""));
  }

  @GetMapping("/hazards/stats")
  public Result<Map<String, Long>> hazardStats() {
    return Result.ok(FireRules.hazardStats(activeIssues(), LocalDate.now()));
  }

  @GetMapping("/buildings/risk")
  public Result<List<FireRules.BuildingRisk>> buildingRisk() {
    return Result.ok(FireRules.buildingRisk(locations().getData().stream()
        .filter(l -> !Integer.valueOf(0).equals(l.getEnabled())).toList(), activeIssues(), LocalDate.now()));
  }

  // ---------- helpers ----------

  private Long org() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) throw new IllegalArgumentException("当前账号未绑定机构");
    return orgId;
  }

  private String currentName() {
    StaffAccount staff = AuthContext.getStaffId() == null ? null : staffMapper.selectById(AuthContext.getStaffId());
    return staff == null ? AuthContext.getUsername() : staff.getRealName();
  }

  private void requireOwned(Long entityOrg, Integer deleted, String label) {
    if (entityOrg == null || Integer.valueOf(1).equals(deleted) || !Objects.equals(entityOrg, org())) {
      throw new ResourceNotFoundException(label + "不存在或无权访问");
    }
  }

  private FireLocation ownedLocation(Long id) {
    FireLocation e = locations.selectById(id);
    requireOwned(e == null ? null : e.getOrgId(), e == null ? null : e.getIsDeleted(), "点位");
    return e;
  }

  private FireCheckTemplate ownedTemplate(Long id) {
    FireCheckTemplate e = templates.selectById(id);
    requireOwned(e == null ? null : e.getOrgId(), e == null ? null : e.getIsDeleted(), "模板");
    return e;
  }

  private FireDutyHandover ownedHandover(Long id) {
    FireDutyHandover e = handovers.selectById(id);
    requireOwned(e == null ? null : e.getOrgId(), e == null ? null : e.getIsDeleted(), "交接记录");
    return e;
  }

  private FireTrainingDrill ownedDrill(Long id) {
    FireTrainingDrill e = drills.selectById(id);
    requireOwned(e == null ? null : e.getOrgId(), e == null ? null : e.getIsDeleted(), "台账");
    return e;
  }
}
