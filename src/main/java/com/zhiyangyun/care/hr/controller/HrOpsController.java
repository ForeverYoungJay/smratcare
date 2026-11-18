package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.hr.entity.HrAttendanceAdjustment;
import com.zhiyangyun.care.hr.entity.HrAttendancePeriod;
import com.zhiyangyun.care.hr.entity.HrPayrollBatch;
import com.zhiyangyun.care.hr.entity.HrPayrollLine;
import com.zhiyangyun.care.hr.entity.HrPerformanceAppeal;
import com.zhiyangyun.care.hr.entity.StaffPointsLog;
import com.zhiyangyun.care.hr.entity.StaffServicePlan;
import com.zhiyangyun.care.hr.mapper.HrAttendanceAdjustmentMapper;
import com.zhiyangyun.care.hr.mapper.HrAttendancePeriodMapper;
import com.zhiyangyun.care.hr.mapper.HrPayrollBatchMapper;
import com.zhiyangyun.care.hr.mapper.HrPayrollLineMapper;
import com.zhiyangyun.care.hr.mapper.HrPerformanceAppealMapper;
import com.zhiyangyun.care.hr.mapper.StaffPointsLogMapper;
import com.zhiyangyun.care.hr.mapper.StaffServicePlanMapper;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.service.HrRules;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人事运营：工资批次（核算/审核/财务付款分离）、考勤月度锁定与调整单、积分冲正与申诉、返聘查重、离职清单。
 * /api/admin/hr/** 为人事与财务办理；/api/hr/** 为员工本人查看工资单、提交申诉。
 */
@RestController
@RequestMapping("/api")
public class HrOpsController {
  private static final String PREPARE = "hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String HR_MANAGER = "hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String FINANCE = "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER')";
  private static final String PAYROLL_VIEW = "hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','FINANCE_EMPLOYEE','FINANCE_MINISTER')";

  private final HrPayrollBatchMapper batchMapper;
  private final HrPayrollLineMapper lineMapper;
  private final HrAttendancePeriodMapper periodMapper;
  private final HrAttendanceAdjustmentMapper adjustmentMapper;
  private final HrPerformanceAppealMapper appealMapper;
  private final StaffPointsLogMapper pointsLogMapper;
  private final StaffServicePlanMapper servicePlanMapper;
  private final AttendanceRecordMapper attendanceMapper;
  private final StaffMapper staffMapper;
  private final OaTaskMapper taskMapper;
  private final StaffPointsService pointsService;
  private final AuditLogService auditLogService;

  public HrOpsController(HrPayrollBatchMapper batchMapper, HrPayrollLineMapper lineMapper,
      HrAttendancePeriodMapper periodMapper, HrAttendanceAdjustmentMapper adjustmentMapper,
      HrPerformanceAppealMapper appealMapper, StaffPointsLogMapper pointsLogMapper,
      StaffServicePlanMapper servicePlanMapper, AttendanceRecordMapper attendanceMapper,
      StaffMapper staffMapper, OaTaskMapper taskMapper, StaffPointsService pointsService,
      AuditLogService auditLogService) {
    this.batchMapper = batchMapper;
    this.lineMapper = lineMapper;
    this.periodMapper = periodMapper;
    this.adjustmentMapper = adjustmentMapper;
    this.appealMapper = appealMapper;
    this.pointsLogMapper = pointsLogMapper;
    this.servicePlanMapper = servicePlanMapper;
    this.attendanceMapper = attendanceMapper;
    this.staffMapper = staffMapper;
    this.taskMapper = taskMapper;
    this.pointsService = pointsService;
    this.auditLogService = auditLogService;
  }

  // ---------- 工资批次 ----------

  @PreAuthorize(PAYROLL_VIEW)
  @GetMapping("/admin/hr/payroll/batches")
  public Result<IPage<HrPayrollBatch>> batches(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String status) {
    return Result.ok(batchMapper.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(HrPayrollBatch.class)
        .eq(HrPayrollBatch::getIsDeleted, 0)
        .eq(HrPayrollBatch::getOrgId, AuthContext.getOrgId())
        .eq(status != null && !status.isBlank(), HrPayrollBatch::getStatus, status)
        .orderByDesc(HrPayrollBatch::getPeriod)));
  }

  public record CreateBatchRequest(String period) {}

  /** 生成工资草稿：在职员工各一行；同一员工沿用上一批次的基本工资和津补贴，其余由人事核算填写 */
  @PreAuthorize(PREPARE)
  @PostMapping("/admin/hr/payroll/batches")
  @Transactional
  public Result<HrPayrollBatch> createBatch(@RequestBody CreateBatchRequest request) {
    Long orgId = AuthContext.getOrgId();
    String period;
    try {
      period = YearMonth.parse(request.period()).toString();
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("工资期间格式应为 YYYY-MM");
    }
    if (batchMapper.selectCount(Wrappers.lambdaQuery(HrPayrollBatch.class)
        .eq(HrPayrollBatch::getIsDeleted, 0).eq(HrPayrollBatch::getOrgId, orgId).eq(HrPayrollBatch::getPeriod, period)) > 0) {
      throw new IllegalArgumentException("该期间已有工资批次，请在原批次上核算");
    }
    List<StaffAccount> staff = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0).eq(StaffAccount::getOrgId, orgId).eq(StaffAccount::getStatus, 1));
    HrPayrollBatch previous = batchMapper.selectOne(Wrappers.lambdaQuery(HrPayrollBatch.class)
        .eq(HrPayrollBatch::getIsDeleted, 0).eq(HrPayrollBatch::getOrgId, orgId).lt(HrPayrollBatch::getPeriod, period)
        .orderByDesc(HrPayrollBatch::getPeriod).last("LIMIT 1"));
    Map<Long, HrPayrollLine> previousLines = previous == null ? Map.of() : lineMapper.selectList(
            Wrappers.lambdaQuery(HrPayrollLine.class).eq(HrPayrollLine::getBatchId, previous.getId()))
        .stream().collect(Collectors.toMap(HrPayrollLine::getStaffId, Function.identity(), (a, b) -> a));

    HrPayrollBatch batch = new HrPayrollBatch();
    batch.setOrgId(orgId);
    batch.setPeriod(period);
    batch.setStatus("DRAFT");
    batch.setPreparedBy(AuthContext.getStaffId());
    batch.setPreparedByName(AuthContext.getUsername());
    batch.setIsDeleted(0);
    batchMapper.insert(batch);
    for (StaffAccount s : staff) {
      HrPayrollLine last = previousLines.get(s.getId());
      HrPayrollLine line = new HrPayrollLine();
      line.setOrgId(orgId);
      line.setBatchId(batch.getId());
      line.setStaffId(s.getId());
      line.setStaffName(s.getRealName());
      line.setBaseAmount(last == null ? BigDecimal.ZERO : last.getBaseAmount());
      line.setAllowanceAmount(last == null ? BigDecimal.ZERO : last.getAllowanceAmount());
      HrRules.computeLine(line);
      lineMapper.insert(line);
    }
    refreshTotals(batch);
    audit("CREATE", batch.getId(), "生成工资草稿 " + period);
    return Result.ok(batch);
  }

  @PreAuthorize(PAYROLL_VIEW)
  @GetMapping("/admin/hr/payroll/batches/{id}/lines")
  public Result<List<HrPayrollLine>> lines(@PathVariable Long id) {
    requireBatch(id);
    return Result.ok(lineMapper.selectList(Wrappers.lambdaQuery(HrPayrollLine.class)
        .eq(HrPayrollLine::getBatchId, id).orderByAsc(HrPayrollLine::getStaffName)));
  }

  /** 只有草稿可改；改后重算本行与批次合计 */
  @PreAuthorize(PREPARE)
  @PutMapping("/admin/hr/payroll/lines/{id}")
  @Transactional
  public Result<HrPayrollLine> updateLine(@PathVariable Long id, @RequestBody HrPayrollLine request) {
    HrPayrollLine line = lineMapper.selectById(id);
    if (line == null) {
      throw new IllegalArgumentException("工资单不存在");
    }
    HrPayrollBatch batch = requireBatch(line.getBatchId());
    if (!"DRAFT".equals(batch.getStatus())) {
      throw new IllegalArgumentException("只有草稿状态的工资批次可以修改");
    }
    line.setBaseAmount(HrRules.money(request.getBaseAmount()));
    line.setPerformanceAmount(HrRules.money(request.getPerformanceAmount()));
    line.setAllowanceAmount(HrRules.money(request.getAllowanceAmount()));
    line.setOvertimeAmount(HrRules.money(request.getOvertimeAmount()));
    line.setDeductionAmount(HrRules.money(request.getDeductionAmount()));
    line.setPersonalAmount(HrRules.money(request.getPersonalAmount()));
    line.setRemark(request.getRemark());
    HrRules.computeLine(line);
    lineMapper.updateById(line);
    refreshTotals(batch);
    return Result.ok(line);
  }

  public record BatchActionRequest(String note, String paymentRef) {}

  @PreAuthorize(PREPARE)
  @PostMapping("/admin/hr/payroll/batches/{id}/submit")
  public Result<HrPayrollBatch> submit(@PathVariable Long id) {
    return Result.ok(transition(id, "SUBMIT", null));
  }

  /** 审核人不能是核算人 */
  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR')")
  @PostMapping("/admin/hr/payroll/batches/{id}/approve")
  public Result<HrPayrollBatch> approve(@PathVariable Long id) {
    return Result.ok(transition(id, "APPROVE", null));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR')")
  @PostMapping("/admin/hr/payroll/batches/{id}/reject")
  public Result<HrPayrollBatch> reject(@PathVariable Long id, @RequestBody BatchActionRequest request) {
    return Result.ok(transition(id, "REJECT", request));
  }

  /** 财务复核后提交银行；不等于付款成功 */
  @PreAuthorize(FINANCE)
  @PostMapping("/admin/hr/payroll/batches/{id}/bank-submit")
  public Result<HrPayrollBatch> bankSubmit(@PathVariable Long id) {
    return Result.ok(transition(id, "BANK_SUBMIT", null));
  }

  /** 财务凭银行回执登记付款成功 */
  @PreAuthorize(FINANCE)
  @PostMapping("/admin/hr/payroll/batches/{id}/mark-paid")
  public Result<HrPayrollBatch> markPaid(@PathVariable Long id, @RequestBody BatchActionRequest request) {
    return Result.ok(transition(id, "MARK_PAID", request));
  }

  /** 员工本人：只看自己、且已审核通过的工资单 */
  @GetMapping("/hr/payroll/my")
  public Result<List<Map<String, Object>>> myPayslips() {
    Long staffId = AuthContext.getStaffId();
    List<HrPayrollLine> lines = lineMapper.selectList(Wrappers.lambdaQuery(HrPayrollLine.class)
        .eq(HrPayrollLine::getStaffId, staffId).eq(HrPayrollLine::getOrgId, AuthContext.getOrgId()));
    if (lines.isEmpty()) {
      return Result.ok(List.of());
    }
    Map<Long, HrPayrollBatch> batches = batchMapper.selectBatchIds(lines.stream().map(HrPayrollLine::getBatchId).toList())
        .stream().collect(Collectors.toMap(HrPayrollBatch::getId, Function.identity()));
    List<Map<String, Object>> result = new ArrayList<>();
    for (HrPayrollLine line : lines) {
      HrPayrollBatch batch = batches.get(line.getBatchId());
      if (batch == null || Integer.valueOf(1).equals(batch.getIsDeleted())
          || List.of("DRAFT", "SUBMITTED").contains(batch.getStatus())) {
        continue;
      }
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("period", batch.getPeriod());
      row.put("status", batch.getStatus());
      row.put("line", line);
      result.add(row);
    }
    result.sort((a, b) -> String.valueOf(b.get("period")).compareTo(String.valueOf(a.get("period"))));
    audit("VIEW_PAYSLIP", staffId, "员工查看本人工资单 " + result.size() + " 期");
    return Result.ok(result);
  }

  private HrPayrollBatch transition(Long id, String action, BatchActionRequest request) {
    HrPayrollBatch batch = requireBatch(id);
    String next = HrRules.nextPayrollStatus(batch.getStatus(), action);
    String note = request == null ? null : request.note();
    LocalDateTime now = LocalDateTime.now();
    switch (action) {
      case "APPROVE" -> {
        if (Objects.equals(batch.getPreparedBy(), AuthContext.getStaffId())) {
          throw new IllegalArgumentException("核算人不能审核自己的工资批次");
        }
        batch.setApprovedBy(AuthContext.getStaffId());
        batch.setApprovedByName(AuthContext.getUsername());
        batch.setApprovedAt(now);
      }
      case "REJECT" -> {
        if (note == null || note.isBlank()) {
          throw new IllegalArgumentException("请填写退回原因");
        }
        batch.setRejectReason(note.trim());
      }
      case "BANK_SUBMIT" -> {
        batch.setBankSubmittedByName(AuthContext.getUsername());
        batch.setBankSubmittedAt(now);
      }
      case "MARK_PAID" -> {
        if (request == null || request.paymentRef() == null || request.paymentRef().isBlank()) {
          throw new IllegalArgumentException("请填写银行付款回执号");
        }
        batch.setPaymentRef(request.paymentRef().trim());
        batch.setPaidByName(AuthContext.getUsername());
        batch.setPaidAt(now);
      }
      default -> { }
    }
    batch.setStatus(next);
    batchMapper.updateById(batch);
    audit(action, batch.getId(), "工资批次 " + batch.getPeriod() + " → " + next);
    return batch;
  }

  private HrPayrollBatch requireBatch(Long id) {
    HrPayrollBatch batch = batchMapper.selectById(id);
    if (batch == null || Integer.valueOf(1).equals(batch.getIsDeleted()) || !Objects.equals(batch.getOrgId(), AuthContext.getOrgId())) {
      throw new IllegalArgumentException("工资批次不存在");
    }
    return batch;
  }

  private void refreshTotals(HrPayrollBatch batch) {
    List<HrPayrollLine> lines = lineMapper.selectList(Wrappers.lambdaQuery(HrPayrollLine.class).eq(HrPayrollLine::getBatchId, batch.getId()));
    batch.setHeadcount(lines.size());
    batch.setTotalGross(lines.stream().map(HrPayrollLine::getGrossAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    batch.setTotalNet(lines.stream().map(HrPayrollLine::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
    batchMapper.updateById(batch);
  }

  // ---------- 考勤月度锁定与调整单 ----------

  @PreAuthorize(PREPARE)
  @GetMapping("/admin/hr/attendance/periods")
  public Result<List<HrAttendancePeriod>> periods() {
    return Result.ok(periodMapper.selectList(Wrappers.lambdaQuery(HrAttendancePeriod.class)
        .eq(HrAttendancePeriod::getOrgId, AuthContext.getOrgId()).orderByDesc(HrAttendancePeriod::getPeriod)));
  }

  /** 核定后锁定：之后只能通过审批的调整单修改 */
  @PreAuthorize(HR_MANAGER)
  @PostMapping("/admin/hr/attendance/periods/{period}/lock")
  public Result<HrAttendancePeriod> lock(@PathVariable String period) {
    String month = HrRules.lockablePeriod(period, YearMonth.now()).toString();
    Long orgId = AuthContext.getOrgId();
    if (periodMapper.selectCount(Wrappers.lambdaQuery(HrAttendancePeriod.class)
        .eq(HrAttendancePeriod::getOrgId, orgId).eq(HrAttendancePeriod::getPeriod, month)) > 0) {
      throw new IllegalArgumentException("该月考勤已锁定");
    }
    HrAttendancePeriod locked = new HrAttendancePeriod();
    locked.setOrgId(orgId);
    locked.setPeriod(month);
    locked.setStatus("LOCKED");
    locked.setLockedByName(AuthContext.getUsername());
    locked.setLockedAt(LocalDateTime.now());
    periodMapper.insert(locked);
    audit("LOCK", locked.getId(), "锁定考勤期间 " + month);
    return Result.ok(locked);
  }

  @PreAuthorize(PREPARE)
  @GetMapping("/admin/hr/attendance/adjustments")
  public Result<IPage<HrAttendanceAdjustment>> adjustments(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String status) {
    return Result.ok(adjustmentMapper.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(HrAttendanceAdjustment.class)
        .eq(HrAttendanceAdjustment::getOrgId, AuthContext.getOrgId())
        .eq(status != null && !status.isBlank(), HrAttendanceAdjustment::getStatus, status)
        .orderByDesc(HrAttendanceAdjustment::getCreateTime)));
  }

  @PreAuthorize(PREPARE)
  @PostMapping("/admin/hr/attendance/adjustments")
  public Result<HrAttendanceAdjustment> requestAdjustment(@RequestBody HrAttendanceAdjustment request) {
    AttendanceRecord record = attendanceMapper.selectById(request.getRecordId());
    if (record == null || !Objects.equals(record.getOrgId(), AuthContext.getOrgId())) {
      throw new IllegalArgumentException("考勤记录不存在");
    }
    if (request.getReason() == null || request.getReason().isBlank()) {
      throw new IllegalArgumentException("请填写调整原因");
    }
    HrAttendanceAdjustment adjustment = new HrAttendanceAdjustment();
    adjustment.setOrgId(record.getOrgId());
    adjustment.setRecordId(record.getId());
    adjustment.setStaffId(record.getStaffId());
    adjustment.setWorkDate(record.getWorkDate());
    adjustment.setNewCheckIn(request.getNewCheckIn());
    adjustment.setNewCheckOut(request.getNewCheckOut());
    adjustment.setNewStatus(request.getNewStatus());
    adjustment.setReason(request.getReason().trim());
    adjustment.setStatus("PENDING");
    adjustment.setRequestedBy(AuthContext.getStaffId());
    adjustment.setRequestedByName(AuthContext.getUsername());
    adjustmentMapper.insert(adjustment);
    return Result.ok(adjustment);
  }

  public record DecideRequest(boolean approve, String note) {}

  /** 审批通过才写回考勤记录；申请人不能审批自己的调整单 */
  @PreAuthorize(HR_MANAGER)
  @PostMapping("/admin/hr/attendance/adjustments/{id}/decide")
  @Transactional
  public Result<HrAttendanceAdjustment> decideAdjustment(@PathVariable Long id, @RequestBody DecideRequest request) {
    HrAttendanceAdjustment adjustment = adjustmentMapper.selectById(id);
    if (adjustment == null || !Objects.equals(adjustment.getOrgId(), AuthContext.getOrgId()) || !"PENDING".equals(adjustment.getStatus())) {
      throw new IllegalArgumentException("仅待审批的调整单可处理");
    }
    if (Objects.equals(adjustment.getRequestedBy(), AuthContext.getStaffId())) {
      throw new IllegalArgumentException("申请人不能审批自己的调整单");
    }
    adjustment.setStatus(request.approve() ? "APPROVED" : "REJECTED");
    adjustment.setDecidedByName(AuthContext.getUsername());
    adjustment.setDecidedAt(LocalDateTime.now());
    adjustment.setDecisionNote(request.note());
    adjustmentMapper.updateById(adjustment);
    if (request.approve()) {
      AttendanceRecord record = attendanceMapper.selectById(adjustment.getRecordId());
      if (adjustment.getNewCheckIn() != null) {
        record.setCheckInTime(adjustment.getNewCheckIn());
      }
      if (adjustment.getNewCheckOut() != null) {
        record.setCheckOutTime(adjustment.getNewCheckOut());
      }
      if (adjustment.getNewStatus() != null && !adjustment.getNewStatus().isBlank()) {
        record.setStatus(adjustment.getNewStatus());
      }
      record.setReviewRemark("调整单 #" + adjustment.getId() + "：" + adjustment.getReason());
      attendanceMapper.updateById(record);
      audit("ADJUST", record.getId(), "考勤调整单 #" + adjustment.getId() + " 通过");
    }
    return Result.ok(adjustment);
  }

  // ---------- 积分冲正与申诉 ----------

  public record ReasonRequest(String reason) {}

  /** 冲正：追加一条相反分值的流水，不改原记录；同一流水只能冲正一次 */
  @PreAuthorize(HR_MANAGER)
  @PostMapping("/admin/hr/points/log/{id}/reverse")
  @Transactional
  public Result<Void> reverse(@PathVariable Long id, @RequestBody ReasonRequest request) {
    reversePoints(id, request == null ? null : request.reason());
    return Result.ok(null);
  }

  @PreAuthorize(PREPARE)
  @GetMapping("/admin/hr/appeals")
  public Result<IPage<HrPerformanceAppeal>> appeals(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String status) {
    return Result.ok(appealMapper.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(HrPerformanceAppeal.class)
        .eq(HrPerformanceAppeal::getOrgId, AuthContext.getOrgId())
        .eq(status != null && !status.isBlank(), HrPerformanceAppeal::getStatus, status)
        .orderByDesc(HrPerformanceAppeal::getCreateTime)));
  }

  /** 复核：积分申诉成立时自动冲正该流水 */
  @PreAuthorize(HR_MANAGER)
  @PostMapping("/admin/hr/appeals/{id}/decide")
  @Transactional
  public Result<HrPerformanceAppeal> decideAppeal(@PathVariable Long id, @RequestBody DecideRequest request) {
    HrPerformanceAppeal appeal = appealMapper.selectById(id);
    if (appeal == null || !Objects.equals(appeal.getOrgId(), AuthContext.getOrgId()) || !"PENDING".equals(appeal.getStatus())) {
      throw new IllegalArgumentException("仅待复核的申诉可处理");
    }
    if (Objects.equals(appeal.getStaffId(), AuthContext.getStaffId())) {
      throw new IllegalArgumentException("不能复核自己的申诉");
    }
    if (request.note() == null || request.note().isBlank()) {
      throw new IllegalArgumentException("请填写复核意见");
    }
    appeal.setStatus(request.approve() ? "ACCEPTED" : "REJECTED");
    appeal.setReviewerName(AuthContext.getUsername());
    appeal.setReviewedAt(LocalDateTime.now());
    appeal.setDecisionNote(request.note().trim());
    appealMapper.updateById(appeal);
    if (request.approve() && "POINTS_LOG".equals(appeal.getTargetType()) && appeal.getTargetId() != null) {
      reversePoints(appeal.getTargetId(), "申诉 #" + appeal.getId() + " 成立：" + appeal.getDecisionNote());
    }
    return Result.ok(appeal);
  }

  /** 员工本人提交申诉（只能针对自己的积分流水等记录） */
  @PostMapping("/hr/appeals")
  public Result<HrPerformanceAppeal> submitAppeal(@RequestBody HrPerformanceAppeal request) {
    Long staffId = AuthContext.getStaffId();
    if (request.getReason() == null || request.getReason().isBlank()) {
      throw new IllegalArgumentException("请填写申诉理由");
    }
    String type = request.getTargetType() == null ? "" : request.getTargetType().trim().toUpperCase();
    if (!List.of("POINTS_LOG", "REWARD_PUNISHMENT", "PERFORMANCE").contains(type)) {
      throw new IllegalArgumentException("申诉对象不正确");
    }
    if ("POINTS_LOG".equals(type)) {
      StaffPointsLog log = pointsLogMapper.selectById(request.getTargetId());
      if (log == null || !Objects.equals(log.getStaffId(), staffId)) {
        throw new IllegalArgumentException("只能对本人的积分记录申诉");
      }
    }
    StaffAccount me = staffMapper.selectById(staffId);
    HrPerformanceAppeal appeal = new HrPerformanceAppeal();
    appeal.setOrgId(AuthContext.getOrgId());
    appeal.setStaffId(staffId);
    appeal.setStaffName(me == null ? AuthContext.getUsername() : me.getRealName());
    appeal.setTargetType(type);
    appeal.setTargetId(request.getTargetId());
    appeal.setReason(request.getReason().trim());
    appeal.setStatus("PENDING");
    appealMapper.insert(appeal);
    return Result.ok(appeal);
  }

  /** 员工本人最近的积分流水（用于查看和发起申诉） */
  @GetMapping("/hr/points/my")
  public Result<List<StaffPointsLog>> myPoints() {
    return Result.ok(pointsLogMapper.selectList(Wrappers.lambdaQuery(StaffPointsLog.class)
        .eq(StaffPointsLog::getStaffId, AuthContext.getStaffId()).eq(StaffPointsLog::getIsDeleted, 0)
        .orderByDesc(StaffPointsLog::getCreateTime).last("LIMIT 50")));
  }

  @GetMapping("/hr/appeals/my")
  public Result<List<HrPerformanceAppeal>> myAppeals() {
    return Result.ok(appealMapper.selectList(Wrappers.lambdaQuery(HrPerformanceAppeal.class)
        .eq(HrPerformanceAppeal::getStaffId, AuthContext.getStaffId()).orderByDesc(HrPerformanceAppeal::getCreateTime)));
  }

  private void reversePoints(Long logId, String reason) {
    StaffPointsLog log = pointsLogMapper.selectById(logId);
    if (log == null || !Objects.equals(log.getOrgId(), AuthContext.getOrgId())) {
      throw new IllegalArgumentException("积分记录不存在");
    }
    if ("REVERSAL".equals(log.getSourceType())) {
      throw new IllegalArgumentException("冲正记录不能再冲正");
    }
    if (pointsLogMapper.selectCount(Wrappers.lambdaQuery(StaffPointsLog.class)
        .eq(StaffPointsLog::getSourceType, "REVERSAL").eq(StaffPointsLog::getSourceId, logId)) > 0) {
      throw new IllegalArgumentException("该积分记录已冲正");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("请填写冲正原因");
    }
    StaffPointsAdjustRequest adjust = new StaffPointsAdjustRequest();
    adjust.setStaffId(log.getStaffId());
    adjust.setChangeType("ADJUST");
    adjust.setChangePoints(-(log.getChangePoints() == null ? 0 : log.getChangePoints()));
    adjust.setSourceType("REVERSAL");
    adjust.setSourceId(logId);
    adjust.setRemark("冲正 #" + logId + "：" + reason.trim());
    if (pointsService.adjust(AuthContext.getOrgId(), AuthContext.getStaffId(), adjust) == null) {
      throw new IllegalArgumentException("积分余额不足，无法冲正");
    }
    audit("REVERSE", logId, adjust.getRemark());
  }

  // ---------- 返聘查重与离职清单 ----------

  /** 入职前查重：同手机号或同名的已有档案（含已离职），离职人员应走返聘而不是新建 */
  @PreAuthorize(PREPARE)
  @GetMapping("/admin/hr/staff/duplicate-check")
  public Result<List<Map<String, Object>>> duplicateCheck(@RequestParam(required = false) String phone,
      @RequestParam(required = false) String name) {
    boolean byPhone = phone != null && !phone.isBlank();
    boolean byName = name != null && !name.isBlank();
    if (!byPhone && !byName) {
      return Result.ok(List.of());
    }
    List<StaffAccount> matches = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getOrgId, AuthContext.getOrgId())
        .and(w -> w.eq(byPhone, StaffAccount::getPhone, byPhone ? phone.trim() : null)
            .or(byName).eq(byName, StaffAccount::getRealName, byName ? name.trim() : null))
        .last("LIMIT 10"));
    return Result.ok(matches.stream().map(s -> {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("staffId", String.valueOf(s.getId()));
      row.put("staffNo", s.getStaffNo());
      row.put("realName", s.getRealName());
      row.put("onJob", Integer.valueOf(1).equals(s.getStatus()));
      row.put("samePhone", byPhone && phone.trim().equals(s.getPhone()));
      return row;
    }).toList());
  }

  /** 离职前核对：宿舍退宿、未发放工资、未完成任务、账号状态 */
  @PreAuthorize(HR_MANAGER)
  @GetMapping("/admin/hr/staff/{staffId}/offboarding-checklist")
  public Result<List<Map<String, Object>>> offboardingChecklist(@PathVariable Long staffId) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null || !Objects.equals(staff.getOrgId(), orgId)) {
      throw new IllegalArgumentException("员工不存在");
    }
    StaffServicePlan plan = servicePlanMapper.selectOne(Wrappers.lambdaQuery(StaffServicePlan.class)
        .eq(StaffServicePlan::getOrgId, orgId).eq(StaffServicePlan::getStaffId, staffId)
        .eq(StaffServicePlan::getIsDeleted, 0).last("LIMIT 1"));
    boolean inDorm = plan != null && Integer.valueOf(1).equals(plan.getLiveInDormitory());
    List<Long> openBatchIds = batchMapper.selectList(Wrappers.lambdaQuery(HrPayrollBatch.class)
        .eq(HrPayrollBatch::getIsDeleted, 0).eq(HrPayrollBatch::getOrgId, orgId).ne(HrPayrollBatch::getStatus, "PAID"))
        .stream().map(HrPayrollBatch::getId).toList();
    long unpaid = openBatchIds.isEmpty() ? 0 : lineMapper.selectCount(Wrappers.lambdaQuery(HrPayrollLine.class)
        .eq(HrPayrollLine::getStaffId, staffId).in(HrPayrollLine::getBatchId, openBatchIds));
    long openTasks = taskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0).eq(OaTask::getOrgId, orgId).eq(OaTask::getAssigneeId, staffId).ne(OaTask::getStatus, "DONE"));
    List<Map<String, Object>> items = new ArrayList<>();
    items.add(item("dormitory", "宿舍退宿", !inDorm, inDorm
        ? "仍登记住在 " + plan.getDormitoryBuilding() + " " + plan.getDormitoryRoomNo() + " 床位 " + plan.getDormitoryBedNo() + "，请办理退宿" : "未登记住宿"));
    items.add(item("payroll", "工资结算", unpaid == 0, unpaid == 0 ? "没有未付款的工资批次" : "有 " + unpaid + " 个工资批次尚未付款，请在批次中结算"));
    items.add(item("tasks", "工作交接", openTasks == 0, openTasks == 0 ? "没有未完成的协同任务" : openTasks + " 项协同任务未完成，请交接或改派"));
    items.add(item("account", "账号停用", !Integer.valueOf(1).equals(staff.getStatus()),
        Integer.valueOf(1).equals(staff.getStatus()) ? "办理离职后账号自动停用" : "账号已停用"));
    return Result.ok(items);
  }

  private Map<String, Object> item(String key, String label, boolean done, String detail) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("key", key);
    row.put("label", label);
    row.put("done", done);
    row.put("detail", detail);
    return row;
  }

  private void audit(String action, Long entityId, String detail) {
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(), action, "HR_OPS", entityId, detail);
  }
}
