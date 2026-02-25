package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaGroupSetting;
import com.zhiyangyun.care.oa.mapper.OaGroupSettingMapper;
import com.zhiyangyun.care.oa.model.OaGroupSettingRequest;
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
    var wrapper = Wrappers.lambdaQuery(OaGroupSetting.class)
        .eq(OaGroupSetting::getIsDeleted, 0)
        .eq(orgId != null, OaGroupSetting::getOrgId, orgId)
        .eq(groupType != null && !groupType.isBlank(), OaGroupSetting::getGroupType, groupType)
        .eq(status != null && !status.isBlank(), OaGroupSetting::getStatus, status);
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
    OaGroupSetting setting = new OaGroupSetting();
    setting.setTenantId(orgId);
    setting.setOrgId(orgId);
    setting.setGroupName(request.getGroupName());
    setting.setGroupType(request.getGroupType());
    setting.setLeaderId(request.getLeaderId());
    setting.setLeaderName(request.getLeaderName());
    setting.setMemberCount(request.getMemberCount() == null ? 0 : request.getMemberCount());
    setting.setStatus(request.getStatus() == null ? "ENABLED" : request.getStatus());
    setting.setRemark(request.getRemark());
    setting.setCreatedBy(AuthContext.getStaffId());
    groupSettingMapper.insert(setting);
    return Result.ok(setting);
  }

  @PutMapping("/{id}")
  public Result<OaGroupSetting> update(@PathVariable Long id, @Valid @RequestBody OaGroupSettingRequest request) {
    OaGroupSetting setting = groupSettingMapper.selectById(id);
    if (setting == null) {
      return Result.ok(null);
    }
    setting.setGroupName(request.getGroupName());
    setting.setGroupType(request.getGroupType());
    setting.setLeaderId(request.getLeaderId());
    setting.setLeaderName(request.getLeaderName());
    setting.setMemberCount(request.getMemberCount());
    setting.setStatus(request.getStatus());
    setting.setRemark(request.getRemark());
    groupSettingMapper.updateById(setting);
    return Result.ok(setting);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaGroupSetting setting = groupSettingMapper.selectById(id);
    if (setting != null) {
      setting.setIsDeleted(1);
      groupSettingMapper.updateById(setting);
    }
    return Result.ok(null);
  }
}
