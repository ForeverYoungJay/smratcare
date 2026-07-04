package com.zhiyangyun.care.ltci.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.ltci.entity.LtciAssessApply;
import com.zhiyangyun.care.ltci.entity.LtciAssessTemplate;
import com.zhiyangyun.care.ltci.entity.LtciAssessment;
import com.zhiyangyun.care.ltci.mapper.LtciAssessApplyMapper;
import com.zhiyangyun.care.ltci.mapper.LtciAssessTemplateMapper;
import com.zhiyangyun.care.ltci.mapper.LtciAssessmentMapper;
import com.zhiyangyun.care.ltci.model.LtciAcceptRequest;
import com.zhiyangyun.care.ltci.model.LtciApplyRequest;
import com.zhiyangyun.care.ltci.model.LtciAssessStatus;
import com.zhiyangyun.care.ltci.model.LtciDisputeRequest;
import com.zhiyangyun.care.ltci.model.LtciGradingResult;
import com.zhiyangyun.care.ltci.model.LtciScoreRequest;
import com.zhiyangyun.care.ltci.service.LtciAssessmentService;
import com.zhiyangyun.care.ltci.service.LtciGradingService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LtciAssessmentServiceImpl implements LtciAssessmentService {

  /** 默认评估结果有效期（年），到期需复评。 */
  private static final int DEFAULT_VALID_YEARS = 2;
  private static final String DEFAULT_STANDARD = "NATIONAL_2021";

  private final LtciAssessTemplateMapper templateMapper;
  private final LtciAssessApplyMapper applyMapper;
  private final LtciAssessmentMapper assessmentMapper;
  private final LtciGradingService gradingService;
  private final ObjectMapper objectMapper;

  public LtciAssessmentServiceImpl(LtciAssessTemplateMapper templateMapper,
      LtciAssessApplyMapper applyMapper, LtciAssessmentMapper assessmentMapper,
      LtciGradingService gradingService, ObjectMapper objectMapper) {
    this.templateMapper = templateMapper;
    this.applyMapper = applyMapper;
    this.assessmentMapper = assessmentMapper;
    this.gradingService = gradingService;
    this.objectMapper = objectMapper;
  }

  @Override
  public Long apply(LtciApplyRequest request) {
    Long orgId = AuthContext.getOrgId();
    LtciAssessApply apply = new LtciAssessApply();
    apply.setOrgId(orgId);
    apply.setElderId(request.getElderId());
    apply.setApplyType(StringUtils.hasText(request.getApplyType())
        ? request.getApplyType() : LtciAssessStatus.TYPE_FIRST);
    apply.setApplySource(request.getApplySource());
    apply.setApplicantName(request.getApplicantName());
    apply.setApplicantPhone(request.getApplicantPhone());
    apply.setAssessStatus(LtciAssessStatus.APPLIED);
    apply.setRemark(request.getRemark());
    apply.setCreatedBy(AuthContext.getStaffId());
    applyMapper.insert(apply);
    return apply.getId();
  }

  @Override
  public void accept(LtciAcceptRequest request) {
    LtciAssessApply apply = loadApply(request.getApplyId());
    if (!LtciAssessStatus.APPLIED.equals(apply.getAssessStatus())) {
      throw new IllegalStateException("仅待受理(APPLIED)状态的申请可受理，当前=" + apply.getAssessStatus());
    }
    apply.setAssessStatus(request.getAssessorId() != null
        ? LtciAssessStatus.ASSIGNED : LtciAssessStatus.ACCEPTED);
    apply.setAssessorId(request.getAssessorId());
    apply.setAcceptedBy(AuthContext.getStaffId());
    apply.setAcceptedAt(LocalDateTime.now());
    if (StringUtils.hasText(request.getRemark())) {
      apply.setRemark(request.getRemark());
    }
    applyMapper.updateById(apply);
  }

  @Override
  @Transactional
  public LtciAssessment score(LtciScoreRequest request) {
    Long orgId = AuthContext.getOrgId();
    LtciAssessApply apply = loadApply(request.getApplyId());
    LtciAssessTemplate template = resolveTemplate(request.getTemplateId(), orgId);

    LtciGradingResult graded = gradingService.judge(
        template.getIndicatorsJson(), template.getCombineRuleJson(), request.getAnswers());

    LocalDate today = LocalDate.now();
    LtciAssessment assessment = new LtciAssessment();
    assessment.setOrgId(orgId);
    assessment.setApplyId(apply.getId());
    assessment.setElderId(request.getElderId());
    assessment.setTemplateId(template.getId());
    assessment.setAssessorId(request.getAssessorId() != null
        ? request.getAssessorId() : apply.getAssessorId());
    assessment.setAssessDate(today);
    assessment.setAdlScore(graded.getAdlScore());
    assessment.setCognitiveScore(graded.getCognitiveScore());
    assessment.setPerceptionScore(graded.getPerceptionScore());
    assessment.setTotalScore(graded.getTotalScore());
    assessment.setDisabilityLevel(graded.getDisabilityLevel());
    assessment.setLevelLabel(graded.getLevelLabel());
    assessment.setEscalated(graded.isEscalated() ? 1 : 0);
    assessment.setAnswersJson(writeJson(request.getAnswers()));
    assessment.setEffectiveStart(today);
    assessment.setEffectiveEnd(today.plusYears(DEFAULT_VALID_YEARS));
    assessment.setAssessStatus(LtciAssessStatus.JUDGED);
    assessment.setRemark(request.getRemark());
    assessment.setCreatedBy(AuthContext.getStaffId());
    assessmentMapper.insert(assessment);

    apply.setAssessStatus(LtciAssessStatus.JUDGED);
    applyMapper.updateById(apply);
    return assessment;
  }

  @Override
  @Transactional
  public void notifyResult(Long assessmentId) {
    LtciAssessment assessment = loadAssessment(assessmentId);
    if (!LtciAssessStatus.JUDGED.equals(assessment.getAssessStatus())) {
      throw new IllegalStateException("仅已判级(JUDGED)记录可告知，当前=" + assessment.getAssessStatus());
    }
    // 同一长者的旧生效记录置为已到期，保证唯一生效。
    LtciAssessment prev = currentEffective(assessment.getElderId());
    if (prev != null && !prev.getId().equals(assessment.getId())) {
      prev.setAssessStatus(LtciAssessStatus.EXPIRED);
      prev.setEffectiveEnd(LocalDate.now());
      assessmentMapper.updateById(prev);
    }
    assessment.setAssessStatus(LtciAssessStatus.EFFECTIVE);
    assessmentMapper.updateById(assessment);

    if (assessment.getApplyId() != null) {
      LtciAssessApply apply = applyMapper.selectById(assessment.getApplyId());
      if (apply != null) {
        apply.setAssessStatus(LtciAssessStatus.NOTIFIED);
        applyMapper.updateById(apply);
      }
    }
  }

  @Override
  @Transactional
  public Long dispute(LtciDisputeRequest request) {
    LtciAssessment assessment = loadAssessment(request.getAssessmentId());
    assessment.setAssessStatus(LtciAssessStatus.DISPUTED);
    assessmentMapper.updateById(assessment);

    LtciAssessApply apply = new LtciAssessApply();
    apply.setOrgId(assessment.getOrgId());
    apply.setElderId(assessment.getElderId());
    apply.setApplyType(LtciAssessStatus.TYPE_DISPUTE);
    apply.setApplySource("DISPUTE");
    apply.setApplicantName(request.getApplicantName());
    apply.setApplicantPhone(request.getApplicantPhone());
    apply.setAssessStatus(LtciAssessStatus.APPLIED);
    apply.setRemark(request.getReason());
    apply.setCreatedBy(AuthContext.getStaffId());
    applyMapper.insert(apply);
    return apply.getId();
  }

  @Override
  public IPage<LtciAssessment> pageAssessments(long pageNo, long pageSize, Long elderId,
      String status, Integer disabilityLevel) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(LtciAssessment.class)
        .eq(LtciAssessment::getIsDeleted, 0)
        .eq(orgId != null, LtciAssessment::getOrgId, orgId)
        .eq(elderId != null, LtciAssessment::getElderId, elderId)
        .eq(StringUtils.hasText(status), LtciAssessment::getAssessStatus, status)
        .eq(disabilityLevel != null, LtciAssessment::getDisabilityLevel, disabilityLevel)
        .orderByDesc(LtciAssessment::getAssessDate)
        .orderByDesc(LtciAssessment::getId);
    return assessmentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public LtciAssessment currentEffective(Long elderId) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    var wrapper = Wrappers.lambdaQuery(LtciAssessment.class)
        .eq(LtciAssessment::getIsDeleted, 0)
        .eq(orgId != null, LtciAssessment::getOrgId, orgId)
        .eq(LtciAssessment::getElderId, elderId)
        .eq(LtciAssessment::getAssessStatus, LtciAssessStatus.EFFECTIVE)
        .le(LtciAssessment::getEffectiveStart, today)
        .and(w -> w.isNull(LtciAssessment::getEffectiveEnd)
            .or().ge(LtciAssessment::getEffectiveEnd, today))
        .orderByDesc(LtciAssessment::getEffectiveStart)
        .orderByDesc(LtciAssessment::getId)
        .last("limit 1");
    return assessmentMapper.selectOne(wrapper);
  }

  /** 解析模板：优先指定 id；否则取机构定制的启用模板；再退回全局国家标准模板。 */
  private LtciAssessTemplate resolveTemplate(Long templateId, Long orgId) {
    if (templateId != null) {
      LtciAssessTemplate t = templateMapper.selectById(templateId);
      if (t == null || Integer.valueOf(1).equals(t.getIsDeleted())) {
        throw new IllegalArgumentException("评估模板不存在: " + templateId);
      }
      return t;
    }
    if (orgId != null) {
      LtciAssessTemplate orgTpl = templateMapper.selectOne(Wrappers.lambdaQuery(LtciAssessTemplate.class)
          .eq(LtciAssessTemplate::getIsDeleted, 0)
          .eq(LtciAssessTemplate::getOrgId, orgId)
          .eq(LtciAssessTemplate::getStandardVersion, DEFAULT_STANDARD)
          .eq(LtciAssessTemplate::getStatus, 1)
          .orderByDesc(LtciAssessTemplate::getUpdateTime)
          .last("limit 1"));
      if (orgTpl != null) {
        return orgTpl;
      }
    }
    LtciAssessTemplate global = templateMapper.selectOne(Wrappers.lambdaQuery(LtciAssessTemplate.class)
        .eq(LtciAssessTemplate::getIsDeleted, 0)
        .isNull(LtciAssessTemplate::getOrgId)
        .eq(LtciAssessTemplate::getStandardVersion, DEFAULT_STANDARD)
        .eq(LtciAssessTemplate::getStatus, 1)
        .last("limit 1"));
    if (global == null) {
      throw new IllegalStateException("未找到可用的失能评估模板，请检查 V215 内置模板");
    }
    return global;
  }

  private LtciAssessApply loadApply(Long applyId) {
    LtciAssessApply apply = applyMapper.selectById(applyId);
    if (apply == null || Integer.valueOf(1).equals(apply.getIsDeleted())) {
      throw new IllegalArgumentException("评估申请不存在: " + applyId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(apply.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的评估申请");
    }
    return apply;
  }

  private LtciAssessment loadAssessment(Long assessmentId) {
    LtciAssessment assessment = assessmentMapper.selectById(assessmentId);
    if (assessment == null || Integer.valueOf(1).equals(assessment.getIsDeleted())) {
      throw new IllegalArgumentException("评估记录不存在: " + assessmentId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(assessment.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的评估记录");
    }
    return assessment;
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException("作答序列化失败: " + ex.getMessage(), ex);
    }
  }
}
