package com.zhiyangyun.care.elder.model;

import lombok.Data;

@Data
public class BedResponse {
  private Long id;
  private Long orgId;
  private Long roomId;
  private String bedNo;
  private String bedQrCode;
  private Integer status;
  private Long elderId;
}
