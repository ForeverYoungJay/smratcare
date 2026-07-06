package com.zhiyangyun.care.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ai.entity.AiRiskModel;
import com.zhiyangyun.care.ai.entity.AiRiskScore;
import com.zhiyangyun.care.ai.model.AiRiskRecomputeRequest;
import com.zhiyangyun.care.ai.model.AiRiskSummaryResponse;
import com.zhiyangyun.care.ai.model.AiRiskTrendPointResponse;
import com.zhiyangyun.care.ai.service.AiRiskService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** AI 健康风险预测：跌倒/压疮/再入院评分、看板与模型配置。 */
@RestController
@RequestMapping("/api/ai/risk")
@PreAuthorize("hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class AiRiskController {
  private final AiRiskService riskService;

  public AiRiskController(AiRiskService riskService) {
    this.riskService = riskService;
  }

  @GetMapping("/models")
  public Result<List<AiRiskModel>> listModels() {
    return Result.ok(riskService.listModels(AuthContext.getOrgId()));
  }

  @PreAuthorize("hasAnyRole('MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/models")
  public Result<AiRiskModel> saveModel(@RequestBody AiRiskModel request) {
    try {
      return Result.ok(riskService.saveModel(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
    } catch (IllegalArgumentException ex) {
      return Result.error(400, ex.getMessage());
    }
  }

  @PostMapping("/recompute")
  public Result<Map<String, Object>> recompute(@RequestBody(required = false) AiRiskRecomputeRequest request) {
    Long elderId = request == null ? null : request.getElderId();
    String riskType = request == null ? null : request.getRiskType();
    int count = riskService.recompute(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, riskType);
    return Result.ok(Map.of("scoreCount", (Object) count));
  }

  @GetMapping("/scores/page")
  public Result<IPage<AiRiskScore>> pageScores(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String riskType,
      @RequestParam(required = false) String riskLevel,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    return Result.ok(riskService.pageScores(
        AuthContext.getOrgId(), pageNo, pageSize, riskType, riskLevel, elderId, keyword));
  }

  @GetMapping("/scores/summary")
  public Result<AiRiskSummaryResponse> summary() {
    return Result.ok(riskService.summary(AuthContext.getOrgId()));
  }

  @GetMapping("/scores/trend")
  public Result<List<AiRiskTrendPointResponse>> trend(
      @RequestParam Long elderId,
      @RequestParam String riskType,
      @RequestParam(defaultValue = "30") int days) {
    return Result.ok(riskService.trend(AuthContext.getOrgId(), elderId, riskType, days));
  }
}
