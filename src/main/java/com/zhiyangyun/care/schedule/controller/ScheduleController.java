package com.zhiyangyun.care.schedule.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import com.zhiyangyun.care.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<ScheduleResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortOrder) {
    LocalDate from = dateFrom == null || dateFrom.isBlank() ? null : LocalDate.parse(dateFrom);
    LocalDate to = dateTo == null || dateTo.isBlank() ? null : LocalDate.parse(dateTo);
    return Result.ok(scheduleService.page(AuthContext.getOrgId(), pageNo, pageSize, staffId, from, to, status, sortBy, sortOrder));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest request) {
    return Result.ok(scheduleService.create(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ScheduleResponse> update(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
    request.setId(id);
    ScheduleResponse response = scheduleService.update(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    if (response == null) {
      return Result.error(404, "Schedule not found");
    }
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    scheduleService.delete(AuthContext.getOrgId(), id);
    return Result.ok(null);
  }
}
