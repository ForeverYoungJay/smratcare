package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.service.ElderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elder")
public class ElderController {
  private final ElderService elderService;

  public ElderController(ElderService elderService) {
    this.elderService = elderService;
  }

  @PostMapping
  public Result<ElderResponse> create(@Valid @RequestBody ElderCreateRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    return Result.ok(elderService.create(request));
  }

  @PutMapping("/{id}")
  public Result<ElderResponse> update(@PathVariable Long id, @Valid @RequestBody ElderUpdateRequest request) {
    return Result.ok(elderService.update(id, request));
  }

  @GetMapping("/{id}")
  public Result<ElderResponse> get(@PathVariable Long id) {
    return Result.ok(elderService.get(id));
  }

  @GetMapping("/page")
  public Result<IPage<ElderResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(elderService.page(pageNo, pageSize, keyword));
  }

  @PostMapping("/{id}/assignBed")
  public Result<ElderResponse> assignBed(@PathVariable Long id, @Valid @RequestBody AssignBedRequest request) {
    return Result.ok(elderService.assignBed(id, request));
  }

  @PostMapping("/{id}/unbindBed")
  public Result<ElderResponse> unbindBed(@PathVariable Long id,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) String reason) {
    return Result.ok(elderService.unbindBed(id, endDate, reason));
  }
}
