package com.zhiyangyun.care.compliance.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 导出二次确认结果：一次性导出凭证。
 */
@Data
public class ExportTicketResponse {
  private String exportTicket;
  private LocalDateTime expiresAt;
}
