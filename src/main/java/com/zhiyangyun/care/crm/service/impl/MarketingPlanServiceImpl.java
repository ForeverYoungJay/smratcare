package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlan;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlanApproval;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlanDepartment;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlanPerformance;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlanReceipt;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanApprovalMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanDepartmentMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanPerformanceMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanReceiptMapper;
import com.zhiyangyun.care.crm.model.MarketingPlanApprovalRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanReadConfirmRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanReceiptResponse;
import com.zhiyangyun.care.crm.model.MarketingPlanRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanResponse;
import com.zhiyangyun.care.crm.model.MarketingPlanWorkflowSummaryResponse;
import com.zhiyangyun.care.crm.service.MarketingPlanService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MarketingPlanServiceImpl implements MarketingPlanService {
  private static final Set<String> EDITABLE_STATUSES = Set.of("DRAFT", "REJECTED", "INACTIVE");

  private final CrmMarketingPlanMapper marketingPlanMapper;
  private final CrmMarketingPlanDepartmentMapper planDepartmentMapper;
  private final CrmMarketingPlanApprovalMapper approvalMapper;
  private final CrmMarketingPlanReceiptMapper receiptMapper;
  private final CrmMarketingPlanPerformanceMapper performanceMapper;
  private final CrmLeadMapper crmLeadMapper;
  private final DepartmentMapper departmentMapper;
  private final StaffMapper staffMapper;
  private final OaTodoMapper todoMapper;
  private final OaApprovalMapper oaApprovalMapper;

  public MarketingPlanServiceImpl(
      CrmMarketingPlanMapper marketingPlanMapper,
      CrmMarketingPlanDepartmentMapper planDepartmentMapper,
      CrmMarketingPlanApprovalMapper approvalMapper,
      CrmMarketingPlanReceiptMapper receiptMapper,
      CrmMarketingPlanPerformanceMapper performanceMapper,
      CrmLeadMapper crmLeadMapper,
      DepartmentMapper departmentMapper,
      StaffMapper staffMapper,
      OaTodoMapper todoMapper,
      OaApprovalMapper oaApprovalMapper) {
    this.marketingPlanMapper = marketingPlanMapper;
    this.planDepartmentMapper = planDepartmentMapper;
    this.approvalMapper = approvalMapper;
    this.receiptMapper = receiptMapper;
    this.performanceMapper = performanceMapper;
    this.crmLeadMapper = crmLeadMapper;
    this.departmentMapper = departmentMapper;
    this.staffMapper = staffMapper;
    this.todoMapper = todoMapper;
    this.oaApprovalMapper = oaApprovalMapper;
  }

  @Override
  public IPage<MarketingPlanResponse> page(
      Long orgId,
      long pageNo,
      long pageSize,
      String moduleType,
      String status,
      String keyword) {
    Page<CrmMarketingPlan> page = new Page<>(pageNo, pageSize);
    LambdaQueryWrapper<CrmMarketingPlan> query = baseQuery(orgId)
        .eq(StringUtils.hasText(moduleType), CrmMarketingPlan::getModuleType, normalizeModuleType(moduleType))
        .eq(StringUtils.hasText(status), CrmMarketingPlan::getStatus, normalizeStatus(status))
        .and(StringUtils.hasText(keyword), w -> w
            .like(CrmMarketingPlan::getTitle, keyword)
            .or()
            .like(CrmMarketingPlan::getSummary, keyword)
            .or()
            .like(CrmMarketingPlan::getTarget, keyword)
            .or()
            .like(CrmMarketingPlan::getCampaignCode, keyword)
            .or()
            .like(CrmMarketingPlan::getSourceTag, keyword)
            .or()
            .like(CrmMarketingPlan::getOwner, keyword))
        .orderByAsc(CrmMarketingPlan::getPriority)
        .orderByDesc(CrmMarketingPlan::getEffectiveDate)
        .orderByDesc(CrmMarketingPlan::getCreateTime);
    Page<CrmMarketingPlan> data = marketingPlanMapper.selectPage(page, query);
    Page<MarketingPlanResponse> result = new Page<>(data.getCurrent(), data.getSize(), data.getTotal());
    result.setRecords(data.getRecords().stream().map(item -> toResponse(item, null)).toList());
    return result;
  }

  @Override
  public List<MarketingPlanResponse> listByModule(Long orgId, String moduleType) {
    LambdaQueryWrapper<CrmMarketingPlan> query = baseQuery(orgId)
        .eq(StringUtils.hasText(moduleType), CrmMarketingPlan::getModuleType, normalizeModuleType(moduleType))
        .in(CrmMarketingPlan::getStatus, List.of("PUBLISHED", "ACTIVE"))
        .orderByAsc(CrmMarketingPlan::getPriority)
        .orderByDesc(CrmMarketingPlan::getEffectiveDate)
        .orderByDesc(CrmMarketingPlan::getUpdateTime);
    return marketingPlanMapper.selectList(query).stream().map(item -> toResponse(item, null)).toList();
  }

  @Override
  public MarketingPlanResponse detail(Long orgId, Long staffId, Long id) {
    return toResponse(getById(orgId, id), staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse create(Long orgId, Long staffId, MarketingPlanRequest request) {
    CrmMarketingPlan entity = new CrmMarketingPlan();
    entity.setOrgId(orgId);
    entity.setTenantId(orgId);
    entity.setCreatedBy(staffId);
    applyRequest(entity, request);
    marketingPlanMapper.insert(entity);
    replaceLinkedDepartments(orgId, entity.getId(), request == null ? null : request.getLinkedDepartmentIds());
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse update(Long orgId, Long id, MarketingPlanRequest request) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!EDITABLE_STATUSES.contains(entity.getStatus())) {
      throw new IllegalArgumentException("仅草稿/驳回/停用状态方案可编辑");
    }
    applyRequest(entity, request);
    marketingPlanMapper.updateById(entity);
    replaceLinkedDepartments(orgId, entity.getId(), request == null ? null : request.getLinkedDepartmentIds());
    return toResponse(entity, null);
  }

  @Override
  @Transactional
  public MarketingPlanResponse submit(Long orgId, Long staffId, Long id) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!EDITABLE_STATUSES.contains(entity.getStatus())) {
      throw new IllegalArgumentException("当前状态不能提交审批");
    }
    entity.setStatus("PENDING_APPROVAL");
    marketingPlanMapper.updateById(entity);
    syncApprovalTicketOnSubmit(orgId, staffId, entity);
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse approve(Long orgId, Long staffId, Long id, MarketingPlanApprovalRequest request) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
      throw new IllegalArgumentException("仅待审批状态方案可审批");
    }
    CrmMarketingPlanApproval approval = buildApproval(orgId, id, staffId, "APPROVED", request == null ? null : request.getRemark());
    approvalMapper.insert(approval);
    entity.setStatus("APPROVED");
    marketingPlanMapper.updateById(entity);
    syncApprovalTicketStatus(orgId, id, "APPROVED", request == null ? null : request.getRemark());
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse reject(Long orgId, Long staffId, Long id, MarketingPlanApprovalRequest request) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
      throw new IllegalArgumentException("仅待审批状态方案可驳回");
    }
    CrmMarketingPlanApproval approval = buildApproval(orgId, id, staffId, "REJECTED", request == null ? null : request.getRemark());
    approvalMapper.insert(approval);
    entity.setStatus("REJECTED");
    marketingPlanMapper.updateById(entity);
    syncApprovalTicketStatus(orgId, id, "REJECTED", request == null ? null : request.getRemark());
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse publish(Long orgId, Long staffId, Long id) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!"APPROVED".equals(entity.getStatus())
        && !"INACTIVE".equals(entity.getStatus())
        && !"PUBLISHED".equals(entity.getStatus())) {
      throw new IllegalArgumentException("仅审批通过/停用/已发布方案可发布");
    }
    entity.setStatus("PUBLISHED");
    marketingPlanMapper.updateById(entity);
    syncPublishLinkages(orgId, staffId, entity);
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse deactivate(Long orgId, Long staffId, Long id) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!"PUBLISHED".equals(entity.getStatus())) {
      throw new IllegalArgumentException("仅已发布方案可停用");
    }
    entity.setStatus("INACTIVE");
    marketingPlanMapper.updateById(entity);
    return toResponse(entity, staffId);
  }

  @Override
  @Transactional
  public MarketingPlanResponse confirmRead(Long orgId, Long staffId, Long id, MarketingPlanReadConfirmRequest request) {
    CrmMarketingPlan entity = getById(orgId, id);
    if (!"PUBLISHED".equals(entity.getStatus()) && !"ACTIVE".equals(entity.getStatus())) {
      throw new IllegalArgumentException("仅已发布方案可确认阅读");
    }
    String action = normalizeReadAction(request == null ? null : request.getAction());
    String detail = trimToNull(request == null ? null : request.getActionDetail());
    if ("IMPROVE".equals(action) && detail == null) {
      throw new IllegalArgumentException("提交改进建议时必须填写改进详情");
    }

    StaffAccount staff = staffMapper.selectById(staffId);
    String staffName = staff == null ? String.valueOf(staffId) : trimToNull(staff.getRealName());
    CrmMarketingPlanReceipt receipt = receiptMapper.selectOne(Wrappers.lambdaQuery(CrmMarketingPlanReceipt.class)
        .eq(CrmMarketingPlanReceipt::getIsDeleted, 0)
        .eq(CrmMarketingPlanReceipt::getOrgId, orgId)
        .eq(CrmMarketingPlanReceipt::getPlanId, id)
        .eq(CrmMarketingPlanReceipt::getStaffId, staffId)
        .last("LIMIT 1"));
    LocalDateTime now = LocalDateTime.now();
    if (receipt == null) {
      receipt = new CrmMarketingPlanReceipt();
      receipt.setTenantId(orgId);
      receipt.setOrgId(orgId);
      receipt.setPlanId(id);
      receipt.setStaffId(staffId);
      receipt.setStaffName(staffName);
      receipt.setReadTime(now);
      receipt.setAction(action);
      receipt.setActionDetail(detail);
      receipt.setActionTime(now);
      receiptMapper.insert(receipt);
    } else {
      if (receipt.getReadTime() == null) {
        receipt.setReadTime(now);
      }
      receipt.setAction(action);
      receipt.setActionDetail(detail);
      receipt.setActionTime(now);
      receipt.setStaffName(staffName);
      receiptMapper.updateById(receipt);
    }
    syncPerformanceOnRead(orgId, id, staffId, staffName, action);
    return toResponse(entity, staffId);
  }

  @Override
  public List<MarketingPlanReceiptResponse> listReceipts(Long orgId, Long id) {
    getById(orgId, id);
    List<CrmMarketingPlanReceipt> receipts = receiptMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlanReceipt.class)
        .eq(CrmMarketingPlanReceipt::getIsDeleted, 0)
        .eq(CrmMarketingPlanReceipt::getOrgId, orgId)
        .eq(CrmMarketingPlanReceipt::getPlanId, id)
        .orderByDesc(CrmMarketingPlanReceipt::getActionTime)
        .orderByDesc(CrmMarketingPlanReceipt::getReadTime));
    return receipts.stream().map(this::toReceiptResponse).toList();
  }

  @Override
  public MarketingPlanWorkflowSummaryResponse workflowSummary(Long orgId, Long id) {
    getById(orgId, id);
    List<CrmMarketingPlanReceipt> receipts = receiptMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlanReceipt.class)
        .eq(CrmMarketingPlanReceipt::getIsDeleted, 0)
        .eq(CrmMarketingPlanReceipt::getOrgId, orgId)
        .eq(CrmMarketingPlanReceipt::getPlanId, id));
    List<StaffAccount> staffs = listActiveStaff(orgId);
    long readCount = receipts.stream().filter(item -> item.getReadTime() != null).count();
    long agreeCount = receipts.stream().filter(item -> "AGREE".equals(item.getAction())).count();
    long improveCount = receipts.stream().filter(item -> "IMPROVE".equals(item.getAction())).count();

    MarketingPlanWorkflowSummaryResponse response = new MarketingPlanWorkflowSummaryResponse();
    response.setPlanId(id);
    response.setTotalStaffCount(staffs.size());
    response.setReadCount(readCount);
    response.setUnreadCount(Math.max(staffs.size() - readCount, 0));
    response.setAgreeCount(agreeCount);
    response.setImproveCount(improveCount);
    response.setReceipts(receipts.stream().map(this::toReceiptResponse).toList());
    return response;
  }

  @Override
  public void remove(Long orgId, Long id) {
    CrmMarketingPlan entity = getById(orgId, id);
    entity.setIsDeleted(1);
    marketingPlanMapper.updateById(entity);
    List<OaApproval> approvals = oaApprovalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "MARKETING_PLAN")
        .orderByDesc(OaApproval::getCreateTime)
        .last("LIMIT 300"));
    approvals.stream()
        .filter(item -> matchesPlanId(item.getFormData(), id))
        .forEach(item -> {
          item.setIsDeleted(1);
          oaApprovalMapper.updateById(item);
        });
  }

  private void syncPublishLinkages(Long orgId, Long staffId, CrmMarketingPlan plan) {
    List<StaffAccount> staffs = listActiveStaff(orgId);
    LocalDateTime dueTime = plan.getEffectiveDate() == null
        ? LocalDateTime.now().plusDays(1)
        : plan.getEffectiveDate().atTime(18, 0);

    for (StaffAccount staff : staffs) {
      CrmMarketingPlanReceipt receipt = receiptMapper.selectOne(Wrappers.lambdaQuery(CrmMarketingPlanReceipt.class)
          .eq(CrmMarketingPlanReceipt::getIsDeleted, 0)
          .eq(CrmMarketingPlanReceipt::getOrgId, orgId)
          .eq(CrmMarketingPlanReceipt::getPlanId, plan.getId())
          .eq(CrmMarketingPlanReceipt::getStaffId, staff.getId())
          .last("LIMIT 1"));
      if (receipt == null) {
        receipt = new CrmMarketingPlanReceipt();
        receipt.setTenantId(orgId);
        receipt.setOrgId(orgId);
        receipt.setPlanId(plan.getId());
        receipt.setStaffId(staff.getId());
        receipt.setStaffName(trimToNull(staff.getRealName()));
        receiptMapper.insert(receipt);
      }

      CrmMarketingPlanPerformance performance = performanceMapper.selectOne(Wrappers.lambdaQuery(CrmMarketingPlanPerformance.class)
          .eq(CrmMarketingPlanPerformance::getIsDeleted, 0)
          .eq(CrmMarketingPlanPerformance::getOrgId, orgId)
          .eq(CrmMarketingPlanPerformance::getPlanId, plan.getId())
          .eq(CrmMarketingPlanPerformance::getStaffId, staff.getId())
          .last("LIMIT 1"));
      if (performance == null) {
        performance = new CrmMarketingPlanPerformance();
        performance.setTenantId(orgId);
        performance.setOrgId(orgId);
        performance.setPlanId(plan.getId());
        performance.setStaffId(staff.getId());
        performance.setStaffName(trimToNull(staff.getRealName()));
        performance.setScore(BigDecimal.ZERO);
        performance.setMetric("MARKETING_PLAN_READ");
        performance.setSource("MARKETING_PLAN");
        performance.setStatus("PENDING");
        performanceMapper.insert(performance);
      }

      OaTodo todo = new OaTodo();
      String todoTitle = "营销方案待阅读：" + safeText(plan.getTitle(), "未命名方案");
      OaTodo existingTodo = todoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
          .eq(OaTodo::getIsDeleted, 0)
          .eq(OaTodo::getOrgId, orgId)
          .eq(OaTodo::getAssigneeId, staff.getId())
          .eq(OaTodo::getTitle, todoTitle)
          .eq(OaTodo::getStatus, "OPEN")
          .last("LIMIT 1"));
      if (existingTodo == null) {
        todo.setTenantId(orgId);
        todo.setOrgId(orgId);
        todo.setTitle(todoTitle);
        todo.setContent("请阅读并确认是否同意方案，若需优化请填写改进建议。");
        todo.setDueTime(dueTime);
        todo.setStatus("OPEN");
        todo.setAssigneeId(staff.getId());
        todo.setAssigneeName(trimToNull(staff.getRealName()));
        todo.setCreatedBy(staffId);
        todoMapper.insert(todo);
      }
    }
  }

  private void syncApprovalTicketOnSubmit(Long orgId, Long staffId, CrmMarketingPlan plan) {
    StaffAccount staff = staffMapper.selectById(staffId);
    OaApproval approval = findLatestMarketingApprovalByPlanId(orgId, plan.getId());
    if (approval != null && "PENDING".equals(approval.getStatus())) {
      approval.setTitle("营销方案审批：" + safeText(plan.getTitle(), "未命名方案"));
      approval.setApplicantId(staffId);
      approval.setApplicantName(staff == null ? String.valueOf(staffId) : safeText(staff.getRealName(), String.valueOf(staffId)));
      approval.setFormData(buildApprovalFormData(plan));
      approval.setRemark("营销方案重新提交审批");
      oaApprovalMapper.updateById(approval);
      return;
    }

    OaApproval newApproval = new OaApproval();
    newApproval.setTenantId(orgId);
    newApproval.setOrgId(orgId);
    newApproval.setApprovalType("MARKETING_PLAN");
    newApproval.setTitle("营销方案审批：" + safeText(plan.getTitle(), "未命名方案"));
    newApproval.setApplicantId(staffId);
    newApproval.setApplicantName(staff == null ? String.valueOf(staffId) : safeText(staff.getRealName(), String.valueOf(staffId)));
    newApproval.setFormData(buildApprovalFormData(plan));
    newApproval.setStatus("PENDING");
    newApproval.setRemark("营销方案提交审批");
    newApproval.setCreatedBy(staffId);
    oaApprovalMapper.insert(newApproval);
  }

  private void syncApprovalTicketStatus(Long orgId, Long planId, String status, String remark) {
    OaApproval approval = findLatestMarketingApprovalByPlanId(orgId, planId);
    if (approval == null) {
      return;
    }
    if ("PENDING".equals(approval.getStatus()) || !StringUtils.hasText(approval.getStatus())) {
      approval.setStatus(status);
    }
    if (StringUtils.hasText(remark)) {
      approval.setRemark(remark.trim());
    } else if (!StringUtils.hasText(approval.getRemark())) {
      approval.setRemark("APPROVED".equals(status) ? "营销方案审批通过" : "营销方案审批驳回");
    }
    oaApprovalMapper.updateById(approval);
  }

  private OaApproval findLatestMarketingApprovalByPlanId(Long orgId, Long planId) {
    if (planId == null) {
      return null;
    }
    List<OaApproval> approvals = oaApprovalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "MARKETING_PLAN")
        .orderByDesc(OaApproval::getCreateTime)
        .last("LIMIT 300"));
    return approvals.stream()
        .filter(item -> matchesPlanId(item.getFormData(), planId))
        .findFirst()
        .orElse(null);
  }

  private boolean matchesPlanId(String formData, Long planId) {
    String data = trimToNull(formData);
    if (data == null || planId == null) {
      return false;
    }
    String stringToken = "\"planId\":\"" + planId + "\"";
    String numericToken = "\"planId\":" + planId;
    return data.contains(stringToken) || data.contains(numericToken);
  }

  private String buildApprovalFormData(CrmMarketingPlan plan) {
    String planId = String.valueOf(plan.getId());
    String moduleType = safeJsonText(plan.getModuleType());
    String title = safeJsonText(plan.getTitle());
    return "{\"planId\":\"" + planId + "\",\"moduleType\":\"" + moduleType + "\",\"title\":\"" + title + "\"}";
  }

  private String safeJsonText(String value) {
    return safeText(value, "").replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private void syncPerformanceOnRead(Long orgId, Long planId, Long staffId, String staffName, String action) {
    CrmMarketingPlanPerformance performance = performanceMapper.selectOne(Wrappers.lambdaQuery(CrmMarketingPlanPerformance.class)
        .eq(CrmMarketingPlanPerformance::getIsDeleted, 0)
        .eq(CrmMarketingPlanPerformance::getOrgId, orgId)
        .eq(CrmMarketingPlanPerformance::getPlanId, planId)
        .eq(CrmMarketingPlanPerformance::getStaffId, staffId)
        .last("LIMIT 1"));
    if (performance == null) {
      performance = new CrmMarketingPlanPerformance();
      performance.setTenantId(orgId);
      performance.setOrgId(orgId);
      performance.setPlanId(planId);
      performance.setStaffId(staffId);
      performance.setStaffName(staffName);
      performance.setMetric("MARKETING_PLAN_READ");
      performance.setSource("MARKETING_PLAN");
      performance.setScore(BigDecimal.ZERO);
      performance.setStatus("PENDING");
      performanceMapper.insert(performance);
    }
    performance.setStaffName(staffName);
    performance.setStatus("CONFIRMED");
    performance.setScore("AGREE".equals(action) ? BigDecimal.ONE : new BigDecimal("0.50"));
    performanceMapper.updateById(performance);
  }

  private List<StaffAccount> listActiveStaff(Long orgId) {
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1));
  }

  private void replaceLinkedDepartments(Long orgId, Long planId, List<Long> linkedDepartmentIds) {
    List<CrmMarketingPlanDepartment> existing = planDepartmentMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlanDepartment.class)
        .eq(CrmMarketingPlanDepartment::getIsDeleted, 0)
        .eq(CrmMarketingPlanDepartment::getOrgId, orgId)
        .eq(CrmMarketingPlanDepartment::getPlanId, planId));
    for (CrmMarketingPlanDepartment item : existing) {
      item.setIsDeleted(1);
      planDepartmentMapper.updateById(item);
    }

    if (linkedDepartmentIds == null || linkedDepartmentIds.isEmpty()) {
      return;
    }
    List<Long> deptIds = linkedDepartmentIds.stream().filter(Objects::nonNull).distinct().toList();
    if (deptIds.isEmpty()) {
      return;
    }

    List<Department> departments = departmentMapper.selectList(Wrappers.lambdaQuery(Department.class)
        .eq(Department::getIsDeleted, 0)
        .eq(orgId != null, Department::getOrgId, orgId)
        .in(Department::getId, deptIds));
    Map<Long, String> deptNameMap = new HashMap<>();
    for (Department department : departments) {
      deptNameMap.put(department.getId(), trimToNull(department.getDeptName()));
    }

    for (Long deptId : deptIds) {
      CrmMarketingPlanDepartment link = new CrmMarketingPlanDepartment();
      link.setTenantId(orgId);
      link.setOrgId(orgId);
      link.setPlanId(planId);
      link.setDepartmentId(deptId);
      link.setDepartmentName(deptNameMap.get(deptId));
      planDepartmentMapper.insert(link);
    }
  }

  private LambdaQueryWrapper<CrmMarketingPlan> baseQuery(Long orgId) {
    return new LambdaQueryWrapper<CrmMarketingPlan>()
        .eq(CrmMarketingPlan::getIsDeleted, 0)
        .eq(Objects.nonNull(orgId), CrmMarketingPlan::getOrgId, orgId);
  }

  private CrmMarketingPlan getById(Long orgId, Long id) {
    CrmMarketingPlan entity = marketingPlanMapper.selectById(id);
    if (entity == null || entity.getIsDeleted() == 1 || !Objects.equals(entity.getOrgId(), orgId)) {
      throw new IllegalArgumentException("营销方案不存在或已删除");
    }
    return entity;
  }

  private void applyRequest(CrmMarketingPlan entity, MarketingPlanRequest request) {
    entity.setModuleType(normalizeModuleType(request.getModuleType()));
    entity.setTitle(trimToNull(request.getTitle()));
    entity.setSummary(trimToNull(request.getSummary()));
    entity.setContent(trimToNull(request.getContent()));
    entity.setQuarterLabel(trimToNull(request.getQuarterLabel()));
    entity.setTarget(trimToNull(request.getTarget()));
    entity.setOwner(trimToNull(request.getOwner()));
    entity.setCampaignCode(trimToNull(request.getCampaignCode()));
    entity.setSourceTag(trimToNull(request.getSourceTag()));
    entity.setBudgetAmount(normalizeBudget(request.getBudgetAmount()));
    entity.setTargetLeadCount(normalizeCount(request.getTargetLeadCount()));
    entity.setTargetReservationCount(normalizeCount(request.getTargetReservationCount()));
    entity.setTargetContractCount(normalizeCount(request.getTargetContractCount()));
    entity.setPriority(request.getPriority() == null ? 50 : request.getPriority());
    entity.setStatus(normalizeStatus(request.getStatus()));
    entity.setEffectiveDate(request.getEffectiveDate());
  }

  private MarketingPlanResponse toResponse(CrmMarketingPlan entity, Long currentStaffId) {
    MarketingPlanResponse response = new MarketingPlanResponse();
    response.setId(entity.getId());
    response.setModuleType(entity.getModuleType());
    response.setTitle(entity.getTitle());
    response.setSummary(entity.getSummary());
    response.setContent(entity.getContent());
    response.setQuarterLabel(entity.getQuarterLabel());
    response.setTarget(entity.getTarget());
    response.setOwner(entity.getOwner());
    response.setCampaignCode(entity.getCampaignCode());
    response.setSourceTag(entity.getSourceTag());
    response.setBudgetAmount(entity.getBudgetAmount());
    response.setTargetLeadCount(entity.getTargetLeadCount());
    response.setTargetReservationCount(entity.getTargetReservationCount());
    response.setTargetContractCount(entity.getTargetContractCount());
    response.setPriority(entity.getPriority());
    response.setStatus(entity.getStatus());
    response.setEffectiveDate(entity.getEffectiveDate());
    applyEffectMetrics(entity, response);

    List<CrmMarketingPlanDepartment> departments = planDepartmentMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlanDepartment.class)
        .eq(CrmMarketingPlanDepartment::getIsDeleted, 0)
        .eq(CrmMarketingPlanDepartment::getOrgId, entity.getOrgId())
        .eq(CrmMarketingPlanDepartment::getPlanId, entity.getId()));
    response.setLinkedDepartmentIds(departments.stream().map(CrmMarketingPlanDepartment::getDepartmentId).toList());
    response.setLinkedDepartmentNames(departments.stream()
        .map(CrmMarketingPlanDepartment::getDepartmentName)
        .filter(StringUtils::hasText)
        .distinct()
        .toList());

    CrmMarketingPlanApproval latestApproval = approvalMapper.selectOne(Wrappers.lambdaQuery(CrmMarketingPlanApproval.class)
        .eq(CrmMarketingPlanApproval::getIsDeleted, 0)
        .eq(CrmMarketingPlanApproval::getOrgId, entity.getOrgId())
        .eq(CrmMarketingPlanApproval::getPlanId, entity.getId())
        .orderByDesc(CrmMarketingPlanApproval::getCreateTime)
        .last("LIMIT 1"));
    if (latestApproval != null) {
      response.setLatestApprovalStatus(latestApproval.getStatus());
      response.setLatestApprovalRemark(latestApproval.getRemark());
      response.setLatestApprovalTime(latestApproval.getApprovedTime());
    }

    List<CrmMarketingPlanReceipt> receipts = receiptMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlanReceipt.class)
        .eq(CrmMarketingPlanReceipt::getIsDeleted, 0)
        .eq(CrmMarketingPlanReceipt::getOrgId, entity.getOrgId())
        .eq(CrmMarketingPlanReceipt::getPlanId, entity.getId()));
    long totalStaff = listActiveStaff(entity.getOrgId()).size();
    long readCount = receipts.stream().filter(item -> item.getReadTime() != null).count();
    long agreeCount = receipts.stream().filter(item -> "AGREE".equals(item.getAction())).count();
    long improveCount = receipts.stream().filter(item -> "IMPROVE".equals(item.getAction())).count();
    response.setTotalStaffCount(totalStaff);
    response.setReadCount(readCount);
    response.setUnreadCount(Math.max(totalStaff - readCount, 0));
    response.setAgreeCount(agreeCount);
    response.setImproveCount(improveCount);

    if (currentStaffId != null) {
      CrmMarketingPlanReceipt self = receipts.stream()
          .filter(item -> Objects.equals(item.getStaffId(), currentStaffId))
          .findFirst()
          .orElse(null);
      response.setCurrentUserRead(self != null && self.getReadTime() != null);
      response.setCurrentUserAction(self == null ? null : self.getAction());
    }

    response.setCreateTime(entity.getCreateTime());
    response.setUpdateTime(entity.getUpdateTime());
    return response;
  }

  private void applyEffectMetrics(CrmMarketingPlan entity, MarketingPlanResponse response) {
    if (entity == null || response == null) {
      return;
    }
    List<CrmLead> leads = crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(entity.getOrgId() != null, CrmLead::getTenantId, entity.getOrgId()));
    long actualLeadCount = leads.stream().filter(lead -> matchesPlanLead(entity, lead)).count();
    long actualReservationCount = leads.stream().filter(lead -> matchesPlanLead(entity, lead)).filter(this::hasReservationEvidence).count();
    long actualContractCount = leads.stream().filter(lead -> matchesPlanLead(entity, lead)).filter(this::isSignedLead).count();
    response.setActualLeadCount(actualLeadCount);
    response.setActualReservationCount(actualReservationCount);
    response.setActualContractCount(actualContractCount);
    response.setActualContractRate(actualLeadCount <= 0 ? 0D : Double.parseDouble(String.format(java.util.Locale.ROOT, "%.1f", (actualContractCount * 100D) / actualLeadCount)));
  }

  private boolean matchesPlanLead(CrmMarketingPlan plan, CrmLead lead) {
    if (plan == null || lead == null) {
      return false;
    }
    if (plan.getEffectiveDate() != null) {
      java.time.LocalDate consultDate = lead.getConsultDate();
      if (consultDate == null && lead.getCreateTime() != null) {
        consultDate = lead.getCreateTime().toLocalDate();
      }
      if (consultDate == null
          || consultDate.isBefore(plan.getEffectiveDate().minusDays(30))
          || consultDate.isAfter(plan.getEffectiveDate().plusDays(120))) {
        return false;
      }
    }
    String sourceTag = normalizeTraceText(plan.getSourceTag());
    String campaignCode = normalizeTraceText(plan.getCampaignCode());
    if (sourceTag == null && campaignCode == null) {
      return false;
    }
    String leadText = normalizeTraceText(String.join(" ",
        safeText(lead.getSource()),
        safeText(lead.getInfoSource()),
        safeText(lead.getMediaChannel()),
        safeText(lead.getCustomerTag()),
        safeText(lead.getReferralChannel()),
        safeText(lead.getReservationChannel()),
        safeText(lead.getRemark())));
    if (leadText == null) {
      return false;
    }
    boolean sourceMatched = sourceTag == null || leadText.contains(sourceTag);
    boolean campaignMatched = campaignCode == null || leadText.contains(campaignCode);
    return sourceMatched && campaignMatched;
  }

  private boolean hasReservationEvidence(CrmLead lead) {
    if (lead == null || isSignedLead(lead) || Integer.valueOf(3).equals(lead.getStatus())) {
      return false;
    }
    if (lead.getReservationBedId() != null || trimToNull(lead.getReservationRoomNo()) != null) {
      return true;
    }
    if (lead.getReservationAmount() != null && lead.getReservationAmount().compareTo(BigDecimal.ZERO) > 0) {
      return true;
    }
    String reservationStatus = trimToNull(lead.getReservationStatus());
    if (reservationStatus == null) {
      return false;
    }
    String text = reservationStatus.toLowerCase(java.util.Locale.ROOT);
    return text.contains("预") || text.contains("锁") || text.contains("reserve") || text.contains("lock");
  }

  private boolean isSignedLead(CrmLead lead) {
    return lead != null
        && (Integer.valueOf(1).equals(lead.getContractSignedFlag())
        || "SIGNED".equalsIgnoreCase(trimToNull(lead.getFlowStage())));
  }

  private BigDecimal normalizeBudget(BigDecimal budgetAmount) {
    if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) < 0) {
      return null;
    }
    return budgetAmount;
  }

  private Integer normalizeCount(Integer value) {
    if (value == null || value < 0) {
      return null;
    }
    return value;
  }

  private String normalizeTraceText(String value) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      return null;
    }
    return normalized.replace("　", " ").replace(" ", "").toLowerCase(java.util.Locale.ROOT);
  }

  private String safeText(String value) {
    return value == null ? "" : value;
  }

  private CrmMarketingPlanApproval buildApproval(
      Long orgId, Long id, Long staffId, String status, String remark) {
    StaffAccount staff = staffMapper.selectById(staffId);
    CrmMarketingPlanApproval approval = new CrmMarketingPlanApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    approval.setPlanId(id);
    approval.setApproverId(staffId);
    approval.setApproverName(staff == null ? String.valueOf(staffId) : trimToNull(staff.getRealName()));
    approval.setStatus(status);
    approval.setRemark(trimToNull(remark));
    approval.setApprovedTime(LocalDateTime.now());
    return approval;
  }

  private MarketingPlanReceiptResponse toReceiptResponse(CrmMarketingPlanReceipt item) {
    MarketingPlanReceiptResponse response = new MarketingPlanReceiptResponse();
    response.setStaffId(item.getStaffId());
    response.setStaffName(item.getStaffName());
    response.setReadTime(item.getReadTime());
    response.setAction(item.getAction());
    response.setActionDetail(item.getActionDetail());
    response.setActionTime(item.getActionTime());
    return response;
  }

  private String normalizeModuleType(String moduleType) {
    String value = trimToNull(moduleType);
    return value == null ? null : value.toUpperCase();
  }

  private String normalizeStatus(String status) {
    String value = trimToNull(status);
    if (value == null) {
      return "DRAFT";
    }
    String normalized = value.toUpperCase();
    if ("ACTIVE".equals(normalized)) {
      return "PUBLISHED";
    }
    if ("INACTIVE".equals(normalized) || "DRAFT".equals(normalized) || "PENDING_APPROVAL".equals(normalized)
        || "REJECTED".equals(normalized) || "APPROVED".equals(normalized) || "PUBLISHED".equals(normalized)) {
      return normalized;
    }
    throw new IllegalArgumentException("状态不支持：" + status);
  }

  private String normalizeReadAction(String action) {
    String value = trimToNull(action);
    if (value == null) {
      throw new IllegalArgumentException("确认动作不能为空");
    }
    String normalized = value.toUpperCase();
    if (!"AGREE".equals(normalized) && !"IMPROVE".equals(normalized)) {
      throw new IllegalArgumentException("确认动作仅支持 AGREE/IMPROVE");
    }
    return normalized;
  }

  private String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return value.trim();
  }

  private String safeText(String value, String fallback) {
    String trimmed = trimToNull(value);
    return trimmed == null ? fallback : trimmed;
  }
}
