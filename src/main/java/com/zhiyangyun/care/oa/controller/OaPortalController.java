package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.model.OaPortalSummary;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/portal")
public class OaPortalController {
  private final OaNoticeMapper noticeMapper;
  private final OaTodoMapper todoMapper;

  public OaPortalController(OaNoticeMapper noticeMapper, OaTodoMapper todoMapper) {
    this.noticeMapper = noticeMapper;
    this.todoMapper = todoMapper;
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
    OaPortalSummary summary = new OaPortalSummary();
    summary.setNotices(notices);
    summary.setTodos(todos);
    return Result.ok(summary);
  }
}
