package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.NursingResidentItem;
import com.zhiyangyun.care.nursing.service.NursingResidentService;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nursing/residents")
public class NursingResidentController {
  private final NursingResidentService residentService;

  public NursingResidentController(NursingResidentService residentService) {
    this.residentService = residentService;
  }

  @PreAuthorize("hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/page")
  public Result<IPage<NursingResidentItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") boolean reassessDue) {
    return Result.ok(residentService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword, reassessDue, LocalDate.now()));
  }
}
