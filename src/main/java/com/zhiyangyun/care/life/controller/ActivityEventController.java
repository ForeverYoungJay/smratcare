package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.ActivityEvent;
import com.zhiyangyun.care.life.mapper.ActivityEventMapper;
import com.zhiyangyun.care.life.model.ActivityEventRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
@RequestMapping("/api/life/activity")
public class ActivityEventController {
  private final ActivityEventMapper eventMapper;

  public ActivityEventController(ActivityEventMapper eventMapper) {
    this.eventMapper = eventMapper;
  }

  @GetMapping("/page")
  public Result<IPage<ActivityEvent>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ActivityEvent.class)
        .eq(ActivityEvent::getIsDeleted, 0)
        .eq(orgId != null, ActivityEvent::getOrgId, orgId);
    if (dateFrom != null && dateTo != null) {
      wrapper.between(ActivityEvent::getEventDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(ActivityEvent::getEventDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(ActivityEvent::getEventDate, dateTo);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ActivityEvent::getTitle, keyword)
          .or().like(ActivityEvent::getOrganizer, keyword));
    }
    wrapper.orderByDesc(ActivityEvent::getEventDate);
    return Result.ok(eventMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<ActivityEvent> create(@Valid @RequestBody ActivityEventRequest request) {
    Long orgId = AuthContext.getOrgId();
    ActivityEvent event = new ActivityEvent();
    event.setTenantId(orgId);
    event.setOrgId(orgId);
    event.setTitle(request.getTitle());
    event.setEventDate(request.getEventDate());
    event.setStartTime(request.getStartTime());
    event.setEndTime(request.getEndTime());
    event.setLocation(request.getLocation());
    event.setOrganizer(request.getOrganizer());
    event.setContent(request.getContent());
    event.setStatus(request.getStatus() == null ? "PLANNED" : request.getStatus());
    event.setRemark(request.getRemark());
    event.setCreatedBy(AuthContext.getStaffId());
    eventMapper.insert(event);
    return Result.ok(event);
  }

  @PutMapping("/{id}")
  public Result<ActivityEvent> update(@PathVariable Long id, @Valid @RequestBody ActivityEventRequest request) {
    ActivityEvent existing = eventMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setTitle(request.getTitle());
    existing.setEventDate(request.getEventDate());
    existing.setStartTime(request.getStartTime());
    existing.setEndTime(request.getEndTime());
    existing.setLocation(request.getLocation());
    existing.setOrganizer(request.getOrganizer());
    existing.setContent(request.getContent());
    existing.setStatus(request.getStatus());
    existing.setRemark(request.getRemark());
    eventMapper.updateById(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    ActivityEvent existing = eventMapper.selectById(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      eventMapper.updateById(existing);
    }
    return Result.ok(null);
  }
}
