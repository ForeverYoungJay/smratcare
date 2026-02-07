package com.zhiyangyun.care.visit.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family/visit")
public class FamilyVisitController {
  private final VisitService visitService;

  public FamilyVisitController(VisitService visitService) {
    this.visitService = visitService;
  }

  @PostMapping("/book")
  public Result<VisitBookingResponse> book(@Valid @RequestBody VisitBookRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    request.setFamilyUserId(AuthContext.getStaffId());
    return Result.ok(visitService.book(request));
  }

  @GetMapping("/my")
  public Result<java.util.List<VisitBookingResponse>> my() {
    return Result.ok(visitService.listByFamily(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }
}
