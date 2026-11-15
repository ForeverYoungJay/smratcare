package com.zhiyangyun.care.crm.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;
import lombok.Data;

@Data
public class CrmLeadDuplicateResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  private String name;
  private String phone;
  private String ownerStaffName;
  private Integer status;
  private boolean accessible;
  private List<String> reasons;
}
