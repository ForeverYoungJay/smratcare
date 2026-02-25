package com.zhiyangyun.care.crm.model.action;

import lombok.Data;

@Data
public class CrmContractAttachmentRequest {
  private String contractNo;
  private String fileName;
  private String fileUrl;
  private String fileType;
  private Long fileSize;
  private String remark;
}
