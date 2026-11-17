package com.zhiyangyun.care.elder.model.lifecycle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 长者工作台待办：服务端汇总，不截断条数。 */
@Data
public class ElderWorkbenchTodoResponse {
  private String scope;
  /** 当前 scope 下的条目，按 urgent → high → normal、截止时间早的在前。 */
  private List<Item> items = new ArrayList<>();
  /** 当前 scope 下的数量 */
  private long total;
  private long urgentCount;
  private long highCount;
  private long normalCount;
  /** 与 scope 无关：我负责的 / 全部 的条数，前端据此决定默认页签 */
  private long mineCount;
  private long allCount;
  private LocalDateTime generatedAt;

  @Data
  public static class Item {
    /** type-来源ID，前端列表 key */
    private String id;
    private String type;
    /** urgent | high | normal */
    private String level;
    private Long elderId;
    private String elderName;
    private String title;
    private String detail;
    private LocalDateTime dueTime;
    /** 责任部门编码，与前端 DEPARTMENT_CODES 一致 */
    private String department;
    private String departmentName;
    private Long ownerStaffId;
    private String ownerName;
    private String path;
    private boolean mine;
  }
}
