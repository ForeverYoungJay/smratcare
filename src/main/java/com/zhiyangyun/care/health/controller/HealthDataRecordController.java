package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.model.HealthDataSummaryResponse;
import com.zhiyangyun.care.health.model.HealthDataTypeStatItem;
import com.zhiyangyun.care.health.model.HealthDataRecordRequest;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.List;
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
@RequestMapping("/api/health/data")
public class HealthDataRecordController {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final HealthDataRecordMapper mapper;
  private final ElderResolveSupport elderResolveSupport;

  public HealthDataRecordController(HealthDataRecordMapper mapper, ElderResolveSupport elderResolveSupport) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
  }

  @GetMapping("/page")
  public Result<IPage<HealthDataRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String dataType,
      @RequestParam(required = false) Integer abnormalFlag,
      @RequestParam(required = false) String measuredFrom,
      @RequestParam(required = false) String measuredTo,
      @RequestParam(required = false) String keyword) {
    var wrapper = buildQuery(
        AuthContext.getOrgId(),
        elderId,
        dataType,
        abnormalFlag,
        measuredFrom,
        measuredTo,
        keyword);
    wrapper.orderByDesc(HealthDataRecord::getMeasuredAt);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<HealthDataSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String dataType,
      @RequestParam(required = false) Integer abnormalFlag,
      @RequestParam(required = false) String measuredFrom,
      @RequestParam(required = false) String measuredTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime from = parseDateTime(measuredFrom);
    LocalDateTime to = parseDateTime(measuredTo);
    var wrapper = buildQuery(orgId, elderId, dataType, abnormalFlag, measuredFrom, measuredTo, keyword);
    long totalCount = mapper.selectCount(wrapper);

    long abnormalCount = 0L;
    if (totalCount > 0) {
      abnormalCount = mapper.selectCount(buildQuery(orgId, elderId, dataType, 1, measuredFrom, measuredTo, keyword));
    }
    long normalCount = Math.max(totalCount - abnormalCount, 0L);

    var latestWrapper = buildQuery(orgId, elderId, dataType, abnormalFlag, measuredFrom, measuredTo, keyword)
        .orderByDesc(HealthDataRecord::getMeasuredAt)
        .last("limit 1");
    HealthDataRecord latestRecord = mapper.selectOne(latestWrapper);

    var typeWrapper = Wrappers.query(HealthDataRecord.class)
        .select("data_type AS dataType", "COUNT(1) AS totalCount", "SUM(CASE WHEN abnormal_flag = 1 THEN 1 ELSE 0 END) AS abnormalCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(elderId != null, "elder_id", elderId)
        .eq(dataType != null && !dataType.isBlank(), "data_type", dataType)
        .eq(abnormalFlag != null && (abnormalFlag == 0 || abnormalFlag == 1), "abnormal_flag", abnormalFlag)
        .ge(from != null, "measured_at", from)
        .le(to != null, "measured_at", to)
        .groupBy("data_type")
        .orderByDesc("totalCount");
    if (keyword != null && !keyword.isBlank()) {
      typeWrapper.and(w -> w.like("elder_name", keyword).or().like("data_value", keyword));
    }
    List<HealthDataTypeStatItem> typeStats = mapper.selectMaps(typeWrapper).stream().map(map -> {
      HealthDataTypeStatItem item = new HealthDataTypeStatItem();
      item.setDataType(readString(map, "dataType", "data_type", "DATA_TYPE"));
      item.setTotalCount(toLong(map.get("totalCount")));
      item.setAbnormalCount(toLong(map.get("abnormalCount")));
      long itemTotal = item.getTotalCount() == null ? 0L : item.getTotalCount();
      long itemAbnormal = item.getAbnormalCount() == null ? 0L : item.getAbnormalCount();
      item.setAbnormalRate(itemTotal == 0 ? 0D : (double) itemAbnormal * 100D / itemTotal);
      return item;
    }).toList();

    HealthDataSummaryResponse response = new HealthDataSummaryResponse();
    response.setTotalCount(totalCount);
    response.setAbnormalCount(abnormalCount);
    response.setNormalCount(normalCount);
    response.setAbnormalRate(totalCount == 0 ? 0D : (double) abnormalCount * 100D / totalCount);
    response.setLatestMeasuredAt(latestRecord == null ? null : latestRecord.getMeasuredAt());
    response.setTypeStats(typeStats);
    return Result.ok(response);
  }

  @PostMapping
  public Result<HealthDataRecord> create(@Valid @RequestBody HealthDataRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthDataRecord item = new HealthDataRecord();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDataType(request.getDataType());
    item.setDataValue(request.getDataValue());
    item.setMeasuredAt(request.getMeasuredAt());
    item.setSource(request.getSource());
    item.setAbnormalFlag(request.getAbnormalFlag() == null ? 0 : request.getAbnormalFlag());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthDataRecord> update(@PathVariable Long id, @Valid @RequestBody HealthDataRecordRequest request) {
    HealthDataRecord item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDataType(request.getDataType());
    item.setDataValue(request.getDataValue());
    item.setMeasuredAt(request.getMeasuredAt());
    item.setSource(request.getSource());
    item.setAbnormalFlag(request.getAbnormalFlag() == null ? 0 : request.getAbnormalFlag());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthDataRecord item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HealthDataRecord> buildQuery(
      Long orgId,
      Long elderId,
      String dataType,
      Integer abnormalFlag,
      String measuredFrom,
      String measuredTo,
      String keyword) {
    LocalDateTime from = parseDateTime(measuredFrom);
    LocalDateTime to = parseDateTime(measuredTo);
    var wrapper = Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(elderId != null, HealthDataRecord::getElderId, elderId)
        .eq(dataType != null && !dataType.isBlank(), HealthDataRecord::getDataType, dataType)
        .eq(abnormalFlag != null && (abnormalFlag == 0 || abnormalFlag == 1), HealthDataRecord::getAbnormalFlag, abnormalFlag)
        .ge(from != null, HealthDataRecord::getMeasuredAt, from)
        .le(to != null, HealthDataRecord::getMeasuredAt, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthDataRecord::getElderName, keyword)
          .or().like(HealthDataRecord::getDataValue, keyword));
    }
    return wrapper;
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      } catch (DateTimeParseException e) {
        return null;
      }
    }
  }

  private Long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return 0L;
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private String readString(Map<String, Object> source, String... keys) {
    for (String key : keys) {
      Object value = source.get(key);
      if (value != null) {
        return String.valueOf(value);
      }
    }
    return "";
  }
}
