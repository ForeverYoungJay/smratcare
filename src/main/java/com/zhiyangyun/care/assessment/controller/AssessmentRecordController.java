package com.zhiyangyun.care.assessment.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.assessment.model.AssessmentRecordRequest;
import com.zhiyangyun.care.assessment.model.AssessmentRecordResponse;
import com.zhiyangyun.care.assessment.model.AssessmentScoreResult;
import com.zhiyangyun.care.assessment.service.AssessmentScoringService;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/assessment/records")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AssessmentRecordController {
  private final AssessmentRecordMapper recordMapper;
  private final ElderMapper elderMapper;
  private final OrgMapper orgMapper;
  private final AssessmentScoringService scoringService;

  public AssessmentRecordController(AssessmentRecordMapper recordMapper, ElderMapper elderMapper, OrgMapper orgMapper,
      AssessmentScoringService scoringService) {
    this.recordMapper = recordMapper;
    this.elderMapper = elderMapper;
    this.orgMapper = orgMapper;
    this.scoringService = scoringService;
  }

  @GetMapping("/page")
  public Result<IPage<AssessmentRecordResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String assessmentType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = buildPageQuery(orgId, assessmentType, status, elderId, templateId, keyword, dateFrom, dateTo);
    wrapper.orderByDesc(AssessmentRecord::getAssessmentDate).orderByDesc(AssessmentRecord::getUpdateTime);
    IPage<AssessmentRecord> page = recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(convertPage(page));
  }

  @GetMapping("/{id}")
  public Result<AssessmentRecordResponse> get(@PathVariable Long id) {
    AssessmentRecord record = recordMapper.selectById(id);
    if (record == null || record.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(record.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该评估记录");
    }
    return Result.ok(toResponse(record, selectElder(record.getElderId()), selectOrg(record.getOrgId())));
  }

  @PostMapping
  public Result<AssessmentRecordResponse> create(@Valid @RequestBody AssessmentRecordRequest request) {
    validateRequest(request);
    Long orgId = AuthContext.getOrgId();
    Long elderId = resolveElderId(orgId, request.getElderId(), request.getElderName());

    AssessmentRecord record = new AssessmentRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    patchFromRequest(record, request);
    applyAutoScore(orgId, record);

    if (record.getStatus() == null || record.getStatus().isBlank()) {
      record.setStatus("COMPLETED");
    }
    if (record.getScoreAuto() == null) {
      record.setScoreAuto(1);
    }
    if (record.getAssessorId() == null) {
      record.setAssessorId(AuthContext.getStaffId());
    }
    if (record.getAssessorName() == null || record.getAssessorName().isBlank()) {
      record.setAssessorName(AuthContext.getUsername());
    }
    if (record.getSource() == null || record.getSource().isBlank()) {
      record.setSource("MANUAL");
    }
    record.setCreatedBy(AuthContext.getStaffId());
    record.setIsDeleted(0);
    recordMapper.insert(record);
    return Result.ok(toResponse(record, selectElder(record.getElderId()), selectOrg(record.getOrgId())));
  }

  @PutMapping("/{id}")
  public Result<AssessmentRecordResponse> update(@PathVariable Long id, @Valid @RequestBody AssessmentRecordRequest request) {
    validateRequest(request);
    AssessmentRecord record = recordMapper.selectById(id);
    if (record == null || record.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(record.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该评估记录");
    }

    Long elderId = resolveElderId(orgId, request.getElderId(), request.getElderName());
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    patchFromRequest(record, request);
    applyAutoScore(orgId, record);

    if (record.getAssessorId() == null) {
      record.setAssessorId(AuthContext.getStaffId());
    }
    if (record.getScoreAuto() == null) {
      record.setScoreAuto(1);
    }
    if (record.getAssessorName() == null || record.getAssessorName().isBlank()) {
      record.setAssessorName(AuthContext.getUsername());
    }
    if (record.getSource() == null || record.getSource().isBlank()) {
      record.setSource("MANUAL");
    }

    recordMapper.updateById(record);
    return Result.ok(toResponse(record, selectElder(record.getElderId()), selectOrg(record.getOrgId())));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    AssessmentRecord record = recordMapper.selectById(id);
    if (record != null && record.getIsDeleted() != 1) {
      Long orgId = AuthContext.getOrgId();
      if (orgId != null && !orgId.equals(record.getOrgId())) {
        throw new IllegalArgumentException("无权限访问该评估记录");
      }
      record.setIsDeleted(1);
      recordMapper.updateById(record);
    }
    return Result.ok(null);
  }

  @PostMapping("/batch-delete")
  public Result<Integer> batchDelete(@RequestBody List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    int deleted = 0;
    for (Long id : ids) {
      if (id == null) {
        continue;
      }
      AssessmentRecord record = recordMapper.selectById(id);
      if (record == null || record.getIsDeleted() == 1) {
        continue;
      }
      if (orgId != null && !orgId.equals(record.getOrgId())) {
        throw new IllegalArgumentException("无权限访问该评估记录");
      }
      record.setIsDeleted(1);
      recordMapper.updateById(record);
      deleted++;
    }
    return Result.ok(deleted);
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String assessmentType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long templateId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = buildPageQuery(orgId, assessmentType, status, elderId, templateId, keyword, dateFrom, dateTo);
    wrapper.orderByDesc(AssessmentRecord::getAssessmentDate).orderByDesc(AssessmentRecord::getUpdateTime);
    List<AssessmentRecord> records = recordMapper.selectList(wrapper);
    List<AssessmentRecordResponse> responses = convertList(records);

    List<String> headers = List.of("评估编码", "老人姓名", "性别", "年龄", "手机号", "家庭地址", "评估类型", "评估日期", "评估状态", "评估结果", "评估分值", "评估人", "所属机构");
    List<List<String>> body = new ArrayList<>();
    for (AssessmentRecordResponse item : responses) {
      body.add(List.of(
          csvValue(item.getArchiveNo() == null ? String.valueOf(item.getId()) : item.getArchiveNo()),
          csvValue(item.getElderName()),
          csvValue(item.getGenderLabel()),
          csvValue(item.getAge() == null ? null : String.valueOf(item.getAge())),
          csvValue(item.getPhone()),
          csvValue(item.getAddress()),
          csvValue(item.getAssessmentTypeLabel()),
          csvValue(item.getAssessmentDate() == null ? null : item.getAssessmentDate().toString()),
          csvValue(item.getStatusLabel()),
          csvValue(item.getResultSummary()),
          csvValue(item.getScore() == null ? null : item.getScore().toPlainString()),
          csvValue(item.getAssessorName()),
          csvValue(item.getOrgName())));
    }
    StringBuilder csv = new StringBuilder("\uFEFF");
    csv.append(String.join(",", headers)).append('\n');
    for (List<String> row : body) {
      csv.append(String.join(",", row)).append('\n');
    }

    String filename = "assessment-records-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(csv.toString().getBytes(StandardCharsets.UTF_8));
  }

  private void patchFromRequest(AssessmentRecord record, AssessmentRecordRequest request) {
    record.setAssessmentType(request.getAssessmentType());
    record.setTemplateId(request.getTemplateId());
    record.setLevelCode(request.getLevelCode());
    record.setScore(request.getScore());
    record.setAssessmentDate(request.getAssessmentDate());
    record.setNextAssessmentDate(request.getNextAssessmentDate());
    record.setAssessorId(request.getAssessorId());
    record.setAssessorName(request.getAssessorName());
    record.setStatus(request.getStatus());
    record.setResultSummary(request.getResultSummary());
    record.setSuggestion(request.getSuggestion());
    record.setDetailJson(request.getDetailJson());
    record.setScoreAuto(request.getScoreAuto());
    record.setArchiveNo(request.getArchiveNo());
    record.setSource(request.getSource());
  }

  private void validateRequest(AssessmentRecordRequest request) {
    if (request.getAssessmentDate() != null && request.getNextAssessmentDate() != null
        && request.getNextAssessmentDate().isBefore(request.getAssessmentDate())) {
      throw new IllegalArgumentException("下次评估日期不能早于评估日期");
    }
    boolean autoScore = request.getScoreAuto() == null || request.getScoreAuto() == 1;
    if (autoScore) {
      if (request.getTemplateId() == null) {
        throw new IllegalArgumentException("自动评分开启时必须选择量表模板");
      }
      if (request.getDetailJson() == null || request.getDetailJson().isBlank()) {
        throw new IllegalArgumentException("自动评分开启时必须填写量表明细JSON");
      }
    }
  }

  private void applyAutoScore(Long orgId, AssessmentRecord record) {
    if (record.getTemplateId() == null) {
      return;
    }
    AssessmentScaleTemplate template = scoringService.getActiveTemplate(orgId, record.getTemplateId());
    if (template == null) {
      throw new IllegalArgumentException("Template not found");
    }
    if (!record.getAssessmentType().equals(template.getAssessmentType())) {
      throw new IllegalArgumentException("Template type mismatch");
    }
    if (record.getScoreAuto() != null && record.getScoreAuto() == 0) {
      return;
    }
    if (record.getDetailJson() == null || record.getDetailJson().isBlank()) {
      return;
    }
    AssessmentScoreResult result = scoringService.score(template, record.getDetailJson());
    record.setScore(result.getScore());
    if (record.getLevelCode() == null || record.getLevelCode().isBlank()) {
      record.setLevelCode(result.getLevelCode());
    }
    record.setScoreAuto(1);
  }

  private Long resolveElderId(Long orgId, Long elderId, String elderName) {
    if (elderId != null) {
      return elderId;
    }
    if (elderName == null || elderName.isBlank()) {
      throw new IllegalArgumentException("elderName required");
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("elder not found");
    }
    return elder.getId();
  }

  private String resolveElderName(Long elderId, String fallback) {
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }

  private IPage<AssessmentRecordResponse> convertPage(IPage<AssessmentRecord> page) {
    if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
      return page == null ? new Page<AssessmentRecordResponse>() : page.convert(record -> toResponse(record, null, null));
    }
    Map<Long, ElderProfile> elderMap = batchSelectElder(page.getRecords().stream()
        .map(AssessmentRecord::getElderId)
        .filter(id -> id != null)
        .toList());
    Map<Long, Org> orgMap = batchSelectOrg(page.getRecords().stream()
        .map(AssessmentRecord::getOrgId)
        .filter(id -> id != null)
        .toList());
    return page.convert(record -> toResponse(record, elderMap.get(record.getElderId()), orgMap.get(record.getOrgId())));
  }

  private List<AssessmentRecordResponse> convertList(List<AssessmentRecord> records) {
    if (records == null || records.isEmpty()) {
      return List.of();
    }
    Map<Long, ElderProfile> elderMap = batchSelectElder(records.stream()
        .map(AssessmentRecord::getElderId)
        .filter(id -> id != null)
        .toList());
    Map<Long, Org> orgMap = batchSelectOrg(records.stream()
        .map(AssessmentRecord::getOrgId)
        .filter(id -> id != null)
        .toList());
    return records.stream()
        .map(record -> toResponse(record, elderMap.get(record.getElderId()), orgMap.get(record.getOrgId())))
        .toList();
  }

  private Map<Long, ElderProfile> batchSelectElder(Collection<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return elderMapper.selectBatchIds(elderIds).stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (left, right) -> left));
  }

  private Map<Long, Org> batchSelectOrg(Collection<Long> orgIds) {
    if (orgIds == null || orgIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return orgMapper.selectBatchIds(orgIds).stream()
        .collect(Collectors.toMap(Org::getId, Function.identity(), (left, right) -> left));
  }

  private ElderProfile selectElder(Long elderId) {
    if (elderId == null) {
      return null;
    }
    return elderMapper.selectById(elderId);
  }

  private Org selectOrg(Long orgId) {
    if (orgId == null) {
      return null;
    }
    return orgMapper.selectById(orgId);
  }

  private AssessmentRecordResponse toResponse(AssessmentRecord record, ElderProfile elder, Org org) {
    if (record == null) {
      return null;
    }
    AssessmentRecordResponse response = new AssessmentRecordResponse();
    response.setId(record.getId());
    response.setTenantId(record.getTenantId());
    response.setOrgId(record.getOrgId());
    response.setOrgName(org == null ? null : org.getOrgName());
    response.setElderId(record.getElderId());
    response.setElderName(record.getElderName());
    response.setGender(elder == null ? null : elder.getGender());
    response.setGenderLabel(resolveGenderLabel(elder == null ? null : elder.getGender()));
    response.setAge(resolveAge(elder == null ? null : elder.getBirthDate()));
    response.setPhone(elder == null ? null : elder.getPhone());
    response.setAddress(elder == null ? null : elder.getHomeAddress());
    response.setAssessmentType(record.getAssessmentType());
    response.setAssessmentTypeLabel(resolveAssessmentTypeLabel(record.getAssessmentType()));
    response.setTemplateId(record.getTemplateId());
    response.setLevelCode(record.getLevelCode());
    response.setScore(record.getScore());
    response.setAssessmentDate(record.getAssessmentDate());
    response.setNextAssessmentDate(record.getNextAssessmentDate());
    response.setAssessorId(record.getAssessorId());
    response.setAssessorName(record.getAssessorName());
    response.setStatus(record.getStatus());
    response.setStatusLabel(resolveStatusLabel(record.getStatus()));
    response.setResultSummary(record.getResultSummary());
    response.setSuggestion(record.getSuggestion());
    response.setDetailJson(record.getDetailJson());
    response.setScoreAuto(record.getScoreAuto());
    response.setArchiveNo(record.getArchiveNo());
    response.setSource(record.getSource());
    response.setCreatedBy(record.getCreatedBy());
    response.setCreateTime(record.getCreateTime());
    response.setUpdateTime(record.getUpdateTime());
    return response;
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssessmentRecord> buildPageQuery(
      Long orgId,
      String assessmentType,
      String status,
      Long elderId,
      Long templateId,
      String keyword,
      String dateFrom,
      String dateTo) {
    var wrapper = Wrappers.lambdaQuery(AssessmentRecord.class)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(orgId != null, AssessmentRecord::getOrgId, orgId)
        .eq(elderId != null, AssessmentRecord::getElderId, elderId)
        .eq(templateId != null, AssessmentRecord::getTemplateId, templateId)
        .eq(assessmentType != null && !assessmentType.isBlank(), AssessmentRecord::getAssessmentType, assessmentType)
        .eq(status != null && !status.isBlank(), AssessmentRecord::getStatus, status);

    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(AssessmentRecord::getElderName, keyword)
          .or().like(AssessmentRecord::getAssessorName, keyword)
          .or().like(AssessmentRecord::getArchiveNo, keyword)
          .or().like(AssessmentRecord::getResultSummary, keyword));
    }
    if (dateFrom != null && !dateFrom.isBlank()) {
      wrapper.ge(AssessmentRecord::getAssessmentDate, LocalDate.parse(dateFrom));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      wrapper.le(AssessmentRecord::getAssessmentDate, LocalDate.parse(dateTo));
    }
    return wrapper;
  }

  private Integer resolveAge(LocalDate birthDate) {
    if (birthDate == null) {
      return null;
    }
    int years = Period.between(birthDate, LocalDate.now()).getYears();
    return Math.max(years, 0);
  }

  private String resolveGenderLabel(Integer gender) {
    if (gender == null) {
      return "未知";
    }
    return switch (gender) {
      case 1 -> "男";
      case 2 -> "女";
      default -> "未知";
    };
  }

  private String resolveAssessmentTypeLabel(String assessmentType) {
    if (assessmentType == null || assessmentType.isBlank()) {
      return null;
    }
    return switch (assessmentType) {
      case "ADMISSION" -> "入住评估";
      case "REGISTRATION" -> "评估登记";
      case "CONTINUOUS" -> "持续评估";
      case "ARCHIVE" -> "评估档案";
      case "OTHER_SCALE" -> "其他量表评估";
      case "COGNITIVE" -> "认知能力评估";
      case "SELF_CARE" -> "自理能力评估";
      default -> assessmentType;
    };
  }

  private String resolveStatusLabel(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    return switch (status) {
      case "DRAFT" -> "草稿";
      case "ARCHIVED" -> "已归档";
      default -> "已完成";
    };
  }

  private String csvValue(String value) {
    String normalized = value == null ? "" : value;
    return "\"" + normalized.replace("\"", "\"\"") + "\"";
  }
}
