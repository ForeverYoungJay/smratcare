package com.zhiyangyun.care.report.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportResponse {
  private Long id;
  private String name;
  private LocalDateTime generatedAt;
  private String url;
}
