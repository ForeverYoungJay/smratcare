package com.zhiyangyun.care.ltci.service;

import com.zhiyangyun.care.ltci.model.LtciGradingResult;
import java.util.Map;

/**
 * 长护险失能等级判级内核（纯逻辑）。
 *
 * <p>依据《长期护理失能等级评估标准（试行）》：3 个一级指标（日常生活活动能力/认知能力/
 * 感知觉与沟通）+ 17 个二级指标，采用组合法判定失能等级 0-5。判级规则外置于模板 JSON，
 * 标准修订时只改模板不改代码。
 */
public interface LtciGradingService {

  /**
   * 依据模板指标定义与组合规则，对逐项作答进行判级。
   *
   * @param indicatorsJson 模板指标定义（dimensions[]）
   * @param combineRuleJson 组合判级规则（primary/bands/escalation）
   * @param answers 二级指标编码到得分的映射
   * @return 三维得分与最终失能等级
   * @throws IllegalArgumentException 模板或作答非法
   */
  LtciGradingResult judge(String indicatorsJson, String combineRuleJson, Map<String, Double> answers);
}
