package com.zhiyangyun.care.nursing.model;

import com.zhiyangyun.care.elder.model.ElderResponse;
import java.time.LocalDate;
import lombok.Data;

/** 护理档案列表的一行：长者资料 + 最新一次正式评估 */
@Data
public class NursingResidentItem {
  private ElderResponse elder;
  private String latestLevelCode;
  private LocalDate latestAssessmentDate;
  private LocalDate nextAssessmentDate;
}
