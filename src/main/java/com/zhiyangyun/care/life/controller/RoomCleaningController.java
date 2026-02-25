package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.life.entity.RoomCleaningTask;
import com.zhiyangyun.care.life.mapper.RoomCleaningTaskMapper;
import com.zhiyangyun.care.life.model.RoomCleaningTaskRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/life/room-cleaning")
public class RoomCleaningController {
  private final RoomCleaningTaskMapper taskMapper;
  private final RoomMapper roomMapper;

  public RoomCleaningController(RoomCleaningTaskMapper taskMapper, RoomMapper roomMapper) {
    this.taskMapper = taskMapper;
    this.roomMapper = roomMapper;
  }

  @GetMapping("/page")
  public Result<IPage<RoomCleaningTask>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), RoomCleaningTask::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(RoomCleaningTask::getRoomNo, keyword)
          .or().like(RoomCleaningTask::getCleanerName, keyword));
    }
    if (dateFrom != null && dateTo != null) {
      wrapper.between(RoomCleaningTask::getPlanDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(RoomCleaningTask::getPlanDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(RoomCleaningTask::getPlanDate, dateTo);
    }
    wrapper.orderByDesc(RoomCleaningTask::getPlanDate).orderByDesc(RoomCleaningTask::getCreateTime);
    return Result.ok(taskMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<RoomCleaningTask> create(@Valid @RequestBody RoomCleaningTaskRequest request) {
    Long orgId = AuthContext.getOrgId();
    RoomCleaningTask task = new RoomCleaningTask();
    task.setTenantId(orgId);
    task.setOrgId(orgId);
    task.setRoomId(request.getRoomId());
    task.setRoomNo(resolveRoomNo(request.getRoomId()));
    task.setCleanerName(request.getCleanerName());
    task.setPlanDate(request.getPlanDate());
    task.setStatus(request.getStatus() == null ? "PENDING" : request.getStatus());
    task.setRemark(request.getRemark());
    task.setCreatedBy(AuthContext.getStaffId());
    taskMapper.insert(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}")
  public Result<RoomCleaningTask> update(@PathVariable Long id, @Valid @RequestBody RoomCleaningTaskRequest request) {
    RoomCleaningTask task = taskMapper.selectById(id);
    if (task == null) {
      return Result.ok(null);
    }
    task.setRoomId(request.getRoomId());
    task.setRoomNo(resolveRoomNo(request.getRoomId()));
    task.setCleanerName(request.getCleanerName());
    task.setPlanDate(request.getPlanDate());
    task.setStatus(request.getStatus() == null ? task.getStatus() : request.getStatus());
    task.setRemark(request.getRemark());
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @PutMapping("/{id}/done")
  public Result<RoomCleaningTask> done(@PathVariable Long id) {
    RoomCleaningTask task = taskMapper.selectById(id);
    if (task == null) {
      return Result.ok(null);
    }
    task.setStatus("DONE");
    task.setCleanedAt(LocalDateTime.now());
    taskMapper.updateById(task);
    return Result.ok(task);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    RoomCleaningTask task = taskMapper.selectById(id);
    if (task != null) {
      task.setIsDeleted(1);
      taskMapper.updateById(task);
    }
    return Result.ok(null);
  }

  private String resolveRoomNo(Long roomId) {
    if (roomId == null) {
      return null;
    }
    Room room = roomMapper.selectById(roomId);
    return room == null ? null : room.getRoomNo();
  }
}
