package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

/** 新建时 title/ownerId/dueDate 必填；更新时为空的字段保持不变。 */
@Data
public class NursingRectificationRequest {
  @Size(max = 255, message = "title too long")
  private String title;
  @Size(max = 64, message = "source too long")
  private String source;
  private Long ownerId;
  private String ownerName;
  private LocalDate dueDate;
  private String evidence;
  @Pattern(regexp = "^(OPEN|IN_PROGRESS|PENDING_REVIEW)?$", message = "invalid status")
  private String status;
}
