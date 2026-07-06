package com.zhiyangyun.care.elder.model;

import com.zhiyangyun.care.compliance.annotation.SensitiveField;
import com.zhiyangyun.care.compliance.annotation.SensitiveType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FamilyUserPageItem {
  private Long id;
  private String realName;
  @SensitiveField(type = SensitiveType.PHONE)
  private String phone;
  @SensitiveField(type = SensitiveType.ID_CARD)
  private String idCardNo;
  private Integer status;
  private Long elderCount;
  private LocalDateTime createTime;
}
