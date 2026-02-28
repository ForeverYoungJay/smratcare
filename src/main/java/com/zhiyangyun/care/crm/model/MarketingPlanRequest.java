package com.zhiyangyun.care.crm.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class MarketingPlanRequest {
  @NotBlank(message = "模块类型不能为空")
  @Pattern(regexp = "SPEECH|POLICY", message = "模块类型仅支持 SPEECH 或 POLICY")
  private String moduleType;

  @NotBlank(message = "标题不能为空")
  @Size(max = 128, message = "标题长度不能超过 128")
  private String title;

  @Size(max = 255, message = "摘要长度不能超过 255")
  private String summary;

  private String content;

  @Size(max = 32, message = "季度标签长度不能超过 32")
  private String quarterLabel;

  @Size(max = 255, message = "运营目标长度不能超过 255")
  private String target;

  @Size(max = 64, message = "责任部门长度不能超过 64")
  private String owner;

  @Min(value = 1, message = "优先级最小为 1")
  @Max(value = 999, message = "优先级最大为 999")
  private Integer priority;

  @Pattern(
      regexp = "DRAFT|PENDING_APPROVAL|REJECTED|APPROVED|PUBLISHED|ACTIVE|INACTIVE",
      message = "状态仅支持 DRAFT/PENDING_APPROVAL/REJECTED/APPROVED/PUBLISHED/ACTIVE/INACTIVE")
  private String status;

  private LocalDate effectiveDate;

  private List<Long> linkedDepartmentIds;
}
