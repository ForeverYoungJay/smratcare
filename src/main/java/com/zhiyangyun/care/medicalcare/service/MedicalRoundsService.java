package com.zhiyangyun.care.medicalcare.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsRecord;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsRecordRequest;

/** 医生巡诊服务。 */
public interface MedicalRoundsService {

  IPage<MedicalRoundsPlan> pagePlans(Long orgId, int pageNo, int pageSize, Long doctorId, String status,
      String dateFrom, String dateTo);

  MedicalRoundsPlan createPlan(Long orgId, MedicalRoundsPlanRequest request);

  MedicalRoundsPlan updatePlanStatus(Long orgId, Long planId, String status);

  IPage<MedicalRoundsRecord> pageRecords(Long orgId, int pageNo, int pageSize, Long planId, Long elderId,
      String resultLevel);

  MedicalRoundsRecord createRecord(Long orgId, MedicalRoundsRecordRequest request);
}
