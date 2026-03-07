package com.zhiyangyun.care.elder.service.impl.lifecycle;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRecordResponse;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ChangeLogResponse;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeResponse;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderLifecycleServiceImpl implements ElderLifecycleService {
  private final ElderAdmissionMapper admissionMapper;
  private final ElderDischargeMapper dischargeMapper;
  private final ElderChangeLogMapper changeLogMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;
  private final RoomMapper roomMapper;
  private final ElderService elderService;
  private final ElderAccountService accountService;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderPointsAccountMapper pointsAccountMapper;
  private final CrmContractMapper crmContractMapper;

  public ElderLifecycleServiceImpl(ElderAdmissionMapper admissionMapper,
                                   ElderDischargeMapper dischargeMapper,
                                   ElderChangeLogMapper changeLogMapper,
                                   ElderMapper elderMapper,
                                   BedMapper bedMapper,
                                   ElderBedRelationMapper relationMapper,
                                   RoomMapper roomMapper,
                                   ElderService elderService,
                                   ElderAccountService accountService,
                                   BillMonthlyMapper billMonthlyMapper,
                                   ElderPointsAccountMapper pointsAccountMapper,
                                   CrmContractMapper crmContractMapper) {
    this.admissionMapper = admissionMapper;
    this.dischargeMapper = dischargeMapper;
    this.changeLogMapper = changeLogMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.roomMapper = roomMapper;
    this.elderService = elderService;
    this.accountService = accountService;
    this.billMonthlyMapper = billMonthlyMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.crmContractMapper = crmContractMapper;
  }

  @Override
  @Transactional
  public AdmissionResponse admit(AdmissionRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (!hasElderAccess(elder, request.getTenantId(), request.getOrgId())) {
      throw new IllegalArgumentException("老人不存在或无权限");
    }
    backfillElderTenantScope(elder, request.getTenantId(), request.getOrgId());
    String normalizedContractNo = request.getContractNo() == null ? null : request.getContractNo().trim();
    request.setContractNo(normalizedContractNo);
    CrmContract contract = validateAdmissionContractGuard(request, elder);
    ElderAdmission admission = new ElderAdmission();
    admission.setTenantId(request.getTenantId());
    admission.setOrgId(request.getOrgId());
    admission.setElderId(request.getElderId());
    admission.setAdmissionDate(request.getAdmissionDate());
    admission.setContractNo(normalizedContractNo);
    admission.setDepositAmount(request.getDepositAmount());
    admission.setRemark(request.getRemark());
    admission.setCreatedBy(request.getCreatedBy());
    admissionMapper.insert(admission);

    if (elder.getAdmissionDate() == null || !elder.getAdmissionDate().equals(request.getAdmissionDate())) {
      ElderChangeLog change = new ElderChangeLog();
      change.setTenantId(request.getTenantId());
      change.setOrgId(request.getOrgId());
      change.setElderId(request.getElderId());
      change.setChangeType("ADMISSION");
      change.setBeforeValue(elder.getAdmissionDate() == null ? null : elder.getAdmissionDate().toString());
      change.setAfterValue(request.getAdmissionDate().toString());
      change.setReason("入院办理");
      change.setCreatedBy(request.getCreatedBy());
      changeLogMapper.insert(change);
    }

    elder.setAdmissionDate(request.getAdmissionDate());
    elder.setStatus(1);
    elderMapper.updateById(elder);

    if (request.getBedId() != null
        && (elder.getBedId() == null || !request.getBedId().equals(elder.getBedId()))) {
      AssignBedRequest assign = new AssignBedRequest();
      assign.setTenantId(request.getTenantId());
      assign.setCreatedBy(request.getCreatedBy());
      assign.setBedId(request.getBedId());
      LocalDate startDate = request.getBedStartDate();
      if (startDate == null) {
        startDate = request.getAdmissionDate() == null ? LocalDate.now() : request.getAdmissionDate();
      }
      assign.setStartDate(startDate);
      elderService.assignBed(request.getElderId(), assign);
    }

    accountService.getOrCreate(request.getOrgId(), request.getElderId(), request.getCreatedBy());
    if (request.getDepositAmount() != null && request.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
      ElderAccountAdjustRequest adjust = new ElderAccountAdjustRequest();
      adjust.setElderId(request.getElderId());
      adjust.setAmount(request.getDepositAmount());
      adjust.setDirection("CREDIT");
      adjust.setRemark("入院押金");
      accountService.adjust(request.getOrgId(), request.getCreatedBy(), adjust);
    }
    ensurePointsAccount(request.getOrgId(), request.getElderId());
    syncContractAfterAdmission(contract, request);

    AdmissionResponse response = new AdmissionResponse();
    response.setId(admission.getId());
    response.setTenantId(admission.getTenantId());
    response.setOrgId(admission.getOrgId());
    response.setElderId(admission.getElderId());
    response.setAdmissionDate(admission.getAdmissionDate());
    response.setContractNo(admission.getContractNo());
    response.setDepositAmount(admission.getDepositAmount());
    response.setRemark(admission.getRemark());
    return response;
  }

  private CrmContract validateAdmissionContractGuard(AdmissionRequest request, ElderProfile elder) {
    String contractNo = request.getContractNo() == null ? null : request.getContractNo().trim();
    if (contractNo == null || contractNo.isBlank()) {
      throw new IllegalStateException("请先填写合同号并完成入住评估");
    }
    CrmContract contract = crmContractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(CrmContract::getTenantId, request.getTenantId())
        .eq(CrmContract::getContractNo, contractNo)
        .orderByDesc(CrmContract::getUpdateTime)
        .last("LIMIT 1"));
    if (contract == null) {
      throw new IllegalStateException("合同不存在，请先完成合同签约");
    }
    if ("VOID".equals(contract.getStatus())) {
      throw new IllegalStateException("作废合同不可办理入住");
    }
    if ("SIGNED".equals(contract.getStatus()) || "EFFECTIVE".equals(contract.getStatus()) || "SIGNED".equals(contract.getFlowStage())) {
      throw new IllegalStateException("合同已完成最终签署，无需重复办理入住");
    }
    if ("PENDING_ASSESSMENT".equals(contract.getFlowStage())) {
      throw new IllegalStateException("合同尚未完成入住评估，请先完成评估");
    }
    if (contract.getElderId() != null && !contract.getElderId().equals(elder.getId())) {
      throw new IllegalStateException("合同绑定老人和当前办理老人不一致");
    }
    return contract;
  }

  private void syncContractAfterAdmission(CrmContract contract, AdmissionRequest request) {
    if (contract == null) {
      return;
    }
    boolean changed = false;
    if (contract.getElderId() == null && request.getElderId() != null) {
      contract.setElderId(request.getElderId());
      changed = true;
    }
    if (request.getBedId() != null) {
      if (contract.getReservationBedId() == null) {
        contract.setReservationBedId(request.getBedId());
        changed = true;
      }
      if (contract.getReservationRoomNo() == null || contract.getReservationRoomNo().isBlank()) {
        Bed bed = bedMapper.selectById(request.getBedId());
        if (bed != null && bed.getRoomId() != null) {
          Room room = roomMapper.selectById(bed.getRoomId());
          if (room != null && room.getRoomNo() != null && !room.getRoomNo().isBlank()) {
            contract.setReservationRoomNo(room.getRoomNo());
            changed = true;
          }
        }
      }
    }
    if (!"PENDING_SIGN".equals(contract.getFlowStage())) {
      contract.setFlowStage("PENDING_SIGN");
      changed = true;
    }
    if (!"MARKETING".equals(contract.getCurrentOwnerDept())) {
      contract.setCurrentOwnerDept("MARKETING");
      changed = true;
    }
    if (!"待签署".equals(contract.getContractStatus())) {
      contract.setContractStatus("待签署");
      changed = true;
    }
    if (changed) {
      crmContractMapper.updateById(contract);
    }
  }

  @Override
  @Transactional
  public DischargeResponse discharge(DischargeRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (!hasElderAccess(elder, request.getTenantId(), request.getOrgId())) {
      throw new IllegalArgumentException("老人不存在或无权限");
    }
    backfillElderTenantScope(elder, request.getTenantId(), request.getOrgId());
    long unpaid = billMonthlyMapper.selectCount(Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(request.getOrgId() != null, BillMonthly::getOrgId, request.getOrgId())
        .eq(BillMonthly::getElderId, request.getElderId())
        .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO));
    if (unpaid > 0) {
      throw new IllegalStateException("存在未结清账单，无法办理出院");
    }
    ElderDischarge discharge = new ElderDischarge();
    discharge.setTenantId(request.getTenantId());
    discharge.setOrgId(request.getOrgId());
    discharge.setElderId(request.getElderId());
    discharge.setDischargeDate(request.getDischargeDate());
    discharge.setReason(request.getReason());
    discharge.setSettleAmount(request.getSettleAmount());
    discharge.setRemark(request.getRemark());
    discharge.setCreatedBy(request.getCreatedBy());
    dischargeMapper.insert(discharge);

    ElderChangeLog change = new ElderChangeLog();
    change.setTenantId(request.getTenantId());
    change.setOrgId(request.getOrgId());
    change.setElderId(request.getElderId());
    change.setChangeType("DISCHARGE");
    change.setBeforeValue(elder.getStatus() == null ? null : elder.getStatus().toString());
    change.setAfterValue("3");
    change.setReason(request.getReason());
    change.setCreatedBy(request.getCreatedBy());
    changeLogMapper.insert(change);

    Long previousBedId = elder.getBedId();
    elder.setStatus(3);
    elder.setBedId(null);
    elderMapper.updateById(elder);
    if (previousBedId != null) {
      insertChangeLog(request.getTenantId(), request.getOrgId(), request.getElderId(), request.getCreatedBy(),
          "BED_CHANGE", String.valueOf(previousBedId), null, "退院释放床位");
    }

    ElderBedRelation active = relationMapper.selectOne(Wrappers.lambdaQuery(ElderBedRelation.class)
        .eq(ElderBedRelation::getIsDeleted, 0)
        .eq(ElderBedRelation::getActiveFlag, 1)
        .eq(ElderBedRelation::getElderId, elder.getId()));
    if (active != null) {
      active.setActiveFlag(0);
      active.setEndDate(request.getDischargeDate());
      relationMapper.updateById(active);
    }

    if (previousBedId != null) {
      Bed bed = bedMapper.selectById(previousBedId);
      if (bed != null) {
        bed.setElderId(null);
        bed.setStatus(1);
        bedMapper.updateById(bed);
      }
    }

    DischargeResponse response = new DischargeResponse();
    response.setId(discharge.getId());
    response.setTenantId(discharge.getTenantId());
    response.setOrgId(discharge.getOrgId());
    response.setElderId(discharge.getElderId());
    response.setDischargeDate(discharge.getDischargeDate());
    response.setReason(discharge.getReason());
    response.setSettleAmount(discharge.getSettleAmount());
    response.setRemark(discharge.getRemark());
    return response;
  }

  @Override
  public IPage<ChangeLogResponse> changeLogs(
      Long tenantId,
      long pageNo,
      long pageSize,
      Long elderId,
      String changeType,
      String reason,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    LocalDateTime actualStartTime = startTime;
    LocalDateTime actualEndTime = endTime;
    if (actualStartTime != null && actualEndTime != null && actualStartTime.isAfter(actualEndTime)) {
      LocalDateTime temp = actualStartTime;
      actualStartTime = actualEndTime;
      actualEndTime = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderChangeLog.class)
        .eq(tenantId != null, ElderChangeLog::getTenantId, tenantId)
        .eq(elderId != null, ElderChangeLog::getElderId, elderId)
        .eq(changeType != null && !changeType.isBlank(), ElderChangeLog::getChangeType, changeType)
        .like(reason != null && !reason.isBlank(), ElderChangeLog::getReason, reason)
        .ge(actualStartTime != null, ElderChangeLog::getCreateTime, actualStartTime)
        .le(actualEndTime != null, ElderChangeLog::getCreateTime, actualEndTime)
        .orderByDesc(ElderChangeLog::getCreateTime);
    IPage<ElderChangeLog> page = changeLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> elderIds = page.getRecords().stream()
        .map(ElderChangeLog::getElderId)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? java.util.Map.of()
        : elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
            .in(ElderProfile::getId, elderIds)
            .eq(ElderProfile::getIsDeleted, 0))
            .stream()
            .collect(java.util.stream.Collectors.toMap(ElderProfile::getId, item -> item, (a, b) -> a));
    return page.convert(log -> {
      ChangeLogResponse response = new ChangeLogResponse();
      response.setId(log.getId());
      response.setTenantId(log.getTenantId());
      response.setOrgId(log.getOrgId());
      response.setElderId(log.getElderId());
      ElderProfile elder = elderMap.get(log.getElderId());
      response.setElderName(elder == null ? null : elder.getFullName());
      response.setChangeType(log.getChangeType());
      response.setBeforeValue(log.getBeforeValue());
      response.setAfterValue(log.getAfterValue());
      response.setReason(log.getReason());
      response.setCreateTime(log.getCreateTime() == null ? null : log.getCreateTime().toString());
      return response;
    });
  }

  @Override
  public IPage<AdmissionRecordResponse> admissionPage(Long tenantId, long pageNo, long pageSize,
      String keyword, String contractNo, Integer elderStatus, LocalDate admissionDateStart, LocalDate admissionDateEnd) {
    LocalDate actualStartDate = admissionDateStart;
    LocalDate actualEndDate = admissionDateEnd;
    if (actualStartDate != null && actualEndDate != null && actualStartDate.isAfter(actualEndDate)) {
      LocalDate temp = actualStartDate;
      actualStartDate = actualEndDate;
      actualEndDate = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getIsDeleted, 0)
        .eq(tenantId != null, ElderAdmission::getTenantId, tenantId)
        .like(contractNo != null && !contractNo.isBlank(), ElderAdmission::getContractNo, contractNo)
        .ge(actualStartDate != null, ElderAdmission::getAdmissionDate, actualStartDate)
        .le(actualEndDate != null, ElderAdmission::getAdmissionDate, actualEndDate)
        .orderByDesc(ElderAdmission::getAdmissionDate)
        .orderByDesc(ElderAdmission::getCreateTime);

    if ((keyword != null && !keyword.isBlank()) || elderStatus != null) {
      var elderWrapper = Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
          .like(keyword != null && !keyword.isBlank(), ElderProfile::getFullName, keyword)
          .eq(elderStatus != null, ElderProfile::getStatus, elderStatus);
      List<Long> elderIds = elderMapper.selectList(elderWrapper).stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
      wrapper.in(ElderAdmission::getElderId, elderIds);
    }

    IPage<ElderAdmission> page = admissionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> elderIds = page.getRecords().stream()
        .map(ElderAdmission::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
            .in(ElderProfile::getId, elderIds)
            .eq(ElderProfile::getIsDeleted, 0))
            .stream()
            .collect(java.util.stream.Collectors.toMap(ElderProfile::getId, item -> item, (a, b) -> a));

    List<AdmissionRecordResponse> records = page.getRecords().stream().map(item -> {
      AdmissionRecordResponse response = new AdmissionRecordResponse();
      response.setId(item.getId());
      response.setElderId(item.getElderId());
      response.setContractNo(item.getContractNo());
      response.setAdmissionDate(item.getAdmissionDate());
      response.setDepositAmount(item.getDepositAmount());
      response.setRemark(item.getRemark());
      response.setCreateTime(item.getCreateTime());
      ElderProfile elder = elderMap.get(item.getElderId());
      response.setElderName(elder == null ? null : elder.getFullName());
      response.setElderStatus(elder == null ? null : elder.getStatus());
      return response;
    }).toList();

    IPage<AdmissionRecordResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(records);
    return resp;
  }

  private void insertChangeLog(Long tenantId, Long orgId, Long elderId, Long createdBy, String changeType,
                               String beforeValue, String afterValue, String reason) {
    ElderChangeLog log = new ElderChangeLog();
    log.setTenantId(tenantId);
    log.setOrgId(orgId);
    log.setElderId(elderId);
    log.setChangeType(changeType);
    log.setBeforeValue(beforeValue);
    log.setAfterValue(afterValue);
    log.setReason(reason);
    log.setCreatedBy(createdBy);
    changeLogMapper.insert(log);
  }

  private void ensurePointsAccount(Long orgId, Long elderId) {
    ElderPointsAccount account = pointsAccountMapper.selectOne(Wrappers.lambdaQuery(ElderPointsAccount.class)
        .eq(ElderPointsAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderPointsAccount::getOrgId, orgId)
        .eq(ElderPointsAccount::getElderId, elderId));
    if (account != null) {
      return;
    }
    ElderPointsAccount created = new ElderPointsAccount();
    created.setOrgId(orgId);
    created.setElderId(elderId);
    created.setPointsBalance(0);
    created.setStatus(1);
    pointsAccountMapper.insert(created);
    }
  }

  private boolean hasElderAccess(ElderProfile elder, Long tenantId, Long orgId) {
    if (elder == null) {
      return false;
    }
    Long elderTenantId = elder.getTenantId();
    Long elderOrgId = elder.getOrgId();
    if (tenantId != null
        && (Objects.equals(tenantId, elderTenantId) || Objects.equals(tenantId, elderOrgId))) {
      return true;
    }
    if (orgId != null
        && (Objects.equals(orgId, elderOrgId) || Objects.equals(orgId, elderTenantId))) {
      return true;
    }
    return false;
  }

  private void backfillElderTenantScope(ElderProfile elder, Long tenantId, Long orgId) {
    if (elder == null) {
      return;
    }
    boolean changed = false;
    if (elder.getTenantId() == null && tenantId != null) {
      elder.setTenantId(tenantId);
      changed = true;
    }
    if (elder.getOrgId() == null && orgId != null) {
      elder.setOrgId(orgId);
      changed = true;
    }
    if (changed) {
      elderMapper.updateById(elder);
    }
  }
