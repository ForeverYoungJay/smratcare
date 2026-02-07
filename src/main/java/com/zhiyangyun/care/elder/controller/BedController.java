package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.model.BedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.service.BedService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/bed")
public class BedController {
  private final BedService bedService;

  public BedController(BedService bedService) {
    this.bedService = bedService;
  }

  @PostMapping
  public Result<BedResponse> create(@Valid @RequestBody BedRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    return Result.ok(bedService.create(request));
  }

  @PutMapping("/{id}")
  public Result<BedResponse> update(@PathVariable Long id, @Valid @RequestBody BedRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    return Result.ok(bedService.update(id, request));
  }

  @GetMapping("/{id}")
  public Result<BedResponse> get(@PathVariable Long id) {
    return Result.ok(bedService.get(id));
  }

  @GetMapping
  public Result<IPage<BedResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    return Result.ok(bedService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, status));
  }

  @GetMapping("/list")
  public Result<java.util.List<BedResponse>> list() {
    return Result.ok(bedService.list(AuthContext.getOrgId()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    bedService.delete(id);
    return Result.ok(null);
  }
}
