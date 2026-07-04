package com.zhiyangyun.care.emr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.emr.entity.EmrRecord;
import com.zhiyangyun.care.emr.mapper.EmrRecordMapper;
import com.zhiyangyun.care.emr.model.EmrRecordRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 电子病历接口。 */
@RestController
@RequestMapping("/api/medical/emr")
public class EmrRecordController {

  private static final String OPERATOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final EmrRecordMapper emrMapper;

  public EmrRecordController(EmrRecordMapper emrMapper) {
    this.emrMapper = emrMapper;
  }

  @GetMapping("/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<EmrRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String recordType) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(EmrRecord.class)
        .eq(EmrRecord::getIsDeleted, 0)
        .eq(orgId != null, EmrRecord::getOrgId, orgId)
        .eq(elderId != null, EmrRecord::getElderId, elderId)
        .eq(recordType != null && !recordType.isBlank(), EmrRecord::getRecordType, recordType)
        .orderByDesc(EmrRecord::getVisitDate)
        .orderByDesc(EmrRecord::getId);
    return Result.ok(emrMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  @PreAuthorize(VIEWER)
  public Result<EmrRecord> get(@PathVariable("id") Long id) {
    return Result.ok(emrMapper.selectById(id));
  }

  @PostMapping
  @PreAuthorize(OPERATOR)
  public Result<EmrRecord> save(@Valid @RequestBody EmrRecordRequest request) {
    EmrRecord record = new EmrRecord();
    BeanUtils.copyProperties(request, record);
    Long orgId = AuthContext.getOrgId();
    if (request.getId() != null) {
      EmrRecord exist = emrMapper.selectById(request.getId());
      if (exist == null || Integer.valueOf(1).equals(exist.getIsDeleted())) {
        return Result.error(404, "病历不存在");
      }
      if (orgId != null && !orgId.equals(exist.getOrgId())) {
        return Result.error(403, "无权编辑其他机构的病历");
      }
      record.setId(request.getId());
      record.setUpdateTime(LocalDateTime.now());
      emrMapper.updateById(record);
    } else {
      record.setOrgId(orgId);
      record.setDoctorId(AuthContext.getStaffId());
      record.setStatus("ACTIVE");
      record.setCreatedBy(AuthContext.getStaffId());
      emrMapper.insert(record);
    }
    return Result.ok(record);
  }
}
