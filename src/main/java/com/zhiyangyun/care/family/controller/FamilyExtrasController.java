package com.zhiyangyun.care.family.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.survey.entity.SurveyQuestion;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.entity.SurveySubmissionItem;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.entity.SurveyTemplateQuestion;
import com.zhiyangyun.care.survey.mapper.SurveyQuestionMapper;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionItemMapper;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateMapper;
import com.zhiyangyun.care.survey.mapper.SurveyTemplateQuestionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 家属端扩展能力：满意度问卷（列表/提交）与账单费用明细。
 * 只做薄查询/写入，复用问卷与账单模块既有表结构，无新增 DDL。
 */
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyExtrasController {

  private final SurveyTemplateMapper surveyTemplateMapper;
  private final SurveyTemplateQuestionMapper surveyTemplateQuestionMapper;
  private final SurveyQuestionMapper surveyQuestionMapper;
  private final SurveySubmissionMapper surveySubmissionMapper;
  private final SurveySubmissionItemMapper surveySubmissionItemMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final ElderFamilyMapper elderFamilyMapper;

  // ---------- 问卷 ----------

  @GetMapping("/surveys")
  public Result<List<Map<String, Object>>> listSurveys() {
    Long orgId = AuthContext.getOrgId();
    Long familyUserId = AuthContext.getStaffId();
    LocalDate today = LocalDate.now();
    List<SurveyTemplate> templates = surveyTemplateMapper.selectList(
        Wrappers.lambdaQuery(SurveyTemplate.class)
            .eq(SurveyTemplate::getIsDeleted, 0)
            .eq(SurveyTemplate::getOrgId, orgId)
            .eq(SurveyTemplate::getStatus, 1)
            .orderByDesc(SurveyTemplate::getUpdateTime));

    List<Long> submittedTemplateIds = surveySubmissionMapper.selectList(
            Wrappers.lambdaQuery(SurveySubmission.class)
                .eq(SurveySubmission::getIsDeleted, 0)
                .eq(SurveySubmission::getOrgId, orgId)
                .eq(SurveySubmission::getSubmitterId, familyUserId)
                .eq(SurveySubmission::getSubmitterRole, "FAMILY"))
        .stream().map(SurveySubmission::getTemplateId).toList();

    List<Map<String, Object>> result = new ArrayList<>();
    for (SurveyTemplate template : templates) {
      if (template.getStartDate() != null && today.isBefore(template.getStartDate())) {
        continue;
      }
      if (template.getEndDate() != null && today.isAfter(template.getEndDate())) {
        continue;
      }
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", String.valueOf(template.getId()));
      item.put("name", template.getTemplateName());
      item.put("description", template.getDescription() == null ? "" : template.getDescription());
      item.put("anonymous", Objects.equals(template.getAnonymousFlag(), 1));
      item.put("submitted", submittedTemplateIds.contains(template.getId()));
      item.put("questions", loadQuestions(orgId, template.getId()));
      result.add(item);
    }
    return Result.ok(result);
  }

  private List<Map<String, Object>> loadQuestions(Long orgId, Long templateId) {
    List<SurveyTemplateQuestion> links = surveyTemplateQuestionMapper.selectList(
        Wrappers.lambdaQuery(SurveyTemplateQuestion.class)
            .eq(SurveyTemplateQuestion::getOrgId, orgId)
            .eq(SurveyTemplateQuestion::getTemplateId, templateId)
            .orderByAsc(SurveyTemplateQuestion::getSortNo));
    if (links.isEmpty()) {
      return List.of();
    }
    List<Long> questionIds = links.stream().map(SurveyTemplateQuestion::getQuestionId).toList();
    Map<Long, SurveyQuestion> questionMap = surveyQuestionMapper.selectList(
            Wrappers.lambdaQuery(SurveyQuestion.class)
                .eq(SurveyQuestion::getIsDeleted, 0)
                .in(SurveyQuestion::getId, questionIds))
        .stream().collect(Collectors.toMap(SurveyQuestion::getId, q -> q, (a, b) -> a));
    List<Map<String, Object>> questions = new ArrayList<>();
    for (SurveyTemplateQuestion link : links) {
      SurveyQuestion question = questionMap.get(link.getQuestionId());
      if (question == null) {
        continue;
      }
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("id", String.valueOf(question.getId()));
      item.put("title", question.getTitle());
      item.put("type", question.getQuestionType());
      item.put("optionsJson", question.getOptionsJson() == null ? "[]" : question.getOptionsJson());
      item.put("required", Objects.equals(link.getRequiredFlag(), 1) || Objects.equals(question.getRequiredFlag(), 1));
      questions.add(item);
    }
    return questions;
  }

  @PostMapping("/surveys/{templateId}/submit")
  @Transactional(rollbackFor = Exception.class)
  public Result<Boolean> submitSurvey(@PathVariable Long templateId, @RequestBody SurveySubmitRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long familyUserId = AuthContext.getStaffId();
    SurveyTemplate template = surveyTemplateMapper.selectOne(
        Wrappers.lambdaQuery(SurveyTemplate.class)
            .eq(SurveyTemplate::getIsDeleted, 0)
            .eq(SurveyTemplate::getOrgId, orgId)
            .eq(SurveyTemplate::getId, templateId)
            .eq(SurveyTemplate::getStatus, 1)
            .last("LIMIT 1"));
    if (template == null) {
      throw new IllegalArgumentException("问卷不存在或已停止收集");
    }
    Long existing = surveySubmissionMapper.selectCount(
        Wrappers.lambdaQuery(SurveySubmission.class)
            .eq(SurveySubmission::getIsDeleted, 0)
            .eq(SurveySubmission::getOrgId, orgId)
            .eq(SurveySubmission::getTemplateId, templateId)
            .eq(SurveySubmission::getSubmitterId, familyUserId)
            .eq(SurveySubmission::getSubmitterRole, "FAMILY"));
    if (existing != null && existing > 0) {
      throw new IllegalArgumentException("您已提交过该问卷，感谢参与");
    }

    boolean anonymous = Objects.equals(template.getAnonymousFlag(), 1);
    SurveySubmission submission = new SurveySubmission();
    submission.setOrgId(orgId);
    submission.setTemplateId(templateId);
    submission.setTargetType(template.getTargetType());
    submission.setSubmitterId(familyUserId);
    submission.setSubmitterName(anonymous ? "匿名家属" : AuthContext.getUsername());
    submission.setSubmitterRole("FAMILY");
    submission.setAnonymousFlag(anonymous ? 1 : 0);
    submission.setStatus(1);
    surveySubmissionMapper.insert(submission);

    List<SurveyAnswer> answers = request.getAnswers() == null ? List.of() : request.getAnswers();
    for (SurveyAnswer answer : answers) {
      if (answer == null || answer.getQuestionId() == null) {
        continue;
      }
      SurveySubmissionItem item = new SurveySubmissionItem();
      item.setOrgId(orgId);
      item.setSubmissionId(submission.getId());
      item.setQuestionId(answer.getQuestionId());
      item.setAnswerText(answer.getAnswerText());
      item.setAnswerJson(answer.getAnswerText() == null ? null : "[\"" + answer.getAnswerText().replace("\"", "\\\"") + "\"]");
      surveySubmissionItemMapper.insert(item);
    }
    return Result.ok(true);
  }

  public static class SurveySubmitRequest {
    private List<SurveyAnswer> answers;

    public List<SurveyAnswer> getAnswers() { return answers; }
    public void setAnswers(List<SurveyAnswer> answers) { this.answers = answers; }
  }

  public static class SurveyAnswer {
    private Long questionId;
    private String answerText;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
  }

  // ---------- 账单明细 ----------

  @GetMapping("/payment/bill-items")
  public Result<Map<String, Object>> billItems(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    Long familyUserId = AuthContext.getStaffId();
    List<Long> boundElderIds = elderFamilyMapper.selectList(
            Wrappers.lambdaQuery(ElderFamily.class)
                .eq(ElderFamily::getIsDeleted, 0)
                .eq(ElderFamily::getOrgId, orgId)
                .eq(ElderFamily::getFamilyUserId, familyUserId))
        .stream().map(ElderFamily::getElderId).filter(Objects::nonNull).distinct().toList();

    Map<String, Object> response = new HashMap<>();
    response.put("month", month == null || month.isBlank()
        ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) : month);
    response.put("items", List.of());
    response.put("totalAmount", BigDecimal.ZERO);
    if (boundElderIds.isEmpty()) {
      return Result.ok(response);
    }
    Long targetElderId = elderId != null && boundElderIds.contains(elderId) ? elderId : boundElderIds.get(0);
    String targetMonth = (String) response.get("month");

    BillMonthly bill = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, targetElderId)
            .eq(BillMonthly::getBillMonth, targetMonth)
            .last("LIMIT 1"));
    if (bill == null) {
      return Result.ok(response);
    }
    List<BillItem> items = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getIsDeleted, 0)
            .eq(BillItem::getOrgId, orgId)
            .eq(BillItem::getBillMonthlyId, bill.getId())
            .orderByDesc(BillItem::getAmount));
    List<Map<String, Object>> itemList = new ArrayList<>();
    for (BillItem item : items) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("name", item.getItemName() == null || item.getItemName().isBlank()
          ? billItemTypeLabel(item.getItemType()) : item.getItemName());
      row.put("type", billItemTypeLabel(item.getItemType()));
      row.put("amount", item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
      row.put("remark", item.getRemark() == null ? "" : item.getRemark());
      itemList.add(row);
    }
    response.put("items", itemList);
    response.put("totalAmount", bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount());
    return Result.ok(response);
  }

  private String billItemTypeLabel(String type) {
    if (type == null) {
      return "其他费用";
    }
    switch (type.toUpperCase()) {
      case "BED":
        return "床位费";
      case "CARE":
      case "NURSING":
        return "护理费";
      case "MEAL":
        return "餐费";
      case "MALL":
      case "CONSUME":
        return "商城/消费";
      case "MEDICAL":
        return "医疗费";
      default:
        return type;
    }
  }
}
