package com.zhiyangyun.care.medicalcare.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupRecordRequest;
import java.time.LocalDate;
import java.util.List;

/** 慢病随访服务。 */
public interface MedicalFollowupService {

  IPage<MedicalFollowupPlan> pagePlans(Long orgId, int pageNo, int pageSize, Long elderId, String diseaseType,
      String status, Boolean dueOnly);

  MedicalFollowupPlan createPlan(Long orgId, MedicalFollowupPlanRequest request);

  MedicalFollowupPlan updatePlan(Long orgId, Long planId, MedicalFollowupPlanRequest request);

  MedicalFollowupPlan updatePlanStatus(Long orgId, Long planId, String status);

  IPage<MedicalFollowupRecord> pageRecords(Long orgId, int pageNo, int pageSize, Long planId, Long elderId);

  /** 新增随访记录：回写计划的上次/下次随访日期（下次随访日期按频次自动排程）。 */
  MedicalFollowupRecord createRecord(Long orgId, MedicalFollowupRecordRequest request);

  /** 到期（含逾期）随访计划列表。 */
  List<MedicalFollowupPlan> listDuePlans(Long orgId, LocalDate date);

  /** 为到期随访计划生成护理待办（care_task_daily，sourceType=CHRONIC_FOLLOWUP），返回新建条数。 */
  int generateDueReminders(LocalDate date);
}
