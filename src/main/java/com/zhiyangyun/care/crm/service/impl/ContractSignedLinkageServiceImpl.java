package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.model.ContractSystemLinkageSummary;
import com.zhiyangyun.care.crm.service.ContractSignedLinkageService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.life.entity.HealthBasicRecord;
import com.zhiyangyun.care.life.mapper.HealthBasicRecordMapper;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.service.CareTaskService;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ContractSignedLinkageServiceImpl implements ContractSignedLinkageService {
  private static final String SIGNED_INSPECTION_ITEM = "新签约长者入院健康巡检";
  private static final String SIGNED_INSPECTION_RESULT = "待首次巡检";
  private static final String SIGNED_INSPECTION_STATUS = "FOLLOWING";
  private static final String SIGNED_CARE_TASK_NAME = "新签约长者护理交接";
  private static final String SIGNED_CARE_TASK_SOURCE = "CONTRACT_SIGNED";

  private final ElderAdmissionMapper admissionMapper;
  private final ElderMapper elderMapper;
  private final ElderAccountService elderAccountService;
  private final ElderPointsAccountMapper pointsAccountMapper;
  private final BillService billService;
  private final HealthBasicRecordMapper healthBasicRecordMapper;
  private final HealthInspectionMapper healthInspectionMapper;
  private final CareTaskService careTaskService;
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public ContractSignedLinkageServiceImpl(
      ElderAdmissionMapper admissionMapper,
      ElderMapper elderMapper,
      ElderAccountService elderAccountService,
      ElderPointsAccountMapper pointsAccountMapper,
      BillService billService,
      HealthBasicRecordMapper healthBasicRecordMapper,
      HealthInspectionMapper healthInspectionMapper,
      CareTaskService careTaskService,
      CareTaskDailyMapper careTaskDailyMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper) {
    this.admissionMapper = admissionMapper;
    this.elderMapper = elderMapper;
    this.elderAccountService = elderAccountService;
    this.pointsAccountMapper = pointsAccountMapper;
    this.billService = billService;
    this.healthBasicRecordMapper = healthBasicRecordMapper;
    this.healthInspectionMapper = healthInspectionMapper;
    this.careTaskService = careTaskService;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  @Transactional
  public ContractSystemLinkageSummary ensureSignedLinkage(CrmContract contract, Long operatorId) {
    ContractSystemLinkageSummary summary = new ContractSystemLinkageSummary();
    if (contract == null) {
      return summary;
    }

    ElderAdmission admission = resolveAdmission(contract);
    ElderProfile elder = resolveElder(contract, admission);
    if (elder == null) {
      summary.setElderArchiveReady(Boolean.FALSE);
      summary.setFinanceAccountReady(Boolean.FALSE);
      summary.setFinanceBillReady(Boolean.FALSE);
      summary.setLogisticsReady(Boolean.FALSE);
      summary.setMedicalRecordReady(Boolean.FALSE);
      return summary;
    }

    Long orgId = firstNonNull(contract.getOrgId(), contract.getTenantId(), elder.getOrgId());
    Long elderId = elder.getId();
    String elderName = firstNonBlank(contract.getElderName(), elder.getFullName());
    if (contract.getElderId() == null) {
      contract.setElderId(elderId);
    }
    if (isBlank(contract.getElderName()) && !isBlank(elderName)) {
      contract.setElderName(elderName);
    }
    if (isBlank(contract.getElderPhone()) && !isBlank(elder.getPhone())) {
      contract.setElderPhone(elder.getPhone());
    }
    backfillReservationInfo(contract, elder);

    summary.setElderId(elderId);
    summary.setElderName(elderName);
    summary.setElderArchiveReady(Boolean.TRUE);

    if (orgId == null) {
      summary.setFinanceAccountReady(Boolean.FALSE);
      summary.setFinanceBillReady(Boolean.FALSE);
      summary.setLogisticsReady(Boolean.TRUE);
      summary.setMedicalRecordReady(Boolean.FALSE);
      return summary;
    }

    elderAccountService.getOrCreate(orgId, elderId, operatorId);
    summary.setFinanceAccountReady(Boolean.TRUE);

    ensurePointsAccount(orgId, elderId);

    String billMonth = resolveBillMonth(contract, admission, elder);
    BillDetailResponse billDetail = billService.ensureMonthlyBillForElder(
        orgId,
        elderId,
        billMonth,
        contract.getId(),
        contract.getContractNo());
    summary.setBillMonth(billMonth);
    summary.setFinanceBillReady(billDetail.getBillId() != null);
    summary.setBillId(billDetail.getBillId());
    summary.setBillItemCount(billDetail.getItems() == null ? 0 : billDetail.getItems().size());
    summary.setBillTotalAmount(billDetail.getTotalAmount());

    LocalDate recordDate = resolveHealthRecordDate(contract, admission, elder);
    ensureHealthBasicRecord(orgId, elderId, elderName, operatorId, recordDate);
    summary.setHealthRecordDate(recordDate);
    summary.setMedicalRecordReady(Boolean.TRUE);

    HealthInspection starterInspection = ensureStarterInspection(orgId, elderId, elderName, contract, operatorId, recordDate);
    summary.setMedicalInspectionReady(starterInspection != null);
    summary.setStarterInspectionId(starterInspection == null ? null : starterInspection.getId());
    summary.setStarterInspectionDate(starterInspection == null ? null : starterInspection.getInspectionDate());

    CareTaskDaily starterCareTask = ensureStarterCareTask(orgId, elderId, contract, operatorId);
    summary.setMedicalCareTaskReady(starterCareTask != null);
    summary.setStarterCareTaskId(starterCareTask == null ? null : starterCareTask.getId());
    summary.setStarterCareTaskDate(starterCareTask == null ? null : starterCareTask.getTaskDate());

    summary.setLogisticsReady(Boolean.TRUE);
    return summary;
  }

  private ElderAdmission resolveAdmission(CrmContract contract) {
    String contractNo = trimToNull(contract.getContractNo());
    if (contractNo != null) {
      ElderAdmission admission = admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
          .eq(ElderAdmission::getIsDeleted, 0)
          .eq(contract.getTenantId() != null, ElderAdmission::getTenantId, contract.getTenantId())
          .eq(contract.getOrgId() != null, ElderAdmission::getOrgId, contract.getOrgId())
          .eq(ElderAdmission::getContractNo, contractNo)
          .orderByDesc(ElderAdmission::getAdmissionDate)
          .orderByDesc(ElderAdmission::getCreateTime)
          .last("LIMIT 1"));
      if (admission != null) {
        return admission;
      }
      return admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
          .eq(ElderAdmission::getIsDeleted, 0)
          .eq(contract.getTenantId() != null, ElderAdmission::getTenantId, contract.getTenantId())
          .eq(contract.getOrgId() != null, ElderAdmission::getOrgId, contract.getOrgId())
          .apply("TRIM(contract_no) = {0}", contractNo)
          .orderByDesc(ElderAdmission::getAdmissionDate)
          .orderByDesc(ElderAdmission::getCreateTime)
          .last("LIMIT 1"));
    }
    if (contract.getElderId() == null) {
      return null;
    }
    return admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getIsDeleted, 0)
        .eq(contract.getTenantId() != null, ElderAdmission::getTenantId, contract.getTenantId())
        .eq(contract.getOrgId() != null, ElderAdmission::getOrgId, contract.getOrgId())
        .eq(ElderAdmission::getElderId, contract.getElderId())
        .orderByDesc(ElderAdmission::getAdmissionDate)
        .orderByDesc(ElderAdmission::getCreateTime)
        .last("LIMIT 1"));
  }

  private ElderProfile resolveElder(CrmContract contract, ElderAdmission admission) {
    Long elderId = firstNonNull(contract.getElderId(), admission == null ? null : admission.getElderId());
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null) {
      return null;
    }
    if (contract.getOrgId() == null && elder.getOrgId() != null) {
      contract.setOrgId(elder.getOrgId());
    }
    if (contract.getTenantId() == null && elder.getOrgId() != null) {
      contract.setTenantId(elder.getOrgId());
    }
    return elder;
  }

  private void ensurePointsAccount(Long orgId, Long elderId) {
    ElderPointsAccount account = pointsAccountMapper.selectOne(Wrappers.lambdaQuery(ElderPointsAccount.class)
        .eq(ElderPointsAccount::getIsDeleted, 0)
        .eq(ElderPointsAccount::getOrgId, orgId)
        .eq(ElderPointsAccount::getElderId, elderId)
        .last("LIMIT 1"));
    if (account != null) {
      return;
    }
    account = new ElderPointsAccount();
    account.setOrgId(orgId);
    account.setElderId(elderId);
    account.setPointsBalance(0);
    account.setStatus(1);
    pointsAccountMapper.insert(account);
  }

  private void ensureHealthBasicRecord(Long orgId, Long elderId, String elderName, Long operatorId, LocalDate recordDate) {
    HealthBasicRecord existing = healthBasicRecordMapper.selectOne(Wrappers.lambdaQuery(HealthBasicRecord.class)
        .eq(HealthBasicRecord::getIsDeleted, 0)
        .eq(HealthBasicRecord::getOrgId, orgId)
        .eq(HealthBasicRecord::getElderId, elderId)
        .orderByDesc(HealthBasicRecord::getRecordDate)
        .orderByDesc(HealthBasicRecord::getCreateTime)
        .last("LIMIT 1"));
    if (existing != null) {
      return;
    }
    HealthBasicRecord record = new HealthBasicRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elderId);
    record.setElderName(elderName);
    record.setRecordDate(recordDate == null ? LocalDate.now() : recordDate);
    record.setRemark("最终签约自动建档");
    record.setCreatedBy(operatorId);
    healthBasicRecordMapper.insert(record);
  }

  private HealthInspection ensureStarterInspection(
      Long orgId,
      Long elderId,
      String elderName,
      CrmContract contract,
      Long operatorId,
      LocalDate inspectionDate) {
    String remark = buildSignedInspectionRemark(contract);
    HealthInspection existing = healthInspectionMapper.selectOne(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(HealthInspection::getOrgId, orgId)
        .eq(HealthInspection::getElderId, elderId)
        .eq(HealthInspection::getInspectionItem, SIGNED_INSPECTION_ITEM)
        .eq(HealthInspection::getRemark, remark)
        .orderByDesc(HealthInspection::getInspectionDate)
        .orderByDesc(HealthInspection::getCreateTime)
        .last("LIMIT 1"));
    if (existing != null) {
      return existing;
    }
    HealthInspection inspection = new HealthInspection();
    inspection.setTenantId(orgId);
    inspection.setOrgId(orgId);
    inspection.setElderId(elderId);
    inspection.setElderName(elderName);
    inspection.setInspectionDate(inspectionDate == null ? LocalDate.now() : inspectionDate);
    inspection.setInspectionItem(SIGNED_INSPECTION_ITEM);
    inspection.setResult(SIGNED_INSPECTION_RESULT);
    inspection.setStatus(SIGNED_INSPECTION_STATUS);
    inspection.setInspectorName("待分派");
    inspection.setFollowUpAction("签约后 24 小时内完成首次健康巡检并记录基础生命体征");
    inspection.setOtherNote("最终签约后自动生成首检任务");
    inspection.setRemark(remark);
    inspection.setCreatedBy(operatorId);
    inspection.setIsDeleted(0);
    healthInspectionMapper.insert(inspection);
    return inspection;
  }

  private CareTaskDaily ensureStarterCareTask(
      Long orgId,
      Long elderId,
      CrmContract contract,
      Long operatorId) {
    if (contract.getId() != null) {
      CareTaskDaily existing = careTaskDailyMapper.selectOne(Wrappers.lambdaQuery(CareTaskDaily.class)
          .eq(CareTaskDaily::getIsDeleted, 0)
          .eq(CareTaskDaily::getOrgId, orgId)
          .eq(CareTaskDaily::getElderId, elderId)
          .eq(CareTaskDaily::getSourceType, SIGNED_CARE_TASK_SOURCE)
          .eq(CareTaskDaily::getSourceId, contract.getId())
          .orderByDesc(CareTaskDaily::getTaskDate)
          .orderByDesc(CareTaskDaily::getCreateTime)
          .last("LIMIT 1"));
      if (existing != null) {
        return existing;
      }
    }
    CareTaskCreateRequest request = new CareTaskCreateRequest();
    request.setElderId(elderId);
    request.setTaskName(SIGNED_CARE_TASK_NAME);
    request.setPlanTime(resolveStarterCareTaskTime(contract).format(DateTimeFormatter.ISO_DATE_TIME));
    request.setStatus("PENDING");
    Long taskId = careTaskService.createTask(orgId, request);
    if (taskId == null) {
      return null;
    }
    CareTaskDaily task = careTaskDailyMapper.selectById(taskId);
    if (task == null) {
      return null;
    }
    task.setSourceType(SIGNED_CARE_TASK_SOURCE);
    task.setSourceId(contract.getId());
    task.setCreatedBy(operatorId);
    careTaskDailyMapper.updateById(task);
    return task;
  }

  private void backfillReservationInfo(CrmContract contract, ElderProfile elder) {
    Long bedId = firstNonNull(contract.getReservationBedId(), elder == null ? null : elder.getBedId());
    if (bedId == null) {
      return;
    }
    if (contract.getReservationBedId() == null) {
      contract.setReservationBedId(bedId);
    }
    if (!isBlank(contract.getReservationRoomNo())) {
      return;
    }
    Bed bed = bedMapper.selectById(bedId);
    if (bed == null || bed.getRoomId() == null) {
      return;
    }
    Room room = roomMapper.selectById(bed.getRoomId());
    if (room != null && !isBlank(room.getRoomNo())) {
      contract.setReservationRoomNo(room.getRoomNo());
    }
  }

  private String resolveBillMonth(CrmContract contract, ElderAdmission admission, ElderProfile elder) {
    LocalDate pivotDate = contract.getSignedAt() == null ? null : contract.getSignedAt().toLocalDate();
    if (pivotDate == null && admission != null) {
      pivotDate = admission.getAdmissionDate();
    }
    if (pivotDate == null && elder != null) {
      pivotDate = elder.getAdmissionDate();
    }
    if (pivotDate == null) {
      pivotDate = LocalDate.now();
    }
    return YearMonth.from(pivotDate).toString();
  }

  private LocalDate resolveHealthRecordDate(CrmContract contract, ElderAdmission admission, ElderProfile elder) {
    if (contract.getSignedAt() != null) {
      return contract.getSignedAt().toLocalDate();
    }
    if (admission != null && admission.getAdmissionDate() != null) {
      return admission.getAdmissionDate();
    }
    if (elder != null && elder.getAdmissionDate() != null) {
      return elder.getAdmissionDate();
    }
    return LocalDate.now();
  }

  private LocalDateTime resolveStarterCareTaskTime(CrmContract contract) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime signedAt = contract == null ? null : contract.getSignedAt();
    LocalDateTime base = signedAt != null && signedAt.isAfter(now.minusHours(4)) ? signedAt : now;
    return base.withMinute(0).withSecond(0).withNano(0).plusHours(1);
  }

  private String buildSignedInspectionRemark(CrmContract contract) {
    String contractNo = trimToNull(contract == null ? null : contract.getContractNo());
    return contractNo == null
        ? "最终签约自动生成首检"
        : "最终签约自动生成首检#" + contractNo;
  }

  private static <T> T firstNonNull(T first, T second, T third) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    return third;
  }

  private static String firstNonBlank(String first, String second) {
    if (!isBlank(first)) {
      return first.trim();
    }
    if (!isBlank(second)) {
      return second.trim();
    }
    return null;
  }

  private static String trimToNull(String value) {
    return isBlank(value) ? null : value.trim();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
