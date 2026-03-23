package com.zhiyangyun.care.auth.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StaffCredentialResponse {
  private Long staffId;
  private String staffNo;
  private String username;
  private String realName;
  private Integer status;
  private String passwordPlaintextSnapshot;
  private LocalDateTime passwordSnapshotUpdatedAt;
}
