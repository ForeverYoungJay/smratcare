package com.zhiyangyun.care.elder.service.impl.lifecycle;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
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
import java.util.List;
import java.util.Map;
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
  private final ElderService elderService;
  private final ElderAccountService accountService;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderPointsAccountMapper pointsAccountMapper;

  public ElderLifecycleServiceImpl(ElderAdmissionMapper admissionMapper,
                                   ElderDischargeMapper dischargeMapper,
                                   ElderChangeLogMapper changeLogMapper,
                                   ElderMapper elderMapper,
                                   BedMapper bedMapper,
                                   ElderBedRelationMapper relationMapper,
                                   ElderService elderService,
                                   ElderAccountService accountService,
                                   BillMonthlyMapper billMonthlyMapper,
                                   ElderPointsAccountMapper pointsAccountMapper) {
    this.admissionMapper = admissionMapper;
    this.dischargeMapper = dischargeMapper;
    this.changeLogMapper = changeLogMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.elderService = elderService;
    this.accountService = accountService;
    this.billMonthlyMapper = billMonthlyMapper;
    this.pointsAccountMapper = pointsAccountMapper;
  }

  @Override
  @Transactional
  public AdmissionResponse admit(AdmissionRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null || !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalArgumentException("老人不存在或无权限");
    }
    ElderAdmission admission = new ElderAdmission();
    admission.setTenantId(request.getTenantId());
    admission.setOrgId(request.getOrgId());
    admission.setElderId(request.getElderId());
    admission.setAdmissionDate(request.getAdmissionDate());
    admission.setContractNo(request.getContractNo());
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

  @Override
  @Transactional
  public DischargeResponse discharge(DischargeRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null || !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalArgumentException("老人不存在或无权限");
    }
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
  public IPage<ChangeLogResponse> changeLogs(Long tenantId, long pageNo, long pageSize, Long elderId) {
    var wrapper = Wrappers.lambdaQuery(ElderChangeLog.class)
        .eq(tenantId != null, ElderChangeLog::getTenantId, tenantId)
        .eq(elderId != null, ElderChangeLog::getElderId, elderId)
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
