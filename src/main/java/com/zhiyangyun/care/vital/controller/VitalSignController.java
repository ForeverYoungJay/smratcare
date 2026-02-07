package com.zhiyangyun.care.vital.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.vital.model.VitalRecordRequest;
import com.zhiyangyun.care.vital.model.VitalRecordResponse;
import com.zhiyangyun.care.vital.service.VitalSignService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vital")
public class VitalSignController {
  private final VitalSignService vitalSignService;

  public VitalSignController(VitalSignService vitalSignService) {
    this.vitalSignService = vitalSignService;
  }

  @PostMapping("/record")
  public Result<VitalRecordResponse> record(@Valid @RequestBody VitalRecordRequest request) {
    return Result.ok(vitalSignService.record(request));
  }

  @GetMapping("/elder/{elderId}")
  public Result<List<VitalRecordResponse>> list(
      @PathVariable Long elderId,
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      @RequestParam(required = false) String type) {
    return Result.ok(vitalSignService.listByElder(elderId, from, to, type));
  }

  @GetMapping("/elder/{elderId}/latest")
  public Result<VitalRecordResponse> latest(@PathVariable Long elderId) {
    return Result.ok(vitalSignService.latest(elderId));
  }

  @GetMapping("/abnormal/today")
  public Result<List<VitalRecordResponse>> abnormalToday() {
    return Result.ok(vitalSignService.abnormalToday());
  }
}
