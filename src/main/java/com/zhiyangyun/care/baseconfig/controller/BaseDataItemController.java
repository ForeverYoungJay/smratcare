package com.zhiyangyun.care.baseconfig.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.baseconfig.model.BaseConfigGroup;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;
import com.zhiyangyun.care.baseconfig.service.BaseDataItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/base-config/items")
public class BaseDataItemController {
  private final BaseDataItemService baseDataItemService;
  private final AuditLogService auditLogService;

  public BaseDataItemController(BaseDataItemService baseDataItemService, AuditLogService auditLogService) {
    this.baseDataItemService = baseDataItemService;
    this.auditLogService = auditLogService;
  }

  @PostMapping
  public Result<BaseDataItemResponse> create(@Valid @RequestBody BaseDataItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    BaseDataItemResponse response = baseDataItemService.create(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "BASE_DATA_ITEM", response == null ? null : response.getId(), "创建基础数据项");
    return Result.ok(response);
  }

  @PutMapping("/{id}")
  public Result<BaseDataItemResponse> update(@PathVariable Long id, @Valid @RequestBody BaseDataItemRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    BaseDataItemResponse response = baseDataItemService.update(id, request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "BASE_DATA_ITEM", id, "更新基础数据项");
    return Result.ok(response);
  }

  @GetMapping("/page")
  public Result<IPage<BaseDataItemResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String configGroup,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(baseDataItemService.page(tenantId, pageNo, pageSize, configGroup, keyword, status));
  }

  @GetMapping("/list")
  public Result<List<BaseDataItemResponse>> list(
      @RequestParam(required = false) String configGroup,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(baseDataItemService.list(tenantId, configGroup, status));
  }

  @GetMapping("/groups")
  public Result<List<Map<String, String>>> groups() {
    return Result.ok(BaseConfigGroup.options());
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    baseDataItemService.delete(id, tenantId);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "BASE_DATA_ITEM", id, "删除基础数据项");
    return Result.ok(null);
  }
}
