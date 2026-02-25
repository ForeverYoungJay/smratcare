package com.zhiyangyun.care.baseconfig.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.baseconfig.model.BaseConfigGroup;
import com.zhiyangyun.care.baseconfig.model.BaseDataBatchStatusRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportResult;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;
import com.zhiyangyun.care.baseconfig.service.BaseDataItemService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @PutMapping("/batch/status")
  public Result<Integer> batchUpdateStatus(@Valid @RequestBody BaseDataBatchStatusRequest request) {
    Long tenantId = AuthContext.getOrgId();
    int updatedCount = baseDataItemService.batchUpdateStatus(tenantId, request.getIds(), request.getStatus());
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "BASE_DATA_ITEM", null, "批量更新基础数据状态");
    return Result.ok(updatedCount);
  }

  @PostMapping("/import")
  public Result<BaseDataImportResult> importItems(@Valid @RequestBody BaseDataImportRequest request) {
    Long tenantId = AuthContext.getOrgId();
    BaseDataImportResult result = baseDataItemService.importItems(tenantId, tenantId, AuthContext.getStaffId(), request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "IMPORT", "BASE_DATA_ITEM", null, "批量导入基础数据项");
    return Result.ok(result);
  }

  @PostMapping("/import/preview")
  public Result<BaseDataImportResult> previewImport(@Valid @RequestBody BaseDataImportRequest request) {
    Long tenantId = AuthContext.getOrgId();
    BaseDataImportResult result = baseDataItemService.previewImport(tenantId, tenantId, AuthContext.getStaffId(), request);
    return Result.ok(result);
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String configGroup,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long tenantId = AuthContext.getOrgId();
    List<BaseDataItemResponse> items = baseDataItemService.list(tenantId, configGroup, keyword, status);
    List<String> headers = List.of("分组编码", "分组名称", "配置编码", "配置名称", "状态", "排序", "备注");
    List<List<String>> rows = items.stream()
        .map(item -> List.of(
            safe(item.getConfigGroup()),
            safe(item.getConfigGroupLabel()),
            safe(item.getItemCode()),
            safe(item.getItemName()),
            item.getStatus() != null && item.getStatus() == 1 ? "启用" : "停用",
            safe(item.getSortNo()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("base-config-items", headers, rows);
  }

  @GetMapping(value = "/import-template", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> downloadImportTemplate() {
    List<String> headers = List.of("配置编码", "配置名称", "状态", "排序", "备注");
    List<List<String>> rows = List.of(List.of("CHANNEL_WECHAT", "微信渠道", "启用", "10", "示例数据"));
    return csvResponse("base-config-import-template", headers, rows);
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    HttpHeaders headersObj = new HttpHeaders();
    headersObj.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv");
    headersObj.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headersObj.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headersObj).body(bytes);
  }

  private String toCsvLine(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String field : fields) {
      String value = field == null ? "" : field;
      value = value.replace("\"", "\"\"");
      if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
        value = "\"" + value + "\"";
      }
      escaped.add(value);
    }
    return String.join(",", escaped);
  }
}
