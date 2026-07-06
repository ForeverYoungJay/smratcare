package com.zhiyangyun.care.ai.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/** 智能排班：生成方案请求。 */
@Data
public class AiScheduleGenerateRequest {
  private String title;
  /** 目标科室/部门（可空 = 全员工） */
  private Long deptId;
  private String deptName;
  @NotNull
  private LocalDate dateFrom;
  @NotNull
  private LocalDate dateTo;
  /** 参与排班的员工（可空 = 按部门/全机构在职员工） */
  private List<Long> staffIds;
  /** 参与排班的班次模板编码（shift_template.shift_code，可空 = 全部启用班次） */
  private List<String> shiftCodes;
  private String remark;
}
