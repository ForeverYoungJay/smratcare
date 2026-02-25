package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaWorkReport;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.mapper.OaWorkReportMapper;
import com.zhiyangyun.care.oa.model.OaPortalSummary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/portal")
public class OaPortalController {
  private final OaNoticeMapper noticeMapper;
  private final OaTodoMapper todoMapper;
  private final OaApprovalMapper approvalMapper;
  private final OaTaskMapper taskMapper;
  private final OaWorkReportMapper workReportMapper;

  public OaPortalController(
      OaNoticeMapper noticeMapper,
      OaTodoMapper todoMapper,
      OaApprovalMapper approvalMapper,
      OaTaskMapper taskMapper,
      OaWorkReportMapper workReportMapper) {
    this.noticeMapper = noticeMapper;
    this.todoMapper = todoMapper;
    this.approvalMapper = approvalMapper;
    this.taskMapper = taskMapper;
    this.workReportMapper = workReportMapper;
  }

  @GetMapping("/summary")
  public Result<OaPortalSummary> summary() {
    Long orgId = AuthContext.getOrgId();
    List<OaNotice> notices = noticeMapper.selectList(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .orderByDesc(OaNotice::getPublishTime)
        .last("LIMIT 5"));
    List<OaTodo> todos = todoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .orderByAsc(OaTodo::getDueTime)
        .last("LIMIT 8"));
    Long openTodoCount = todoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN"));
    Long overdueTodoCount = todoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .lt(OaTodo::getDueTime, LocalDateTime.now()));
    Long pendingApprovalCount = approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING"));
    Long ongoingTaskCount = taskMapper.selectCount(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .in(OaTask::getStatus, "TODO", "DOING"));
    Long submittedReportCount = workReportMapper.selectCount(Wrappers.lambdaQuery(OaWorkReport.class)
        .eq(OaWorkReport::getIsDeleted, 0)
        .eq(orgId != null, OaWorkReport::getOrgId, orgId)
        .eq(OaWorkReport::getStatus, "SUBMITTED")
        .ge(OaWorkReport::getReportDate, LocalDate.now().minusDays(30)));
    OaPortalSummary summary = new OaPortalSummary();
    summary.setNotices(notices);
    summary.setTodos(todos);
    summary.setOpenTodoCount(openTodoCount);
    summary.setOverdueTodoCount(overdueTodoCount);
    summary.setPendingApprovalCount(pendingApprovalCount);
    summary.setOngoingTaskCount(ongoingTaskCount);
    summary.setSubmittedReportCount(submittedReportCount);
    return Result.ok(summary);
  }
}
