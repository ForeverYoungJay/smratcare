package com.zhiyangyun.care.compliance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.compliance.entity.ExportLog;
import com.zhiyangyun.care.compliance.model.ExportConfirmRequest;
import com.zhiyangyun.care.compliance.model.ExportTicketResponse;
import java.time.LocalDateTime;

/**
 * 导出二次确认与留痕：先 confirm 签发一次性 exportTicket，导出完成后 complete 核销并回填行数。
 */
public interface ExportAuditService {

  /** 签发一次性导出凭证并留痕（PENDING）。 */
  ExportTicketResponse confirm(ExportConfirmRequest request);

  /** 核销凭证（仅本人、未使用、未过期），回填实际行数并按 EXPORT 记入敏感访问审计。 */
  void complete(String exportTicket, Long rowCount);

  /** 导出留痕分页查询（审计页）。 */
  IPage<ExportLog> page(Long orgId, String module, String status, LocalDateTime start, LocalDateTime end,
                        long pageNo, long pageSize);
}
