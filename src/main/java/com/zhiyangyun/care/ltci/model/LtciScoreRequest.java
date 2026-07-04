package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

/** 提交失能评估打分请求。answers 为二级指标编码到得分的映射。 */
@Data
public class LtciScoreRequest {
  @NotNull
  private Long applyId;
  @NotNull
  private Long elderId;
  /** 为空时使用国家标准默认模板。 */
  private Long templateId;
  private Long assessorId;
  @NotNull
  private Map<String, Double> answers;
  private String remark;
}
