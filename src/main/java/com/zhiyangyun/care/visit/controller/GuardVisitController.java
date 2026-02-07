package com.zhiyangyun.care.visit.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.model.VisitCheckInRequest;
import com.zhiyangyun.care.visit.model.VisitCheckInResponse;
import com.zhiyangyun.care.visit.service.VisitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guard/visit")
public class GuardVisitController {
  private final VisitService visitService;

  public GuardVisitController(VisitService visitService) {
    this.visitService = visitService;
  }

  @PreAuthorize("hasAnyRole('GUARD','STAFF','ADMIN')")
  @GetMapping("/today")
  public Result<List<VisitBookingResponse>> today() {
    return Result.ok(visitService.todayList(AuthContext.getOrgId()));
  }

  @PreAuthorize("hasAnyRole('GUARD','STAFF','ADMIN')")
  @PostMapping("/checkin")
  public Result<VisitCheckInResponse> checkin(@Valid @RequestBody VisitCheckInRequest request) {
    Long staffId = AuthContext.getStaffId();
    return Result.ok(visitService.checkIn(request, staffId));
  }
}
