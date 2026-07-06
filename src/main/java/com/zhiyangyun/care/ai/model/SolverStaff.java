package com.zhiyangyun.care.ai.model;

import java.util.List;
import lombok.Data;

/** 求解器输入：一名可参与排班的员工。 */
@Data
public class SolverStaff {
  private Long id;
  private String name;
  /** 角色编码，用于班次资质匹配 */
  private List<String> roleCodes;
}
