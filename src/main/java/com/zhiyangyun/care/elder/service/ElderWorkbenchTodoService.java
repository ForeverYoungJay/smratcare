package com.zhiyangyun.care.elder.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmissionProgress;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionProgressMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.elder.model.lifecycle.ElderWorkbenchTodoResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ElderWorkbenchTodoResponse.Item;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import com.zhiyangyun.care.elder.security.ElderAuthz;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * 长者工作台待办汇总。提醒口径（「费用与退住」页使用同一口径）：
 * <ul>
 *   <li>外出 / 外出就医超时未返院：status=OUT 且应返时间早于当前时间</li>
 *   <li>外出申请待审批：status=APPLIED</li>
 *   <li>退住申请待审核：PENDING 且创建未满 {@value #DISCHARGE_REVIEW_TIMEOUT_DAYS} 天；满 3 天为「退住审批超时」</li>
 *   <li>退住办理超时：APPROVED、未归档、计划退住日早于今天</li>
 *   <li>合同即将到期 / 已到期：status 为 SIGNED/EFFECTIVE，到期日不晚于今天+{@value #CONTRACT_EXPIRING_DAYS} 天
 *       （早于今天为已到期），且同一长者没有到期更晚的生效合同（未续签）</li>
 *   <li>合同待选床入住：flowStage=PENDING_BED_SELECT；入住资料退回待补充：入住办理进度 status=RETURNED</li>
 * </ul>
 * 「我负责的」：长者的责任人（elder.responsible_staff_id）是当前员工，或该记录由当前员工创建 / 处理——
 * 外出与就医看 createdBy；退住看 createdBy、审核人 reviewedBy；合同看 createdBy 与线索负责人 ownerStaffId。
 * 入住办理进度只有最后保存人（多为退回人），因此只按长者责任人判断。
 */
@Service
public class ElderWorkbenchTodoService {
  public static final int DISCHARGE_REVIEW_TIMEOUT_DAYS = 3;
  public static final int CONTRACT_EXPIRING_DAYS = 30;

  static final String URGENT = "urgent";
  static final String HIGH = "high";
  static final String NORMAL = "normal";
  private static final Map<String, Integer> LEVEL_ORDER = Map.of(URGENT, 0, HIGH, 1, NORMAL, 2);
  private static final Map<String, String> DEPARTMENT_NAMES = Map.of(
      "NURSING", "护理部", "MEDICAL", "医务部", "FINANCE", "财务部",
      "LOGISTICS", "后勤部", "HR", "人事部", "MARKETING", "市场部");
  private static final String BED_RECORDS = "/elder/bed-panorama?view=records&recordType=";
  private static final String FINANCE_EXIT = "/elder/status-change/discharge-settlement";
  private static final String ADMISSION = "/elder/admission-processing";
  /** 与 CrmContractController 的 CRM_CONTRACT_READ 一致 */
  private static final List<String> CONTRACT_READ_ROLES = List.of(
      "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER",
      "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN");

  private final ElderOutingRecordMapper outingMapper;
  private final ElderMedicalOutingRecordMapper medicalOutingMapper;
  private final ElderDischargeApplyMapper dischargeApplyMapper;
  private final ElderAdmissionProgressMapper admissionProgressMapper;
  private final CrmContractMapper contractMapper;
  private final CrmLeadMapper leadMapper;
  private final ElderMapper elderMapper;
  private final StaffMapper staffMapper;
  private final ElderAuthz elderAuthz;

  public ElderWorkbenchTodoService(ElderOutingRecordMapper outingMapper,
      ElderMedicalOutingRecordMapper medicalOutingMapper,
      ElderDischargeApplyMapper dischargeApplyMapper,
      ElderAdmissionProgressMapper admissionProgressMapper,
      CrmContractMapper contractMapper,
      CrmLeadMapper leadMapper,
      ElderMapper elderMapper,
      StaffMapper staffMapper,
      ElderAuthz elderAuthz) {
    this.outingMapper = outingMapper;
    this.medicalOutingMapper = medicalOutingMapper;
    this.dischargeApplyMapper = dischargeApplyMapper;
    this.admissionProgressMapper = admissionProgressMapper;
    this.contractMapper = contractMapper;
    this.leadMapper = leadMapper;
    this.elderMapper = elderMapper;
    this.staffMapper = staffMapper;
    this.elderAuthz = elderAuthz;
  }

  /** 各来源的未完成记录；当前用户无权查看的来源为空列表。 */
  record Sources(
      List<ElderOutingRecord> outings,
      List<ElderMedicalOutingRecord> medicalOutings,
      List<ElderDischargeApply> dischargeApplies,
      List<CrmContract> pendingBedContracts,
      List<CrmContract> expiryContracts,
      List<ElderAdmissionProgress> returnedAdmissions) {
  }

  public ElderWorkbenchTodoResponse list(String scope) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    LocalDateTime now = LocalDateTime.now();
    Sources sources = loadSources(orgId, staffId, now);

    Set<Long> elderIds = new HashSet<>();
    sources.outings().forEach(r -> elderIds.add(r.getElderId()));
    sources.medicalOutings().forEach(r -> elderIds.add(r.getElderId()));
    sources.dischargeApplies().forEach(r -> elderIds.add(r.getElderId()));
    sources.pendingBedContracts().forEach(r -> elderIds.add(r.getElderId()));
    sources.expiryContracts().forEach(r -> elderIds.add(r.getElderId()));
    sources.returnedAdmissions().forEach(r -> elderIds.add(r.getElderId()));
    elderIds.remove(null);
    Map<Long, ElderProfile> elders = elderIds.isEmpty() ? Map.of()
        : elderMapper.selectBatchIds(elderIds).stream()
            .filter(e -> !Integer.valueOf(1).equals(e.getIsDeleted()))
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));

    Map<Long, CrmLead> leads = loadLeads(sources);
    List<Item> all = buildItems(sources, elders, leads, staffId, now);
    fillOwnerNames(all);
    return summarize(all, scope, now);
  }

  private Sources loadSources(Long orgId, Long staffId, LocalDateTime now) {
    LocalDate today = now.toLocalDate();
    boolean canOuting = elderAuthz.canManageOuting();
    // 退住结算由财务办理，财务也需要看到退住办理超时
    boolean canDischarge = elderAuthz.canManageDischarge() || elderAuthz.canUseFinanceWorkbench();
    boolean canContract = CONTRACT_READ_ROLES.stream().anyMatch(AuthContext::hasRole);

    List<ElderOutingRecord> outings = !canOuting ? List.of()
        : outingMapper.selectList(Wrappers.lambdaQuery(ElderOutingRecord.class)
            .eq(ElderOutingRecord::getIsDeleted, 0)
            .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
            .and(w -> w.eq(ElderOutingRecord::getStatus, ResidenceLifecycleConstants.OUTING_APPLIED)
                .or(o -> o.eq(ElderOutingRecord::getStatus, ResidenceLifecycleConstants.OUTING_OUT)
                    .lt(ElderOutingRecord::getExpectedReturnTime, now))));
    List<ElderMedicalOutingRecord> medicalOutings = !canOuting ? List.of()
        : medicalOutingMapper.selectList(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
            .eq(ElderMedicalOutingRecord::getStatus, ResidenceLifecycleConstants.MEDICAL_OUTING_OUT)
            .lt(ElderMedicalOutingRecord::getExpectedReturnTime, now));
    List<ElderDischargeApply> dischargeApplies = !canDischarge ? List.of()
        : dischargeApplyMapper.selectList(Wrappers.lambdaQuery(ElderDischargeApply.class)
            .eq(ElderDischargeApply::getIsDeleted, 0)
            .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
            .and(w -> w.eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING)
                .or(o -> o.eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED)
                    .isNull(ElderDischargeApply::getArchiveTime)
                    .lt(ElderDischargeApply::getPlannedDischargeDate, today))));
    List<CrmContract> pendingBed = !canContract ? List.of()
        : contractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(orgId != null, CrmContract::getTenantId, orgId)
            .eq(CrmContract::getFlowStage, "PENDING_BED_SELECT"));
    // 与 CrmContractServiceImpl.page 的 expiryReminderTo 口径一致
    List<CrmContract> expiry = !canContract ? List.of()
        : contractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(orgId != null, CrmContract::getTenantId, orgId)
            .in(CrmContract::getStatus, "SIGNED", "EFFECTIVE")
            .isNotNull(CrmContract::getEffectiveTo)
            .le(CrmContract::getEffectiveTo, today.plusDays(CONTRACT_EXPIRING_DAYS))
            .notExists("SELECT 1 FROM crm_contract r WHERE r.tenant_id = crm_contract.tenant_id"
                + " AND r.elder_id = crm_contract.elder_id AND r.is_deleted = 0"
                + " AND r.status IN ('SIGNED', 'EFFECTIVE') AND r.effective_to > crm_contract.effective_to"));
    List<ElderAdmissionProgress> returned = admissionProgressMapper.selectList(
        Wrappers.lambdaQuery(ElderAdmissionProgress.class)
            .eq(ElderAdmissionProgress::getIsDeleted, 0)
            .eq(orgId != null, ElderAdmissionProgress::getOrgId, orgId)
            .eq(ElderAdmissionProgress::getStatus, "RETURNED"));
    return new Sources(outings, medicalOutings, dischargeApplies,
        contractPersonalScope(pendingBed, staffId), contractPersonalScope(expiry, staffId), returned);
  }

  /** 与 CrmContractServiceImpl.applyPersonalScope 一致：市场部普通员工只看自己创建或自己线索下的合同。 */
  private List<CrmContract> contractPersonalScope(List<CrmContract> contracts, Long staffId) {
    if (contracts.isEmpty() || !AuthContext.hasRole("MARKETING_EMPLOYEE") || AuthContext.isMinisterOrHigher()) {
      return contracts;
    }
    Map<Long, CrmLead> leads = selectLeads(contracts.stream().map(CrmContract::getLeadId));
    return contracts.stream().filter(c -> {
      CrmLead lead = get(leads, c.getLeadId());
      return Objects.equals(staffId, c.getCreatedBy())
          || (lead != null && (Objects.equals(staffId, lead.getOwnerStaffId()) || Objects.equals(staffId, lead.getCreatedBy())));
    }).toList();
  }

  private Map<Long, CrmLead> loadLeads(Sources sources) {
    return selectLeads(Stream.concat(sources.pendingBedContracts().stream(), sources.expiryContracts().stream())
        .map(CrmContract::getLeadId));
  }

  private Map<Long, CrmLead> selectLeads(Stream<Long> leadIds) {
    Set<Long> ids = leadIds.filter(Objects::nonNull).collect(Collectors.toSet());
    return ids.isEmpty() ? Map.of() : leadMapper.selectBatchIds(ids).stream()
        .filter(l -> !Integer.valueOf(1).equals(l.getIsDeleted()))
        .collect(Collectors.toMap(CrmLead::getId, Function.identity(), (a, b) -> a));
  }

  private void fillOwnerNames(List<Item> items) {
    Set<Long> staffIds = items.stream().map(Item::getOwnerStaffId).filter(Objects::nonNull).collect(Collectors.toSet());
    if (staffIds.isEmpty()) {
      return;
    }
    Map<Long, String> names = staffMapper.selectBatchIdsSafe(staffIds).stream()
        .filter(s -> !Integer.valueOf(1).equals(s.getIsDeleted()))
        .collect(Collectors.toMap(StaffAccount::getId,
            s -> s.getRealName() == null || s.getRealName().isBlank() ? String.valueOf(s.getUsername()) : s.getRealName(),
            (a, b) -> a));
    items.forEach(item -> item.setOwnerName(names.get(item.getOwnerStaffId())));
  }

  static List<Item> buildItems(Sources s, Map<Long, ElderProfile> elders, Map<Long, CrmLead> leads,
      Long staffId, LocalDateTime now) {
    LocalDate today = now.toLocalDate();
    List<Item> items = new ArrayList<>();
    for (ElderOutingRecord r : s.outings()) {
      ElderProfile elder = get(elders, r.getElderId());
      String path = BED_RECORDS + "OUTING" + elderQuery(r.getElderId());
      if (ResidenceLifecycleConstants.OUTING_OUT.equals(r.getStatus())
          && r.getExpectedReturnTime() != null && r.getExpectedReturnTime().isBefore(now)) {
        items.add(item("OUTING_OVERDUE", URGENT, r.getId(), r.getElderId(), name(r.getElderName(), elder),
            "外出超时未返院", join("应返 " + r.getExpectedReturnTime().toLocalDate(), prefix("陪同 ", r.getCompanion())),
            r.getExpectedReturnTime(), "NURSING", path, elder, staffId, r.getCreatedBy()));
      } else if (ResidenceLifecycleConstants.OUTING_APPLIED.equals(r.getStatus())) {
        items.add(item("OUTING_PENDING", NORMAL, r.getId(), r.getElderId(), name(r.getElderName(), elder),
            "外出申请待审批", join("外出 " + r.getOutingDate(), r.getReason()),
            r.getOutingDate() == null ? null : r.getOutingDate().atStartOfDay(), "NURSING", path, elder, staffId,
            r.getCreatedBy()));
      }
    }
    for (ElderMedicalOutingRecord r : s.medicalOutings()) {
      if (!ResidenceLifecycleConstants.MEDICAL_OUTING_OUT.equals(r.getStatus())
          || r.getExpectedReturnTime() == null || !r.getExpectedReturnTime().isBefore(now)) {
        continue;
      }
      ElderProfile elder = get(elders, r.getElderId());
      items.add(item("MEDICAL_OUTING_OVERDUE", URGENT, r.getId(), r.getElderId(), name(r.getElderName(), elder),
          "外出就医超时未返院", join("应返 " + r.getExpectedReturnTime().toLocalDate(), r.getHospitalName()),
          r.getExpectedReturnTime(), "NURSING", BED_RECORDS + "MEDICAL_OUTING" + elderQuery(r.getElderId()),
          elder, staffId, r.getCreatedBy()));
    }
    for (ElderDischargeApply r : s.dischargeApplies()) {
      ElderProfile elder = get(elders, r.getElderId());
      String path = FINANCE_EXIT + "?tab=discharge" + elderQuery(r.getElderId());
      if (ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING.equals(r.getStatus())) {
        LocalDateTime created = r.getCreateTime() != null ? r.getCreateTime()
            : r.getApplyDate() == null ? null : r.getApplyDate().atStartOfDay();
        LocalDateTime due = created == null ? null : created.plusDays(DISCHARGE_REVIEW_TIMEOUT_DAYS);
        boolean overdue = due != null && due.isBefore(now);
        items.add(item(overdue ? "DISCHARGE_REVIEW_OVERDUE" : "DISCHARGE_PENDING", overdue ? URGENT : HIGH,
            r.getId(), r.getElderId(), name(r.getElderName(), elder), overdue ? "退住审批超时" : "退住申请待审核",
            "计划退住 " + r.getPlannedDischargeDate(), due, "NURSING", path, elder, staffId, r.getCreatedBy()));
      } else if (ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(r.getStatus())
          && r.getArchiveTime() == null && r.getPlannedDischargeDate() != null
          && r.getPlannedDischargeDate().isBefore(today)
          // 交接功能上线前已结算的旧申请视为已办结，与前端 isDischargeInProgress 一致
          && !(ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS.equals(r.getAutoDischargeStatus())
              && r.getItemHandoverTime() == null)) {
        String[] step = dischargeStep(r);
        items.add(item("DISCHARGE_PROCESS_OVERDUE", URGENT, r.getId(), r.getElderId(), name(r.getElderName(), elder),
            "退住办理超时", "计划退住 " + r.getPlannedDischargeDate() + " · " + step[1],
            r.getPlannedDischargeDate().atStartOfDay(), step[0], path, elder, staffId, r.getCreatedBy(), r.getReviewedBy()));
      }
    }
    for (CrmContract c : s.pendingBedContracts()) {
      ElderProfile elder = get(elders, c.getElderId());
      CrmLead lead = get(leads, c.getLeadId());
      String dept = get(DEPARTMENT_NAMES, c.getCurrentOwnerDept()) != null ? c.getCurrentOwnerDept() : "MARKETING";
      items.add(item("CONTRACT_PENDING_BED", NORMAL, c.getId(), c.getElderId(), contractElderName(c, elder),
          "合同待选床入住", join("合同 " + orDash(c.getContractNo()), prefix("预留 ", c.getReservationRoomNo())),
          null, dept, ADMISSION + (c.getContractNo() == null ? "" : "?contractNo=" + c.getContractNo()),
          elder, staffId, c.getCreatedBy(), lead == null ? null : lead.getOwnerStaffId()));
    }
    for (CrmContract c : s.expiryContracts()) {
      if (c.getEffectiveTo() == null || c.getEffectiveTo().isAfter(today.plusDays(CONTRACT_EXPIRING_DAYS))) {
        continue;
      }
      ElderProfile elder = get(elders, c.getElderId());
      CrmLead lead = get(leads, c.getLeadId());
      boolean expired = c.getEffectiveTo().isBefore(today);
      long days = ChronoUnit.DAYS.between(today, c.getEffectiveTo());
      String path = c.getElderId() != null ? "/elder/detail/" + c.getElderId() + "?tab=contract" : FINANCE_EXIT + "?tab=contract";
      items.add(item(expired ? "CONTRACT_EXPIRED" : "CONTRACT_EXPIRING", expired ? URGENT : HIGH, c.getId(),
          c.getElderId(), contractElderName(c, elder), expired ? "合同已到期" : "合同即将到期",
          join("合同 " + orDash(c.getContractNo()), expired ? "已过期 " + (-days) + " 天" : "剩余 " + days + " 天"),
          c.getEffectiveTo().atStartOfDay(), "MARKETING", path, elder, staffId, c.getCreatedBy(),
          lead == null ? null : lead.getOwnerStaffId()));
    }
    for (ElderAdmissionProgress p : s.returnedAdmissions()) {
      ElderProfile elder = get(elders, p.getElderId());
      String path = ADMISSION + "?residentId=" + p.getElderId()
          + (p.getContractNo() == null ? "" : "&contractNo=" + p.getContractNo());
      items.add(item("ADMISSION_RETURNED", HIGH, p.getId(), p.getElderId(), name(null, elder),
          "入住资料退回待补充", join("第 " + p.getCurrentStep() + " 步", p.getReturnReason()),
          null, "MARKETING", path, elder, staffId));
    }
    return items;
  }

  /** 退住办理卡在哪一步，以及该步的责任部门。 */
  private static String[] dischargeStep(ElderDischargeApply r) {
    if (r.getCareHandoverTime() == null) {
      return new String[] {"NURSING", "待护理/医务交接"};
    }
    if (r.getItemHandoverTime() == null) {
      return new String[] {"NURSING", "待物品及房间交接"};
    }
    if (!ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS.equals(r.getAutoDischargeStatus())) {
      return new String[] {"FINANCE", "待费用结清"};
    }
    return new String[] {"NURSING", "待档案归档"};
  }

  static ElderWorkbenchTodoResponse summarize(List<Item> all, String scope, LocalDateTime now) {
    boolean mineOnly = !"all".equalsIgnoreCase(scope);
    List<Item> items = all.stream()
        .filter(item -> !mineOnly || item.isMine())
        .sorted(Comparator.<Item>comparingInt(item -> LEVEL_ORDER.get(item.getLevel()))
            .thenComparing(Item::getDueTime, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
    ElderWorkbenchTodoResponse response = new ElderWorkbenchTodoResponse();
    response.setScope(mineOnly ? "mine" : "all");
    response.setItems(items);
    response.setTotal(items.size());
    response.setUrgentCount(items.stream().filter(i -> URGENT.equals(i.getLevel())).count());
    response.setHighCount(items.stream().filter(i -> HIGH.equals(i.getLevel())).count());
    response.setNormalCount(items.stream().filter(i -> NORMAL.equals(i.getLevel())).count());
    response.setMineCount(all.stream().filter(Item::isMine).count());
    response.setAllCount(all.size());
    response.setGeneratedAt(now);
    return response;
  }

  private static Item item(String type, String level, Long sourceId, Long elderId, String elderName, String title,
      String detail, LocalDateTime dueTime, String department, String path, ElderProfile elder, Long staffId,
      Long... handlers) {
    Long responsible = elder == null ? null : elder.getResponsibleStaffId();
    Item item = new Item();
    item.setId(type + "-" + sourceId);
    item.setType(type);
    item.setLevel(level);
    item.setElderId(elderId);
    item.setElderName(elderName);
    item.setTitle(title);
    item.setDetail(detail);
    item.setDueTime(dueTime);
    item.setDepartment(department);
    item.setDepartmentName(get(DEPARTMENT_NAMES, department));
    item.setPath(path);
    item.setOwnerStaffId(responsible != null ? responsible
        : Stream.of(handlers).filter(Objects::nonNull).findFirst().orElse(null));
    item.setMine(staffId != null
        && (staffId.equals(responsible) || Stream.of(handlers).anyMatch(staffId::equals)));
    return item;
  }

  // Map.of 不接受 null 键
  private static <K, V> V get(Map<K, V> map, K key) {
    return key == null ? null : map.get(key);
  }

  private static String elderQuery(Long elderId) {
    return elderId == null ? "" : "&elderId=" + elderId;
  }

  private static String name(String recordName, ElderProfile elder) {
    if (recordName != null && !recordName.isBlank()) {
      return recordName;
    }
    return elder == null || elder.getFullName() == null ? "--" : elder.getFullName();
  }

  private static String contractElderName(CrmContract c, ElderProfile elder) {
    return name(c.getElderName() != null && !c.getElderName().isBlank() ? c.getElderName() : c.getName(), elder);
  }

  private static String prefix(String label, String value) {
    return value == null || value.isBlank() ? null : label + value;
  }

  private static String orDash(String value) {
    return value == null || value.isBlank() ? "--" : value;
  }

  private static String join(String... parts) {
    return Stream.of(parts).filter(p -> p != null && !p.isBlank()).collect(Collectors.joining(" · "));
  }
}
