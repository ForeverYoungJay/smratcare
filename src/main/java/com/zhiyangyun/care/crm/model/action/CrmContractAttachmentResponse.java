package com.zhiyangyun.care.crm.model.action;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmContractAttachmentResponse {
  private Long id;
  private Long leadId;
  private String contractNo;
  private String fileName;
  private String fileUrl;
  private String fileType;
  private Long fileSize;
  private String remark;
  private LocalDateTime createTime;
}
