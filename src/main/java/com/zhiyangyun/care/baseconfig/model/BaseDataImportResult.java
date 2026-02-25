package com.zhiyangyun.care.baseconfig.model;

import java.util.List;
import lombok.Data;

@Data
public class BaseDataImportResult {
  private Boolean preview;
  private Integer totalCount;
  private Integer successCount;
  private Integer createCount;
  private Integer updateCount;
  private Integer failCount;
  private List<String> errors;
  private List<BaseDataImportErrorItem> errorItems;
}
