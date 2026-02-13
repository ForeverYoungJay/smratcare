package com.zhiyangyun.care.elder.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FamilyUserPageItem {
  private Long id;
  private String realName;
  private String phone;
  private String idCardNo;
  private Integer status;
  private Long elderCount;
  private LocalDateTime createTime;
}
