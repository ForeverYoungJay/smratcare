package com.zhiyangyun.care.nursing.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.zhiyangyun.care.nursing.entity.CareIncident;
import com.zhiyangyun.care.nursing.entity.CareIncidentAction;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 事件字段平铺 + 处置记录 actions（时间正序）。 */
@Data
@AllArgsConstructor
public class CareIncidentDetailResponse {
  @JsonUnwrapped
  private CareIncident incident;
  private List<CareIncidentAction> actions;
}
