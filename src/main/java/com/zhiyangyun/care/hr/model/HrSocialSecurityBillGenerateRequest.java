package com.zhiyangyun.care.hr.model;

import java.util.List;
import lombok.Data;

@Data
public class HrSocialSecurityBillGenerateRequest {
  private String month;
  private List<Long> staffIds;
}
