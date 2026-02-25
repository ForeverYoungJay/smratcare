package com.zhiyangyun.care.oa.model;

import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaTodo;
import java.util.List;
import lombok.Data;

@Data
public class OaPortalSummary {
  private List<OaNotice> notices;
  private List<OaTodo> todos;
  private Long openTodoCount;
  private Long overdueTodoCount;
  private Long pendingApprovalCount;
  private Long ongoingTaskCount;
  private Long submittedReportCount;
}
