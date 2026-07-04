package com.zhiyangyun.care.compliance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.service.SensitiveAccessService;
import com.zhiyangyun.care.compliance.util.DataMaskingUtil;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 长者敏感信息受控读取示例：默认脱敏返回并留痕；reveal=true 需管理岗，按 DECRYPT 留痕并二次校验。
 * 演示"脱敏 + 最小权限 + 访问审计"闭环，可复用到其它敏感数据展示场景。
 */
@RestController
@RequestMapping("/api/compliance/elder")
public class ElderSensitiveInfoController {

  private final ElderMapper elderMapper;
  private final SensitiveAccessService sensitiveAccessService;

  public ElderSensitiveInfoController(ElderMapper elderMapper,
      SensitiveAccessService sensitiveAccessService) {
    this.elderMapper = elderMapper;
    this.sensitiveAccessService = sensitiveAccessService;
  }

  @GetMapping("/{id}/sensitive")
  @PreAuthorize("hasAnyRole('SYS_ADMIN','DIRECTOR','MEDICAL_MINISTER','NURSING_MINISTER','MEDICAL_EMPLOYEE','NURSING_EMPLOYEE')")
  public Result<Map<String, Object>> sensitive(
      @PathVariable Long id,
      @RequestParam(defaultValue = "false") boolean reveal,
      @RequestParam(required = false) String purpose) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted())) {
      return Result.error(404, "长者不存在");
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(elder.getOrgId())) {
      sensitiveAccessService.record("VIEW", "ELDER_IDCARD", "ELDER", id,
          DataMaskingUtil.maskName(elder.getFullName()), "idCardNo,phone", purpose, false);
      return Result.error(403, "无权访问该机构长者信息");
    }

    // reveal（明文）仅限管理岗，且按 DECRYPT 留痕
    boolean canReveal = reveal && (AuthContext.hasRole("SYS_ADMIN") || AuthContext.hasRole("DIRECTOR"));
    if (reveal && !canReveal) {
      sensitiveAccessService.record("DECRYPT", "ELDER_IDCARD", "ELDER", id,
          DataMaskingUtil.maskName(elder.getFullName()), "idCardNo,phone", purpose, false);
      return Result.error(403, "仅管理层可查看明文敏感信息");
    }

    Map<String, Object> data = new LinkedHashMap<>();
    data.put("id", elder.getId());
    data.put("elderCode", elder.getElderCode());
    if (canReveal) {
      data.put("fullName", elder.getFullName());
      data.put("idCardNo", elder.getIdCardNo());
      data.put("phone", elder.getPhone());
      data.put("homeAddress", elder.getHomeAddress());
      data.put("masked", false);
      sensitiveAccessService.record("DECRYPT", "ELDER_IDCARD", "ELDER", id,
          DataMaskingUtil.maskName(elder.getFullName()), "idCardNo,phone,homeAddress", purpose, true);
    } else {
      data.put("fullName", DataMaskingUtil.maskName(elder.getFullName()));
      data.put("idCardNo", DataMaskingUtil.maskIdCard(elder.getIdCardNo()));
      data.put("phone", DataMaskingUtil.maskPhone(elder.getPhone()));
      data.put("homeAddress", DataMaskingUtil.maskAddress(elder.getHomeAddress()));
      data.put("masked", true);
      sensitiveAccessService.record("VIEW", "ELDER_IDCARD", "ELDER", id,
          DataMaskingUtil.maskName(elder.getFullName()), "idCardNo,phone,homeAddress", purpose, true);
    }
    return Result.ok(data);
  }
}
