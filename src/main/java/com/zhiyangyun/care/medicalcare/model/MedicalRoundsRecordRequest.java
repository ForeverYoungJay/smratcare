package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDateTime;
import lombok.Data;

/** 新增巡诊记录请求（可联动生成病程记录与医嘱）。 */
@Data
public class MedicalRoundsRecordRequest {
  private Long planId;
  private Long elderId;
  private String elderName;
  private LocalDateTime roundTime;
  private String findings;
  private String handleOpinion;
  /** NORMAL / ATTENTION / URGENT。 */
  private String resultLevel;
  private String remark;
  /** 1=同时生成 EMR 病程记录。 */
  private Integer generateEmr;
  /** 巡诊诊断（写入病程记录）。 */
  private String diagnosis;
  /** 填写后同时开立医嘱（医嘱建议内容）。 */
  private String orderContent;
  /** LONG_TERM / TEMPORARY，默认 TEMPORARY。 */
  private String orderType;
  /** DRUG / NURSING / EXAM / DIET，默认 DRUG。 */
  private String orderCategory;
  private String orderDosage;
  private String orderFrequency;
}
