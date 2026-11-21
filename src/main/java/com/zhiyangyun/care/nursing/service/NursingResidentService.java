package com.zhiyangyun.care.nursing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.nursing.model.NursingResidentItem;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 护理档案列表：在住长者分页，附最新正式评估；可只看复评日期已到的长者。 */
@Service
public class NursingResidentService {
  private static final String IN_HOSPITAL = "IN_HOSPITAL";

  private final ElderService elderService;
  private final AssessmentRecordMapper assessmentMapper;

  public NursingResidentService(ElderService elderService, AssessmentRecordMapper assessmentMapper) {
    this.elderService = elderService;
    this.assessmentMapper = assessmentMapper;
  }

  public IPage<NursingResidentItem> page(Long tenantId, long pageNo, long pageSize, String keyword, boolean reassessDue,
      LocalDate today) {
    Set<Long> scope = reassessDue ? reassessDueElderIds(tenantId, today) : null;
    IPage<ElderResponse> elders = elderService.pageWithin(tenantId, pageNo, pageSize, keyword, IN_HOSPITAL, scope);
    Map<Long, AssessmentRecord> latest = latestByElder(tenantId,
        elders.getRecords().stream().map(ElderResponse::getId).toList());
    return elders.convert(elder -> {
      NursingResidentItem item = new NursingResidentItem();
      item.setElder(elder);
      AssessmentRecord record = latest.get(elder.getId());
      if (record != null) {
        item.setLatestLevelCode(record.getLevelCode());
        item.setLatestAssessmentDate(record.getAssessmentDate());
        item.setNextAssessmentDate(record.getNextAssessmentDate());
      }
      return item;
    });
  }

  /** 最新一次正式评估的下次复评日已到（含今天）的长者 */
  Set<Long> reassessDueElderIds(Long tenantId, LocalDate today) {
    return latestByElder(tenantId, null).entrySet().stream()
        .filter(e -> e.getValue().getNextAssessmentDate() != null && !e.getValue().getNextAssessmentDate().isAfter(today))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  /** 每位长者评估日期最新的一条非草稿记录；elderIds 为 null 时取全机构 */
  private Map<Long, AssessmentRecord> latestByElder(Long tenantId, Collection<Long> elderIds) {
    if (elderIds != null && elderIds.isEmpty()) {
      return Map.of();
    }
    var rows = assessmentMapper.selectList(Wrappers.lambdaQuery(AssessmentRecord.class)
        .select(AssessmentRecord::getElderId, AssessmentRecord::getLevelCode, AssessmentRecord::getAssessmentDate,
            AssessmentRecord::getNextAssessmentDate)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(tenantId != null, AssessmentRecord::getTenantId, tenantId)
        .ne(AssessmentRecord::getStatus, "DRAFT")
        .isNotNull(AssessmentRecord::getElderId)
        .in(elderIds != null, AssessmentRecord::getElderId, elderIds)
        .orderByDesc(AssessmentRecord::getAssessmentDate)
        .orderByDesc(AssessmentRecord::getId));
    Map<Long, AssessmentRecord> latest = new HashMap<>();
    rows.forEach(row -> latest.putIfAbsent(row.getElderId(), row));
    return latest;
  }
}
