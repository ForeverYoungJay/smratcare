package com.zhiyangyun.care.hr.model;

import java.util.List;
import lombok.Data;

@Data
public class HrStaffElectricityImportRequest {
  private String month;
  private List<HrStaffElectricityImportRowRequest> rows;
}
