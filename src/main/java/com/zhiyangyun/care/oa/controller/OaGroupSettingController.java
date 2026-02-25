package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaGroupSetting;
import com.zhiyangyun.care.oa.mapper.OaGroupSettingMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaGroupSettingRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
@RequestMapping("/api/oa/group-setting")
public class OaGroupSettingController {
  private final OaGroupSettingMapper groupSettingMapper;

  public OaGroupSettingController(OaGroupSettingMapper groupSettingMapper) {
    this.groupSettingMapper = groupSettingMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaGroupSetting>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String groupType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .eq(groupType != null && !groupType.isBlank(), OaGroupSetting::getGroupType, groupType)
        .eq(normalizedStatus != null, OaGroupSetting::getStatus, normalizedStatus);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaGroupSetting::getGroupName, keyword)
          .or().like(OaGroupSetting::getLeaderName, keyword)
          .or().like(OaGroupSetting::getRemark, keyword));
    }
    wrapper.orderByDesc(OaGroupSetting::getCreateTime);
    return Result.ok(groupSettingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaGroupSetting> create(@Valid @RequestBody OaGroupSettingRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateMemberCount(request.getMemberCount());
    OaGroupSetting setting = new OaGroupSetting();
    setting.setTenantId(orgId);
    setting.setOrgId(orgId);
    setting.setGroupName(request.getGroupName());
    setting.setGroupType(request.getGroupType());
    setting.setLeaderId(request.getLeaderId());
    setting.setLeaderName(request.getLeaderName());
    setting.setMemberCount(request.getMemberCount() == null ? 0 : request.getMemberCount());
    String normalizedStatus = normalizeStatus(request.getStatus());
    setting.setStatus(normalizedStatus == null ? "ENABLED" : normalizedStatus);
    setting.setRemark(request.getRemark());
    setting.setCreatedBy(AuthContext.getStaffId());
    groupSettingMapper.insert(setting);
    return Result.ok(setting);
  }

  @PutMapping("/{id}")
  public Result<OaGroupSetting> update(@PathVariable Long id, @Valid @RequestBody OaGroupSettingRequest request) {
    OaGroupSetting setting = findAccessibleSetting(id);
    if (setting == null) {
      return Result.ok(null);
    }
    validateMemberCount(request.getMemberCount());
    setting.setGroupName(request.getGroupName());
    setting.setGroupType(request.getGroupType());
    setting.setLeaderId(request.getLeaderId());
    setting.setLeaderName(request.getLeaderName());
    setting.setMemberCount(request.getMemberCount());
    String normalizedStatus = normalizeStatus(request.getStatus());
    setting.setStatus(normalizedStatus == null ? setting.getStatus() : normalizedStatus);
    setting.setRemark(request.getRemark());
    groupSettingMapper.updateById(setting);
    return Result.ok(setting);
  }

  @PutMapping("/{id}/enable")
  public Result<OaGroupSetting> enable(@PathVariable Long id) {
    OaGroupSetting setting = findAccessibleSetting(id);
    if (setting == null) {
      return Result.ok(null);
    }
    setting.setStatus("ENABLED");
    groupSettingMapper.updateById(setting);
    return Result.ok(setting);
  }

  @PutMapping("/{id}/disable")
  public Result<OaGroupSetting> disable(@PathVariable Long id) {
    OaGroupSetting setting = findAccessibleSetting(id);
    if (setting == null) {
      return Result.ok(null);
    }
    setting.setStatus("DISABLED");
    groupSettingMapper.updateById(setting);
    return Result.ok(setting);
  }

  @PutMapping("/batch/enable")
  public Result<Integer> batchEnable(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaGroupSetting> list = groupSettingMapper.selectList(Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .in(OaGroupSetting::getId, ids));
    for (OaGroupSetting item : list) {
      item.setStatus("ENABLED");
      groupSettingMapper.updateById(item);
    }
    return Result.ok(list.size());
  }

  @PutMapping("/batch/disable")
  public Result<Integer> batchDisable(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaGroupSetting> list = groupSettingMapper.selectList(Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .in(OaGroupSetting::getId, ids));
    for (OaGroupSetting item : list) {
      item.setStatus("DISABLED");
      groupSettingMapper.updateById(item);
    }
    return Result.ok(list.size());
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaGroupSetting> list = groupSettingMapper.selectList(Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .in(OaGroupSetting::getId, ids));
    for (OaGroupSetting item : list) {
      item.setIsDeleted(1);
      groupSettingMapper.updateById(item);
    }
    return Result.ok(list.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String groupType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .eq(groupType != null && !groupType.isBlank(), OaGroupSetting::getGroupType, groupType)
        .eq(normalizedStatus != null, OaGroupSetting::getStatus, normalizedStatus)
        .orderByDesc(OaGroupSetting::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaGroupSetting::getGroupName, keyword)
          .or().like(OaGroupSetting::getLeaderName, keyword)
          .or().like(OaGroupSetting::getRemark, keyword));
    }
    List<OaGroupSetting> list = groupSettingMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "分组名称", "分组类型", "组长", "成员数", "状态", "备注");
    List<List<String>> rows = list.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getGroupName()),
            safe(item.getGroupType()),
            safe(item.getLeaderName()),
            safe(item.getMemberCount()),
            safe(item.getStatus()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-group-setting", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaGroupSetting setting = findAccessibleSetting(id);
    if (setting != null) {
      setting.setIsDeleted(1);
      groupSettingMapper.updateById(setting);
    }
    return Result.ok(null);
  }

  private OaGroupSetting findAccessibleSetting(Long id) {
    Long orgId = AuthContext.getOrgId();
    return groupSettingMapper.selectOne(Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getId, id)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateMemberCount(Integer memberCount) {
    if (memberCount != null && memberCount < 0) {
      throw new IllegalArgumentException("memberCount 不能小于 0");
    }
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"ENABLED".equals(normalized) && !"DISABLED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 ENABLED/DISABLED");
    }
    return normalized;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
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
