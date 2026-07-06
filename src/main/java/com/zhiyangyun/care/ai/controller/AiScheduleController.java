package com.zhiyangyun.care.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.ai.entity.AiScheduleConstraint;
import com.zhiyangyun.care.ai.model.AiScheduleAdoptResponse;
import com.zhiyangyun.care.ai.model.AiScheduleConstraintRequest;
import com.zhiyangyun.care.ai.model.AiScheduleGenerateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleItemUpdateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleProposalItemResponse;
import com.zhiyangyun.care.ai.model.AiScheduleProposalResponse;
import com.zhiyangyun.care.ai.service.AiScheduleService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** AI 智能排班：方案生成 → 人工调整 → 一键采纳写回 staff_schedule。 */
@RestController
@RequestMapping("/api/ai/schedule")
@PreAuthorize("hasAnyRole('NURSING_MINISTER','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class AiScheduleController {
  private final AiScheduleService scheduleService;

  public AiScheduleController(AiScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @PostMapping("/proposals/generate")
  public Result<AiScheduleProposalResponse> generate(@Valid @RequestBody AiScheduleGenerateRequest request) {
    try {
      return Result.ok(scheduleService.generate(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
    } catch (IllegalArgumentException | IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    }
  }

  @GetMapping("/proposals/page")
  public Result<IPage<AiScheduleProposalResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status) {
    return Result.ok(scheduleService.page(AuthContext.getOrgId(), pageNo, pageSize, status));
  }

  @GetMapping("/proposals/{id}")
  public Result<AiScheduleProposalResponse> detail(@PathVariable Long id) {
    AiScheduleProposalResponse response = scheduleService.detail(AuthContext.getOrgId(), id);
    if (response == null) {
      return Result.error(404, "方案不存在");
    }
    return Result.ok(response);
  }

  @PutMapping("/proposals/{id}/items/{itemId}")
  public Result<AiScheduleProposalItemResponse> updateItem(
      @PathVariable Long id, @PathVariable Long itemId, @RequestBody AiScheduleItemUpdateRequest request) {
    try {
      AiScheduleProposalItemResponse response =
          scheduleService.updateItem(AuthContext.getOrgId(), AuthContext.getStaffId(), id, itemId, request);
      if (response == null) {
        return Result.error(404, "排班明细不存在");
      }
      return Result.ok(response);
    } catch (IllegalArgumentException | IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    }
  }

  @DeleteMapping("/proposals/{id}/items/{itemId}")
  public Result<Void> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
    try {
      scheduleService.deleteItem(AuthContext.getOrgId(), id, itemId);
      return Result.ok(null);
    } catch (IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    }
  }

  @PostMapping("/proposals/{id}/adopt")
  public Result<AiScheduleAdoptResponse> adopt(@PathVariable Long id) {
    try {
      AiScheduleAdoptResponse response =
          scheduleService.adopt(AuthContext.getOrgId(), AuthContext.getStaffId(), id);
      if (response == null) {
        return Result.error(404, "方案不存在");
      }
      return Result.ok(response);
    } catch (IllegalStateException ex) {
      return Result.error(400, ex.getMessage());
    }
  }

  @DeleteMapping("/proposals/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    scheduleService.delete(AuthContext.getOrgId(), id);
    return Result.ok(null);
  }

  @GetMapping("/constraint")
  public Result<AiScheduleConstraint> getConstraint() {
    return Result.ok(scheduleService.getConstraint(AuthContext.getOrgId()));
  }

  @PostMapping("/constraint")
  public Result<AiScheduleConstraint> saveConstraint(@RequestBody AiScheduleConstraintRequest request) {
    return Result.ok(scheduleService.saveConstraint(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }
}
