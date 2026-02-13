package com.zhiyangyun.care.report.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.report.entity.ReportRecord;
import com.zhiyangyun.care.report.mapper.ReportRecordMapper;
import com.zhiyangyun.care.report.model.ReportResponse;
import com.zhiyangyun.care.report.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {
  private final ReportRecordMapper reportMapper;

  public ReportServiceImpl(ReportRecordMapper reportMapper) {
    this.reportMapper = reportMapper;
  }

  @Override
  public IPage<ReportResponse> page(Long orgId, long pageNo, long pageSize, String keyword, String sortBy, String sortOrder) {
    var wrapper = Wrappers.lambdaQuery(ReportRecord.class)
        .eq(ReportRecord::getIsDeleted, 0)
        .eq(orgId != null, ReportRecord::getOrgId, orgId)
        .like(keyword != null && !keyword.isBlank(), ReportRecord::getName, keyword);

    boolean asc = "asc".equalsIgnoreCase(sortOrder);
    if ("generatedAt".equals(sortBy)) {
      wrapper.orderBy(true, asc, ReportRecord::getGeneratedAt);
    } else if ("name".equals(sortBy)) {
      wrapper.orderBy(true, asc, ReportRecord::getName);
    } else {
      wrapper.orderByDesc(ReportRecord::getGeneratedAt);
    }

    IPage<ReportRecord> page = reportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<ReportResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      ReportResponse response = new ReportResponse();
      response.setId(item.getId());
      response.setName(item.getName());
      response.setGeneratedAt(item.getGeneratedAt());
      response.setUrl(item.getUrl());
      return response;
    }).toList());
    return resp;
  }
}
