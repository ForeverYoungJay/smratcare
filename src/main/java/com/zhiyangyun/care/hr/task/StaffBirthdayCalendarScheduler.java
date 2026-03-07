package com.zhiyangyun.care.hr.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StaffBirthdayCalendarScheduler {
  private final StaffProfileMapper staffProfileMapper;
  private final StaffMapper staffMapper;
  private final OaTaskMapper oaTaskMapper;
  private final OaTodoMapper oaTodoMapper;

  public StaffBirthdayCalendarScheduler(
      StaffProfileMapper staffProfileMapper,
      StaffMapper staffMapper,
      OaTaskMapper oaTaskMapper,
      OaTodoMapper oaTodoMapper) {
    this.staffProfileMapper = staffProfileMapper;
    this.staffMapper = staffMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.oaTodoMapper = oaTodoMapper;
  }

  @Scheduled(cron = "0 20 0 * * ?")
  public void refreshNextBirthdayTasks() {
    LocalDate today = LocalDate.now();
    autoCompleteExpiredBirthdayTodos(today);
    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .isNotNull(StaffProfile::getBirthday));
    if (profiles.isEmpty()) {
      return;
    }
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
            profiles.stream().map(StaffProfile::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    for (StaffProfile profile : profiles) {
      StaffAccount staff = staffMap.get(profile.getStaffId());
      if (staff == null || staff.getIsDeleted() != null && staff.getIsDeleted() == 1) {
        continue;
      }
      OaTask task = oaTaskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
          .eq(OaTask::getIsDeleted, 0)
          .eq(OaTask::getOrgId, staff.getOrgId())
          .eq(OaTask::getPlanCategory, "STAFF_BIRTHDAY")
          .eq(OaTask::getSourceTodoId, staff.getId())
          .last("LIMIT 1"));
      if (profile.getBirthday() == null) {
        if (task != null) {
          task.setIsDeleted(1);
          oaTaskMapper.updateById(task);
        }
        continue;
      }
      LocalDate nextBirthday = birthdayForYear(profile.getBirthday(), today.getYear());
      if (nextBirthday.isBefore(today)) {
        nextBirthday = birthdayForYear(profile.getBirthday(), today.getYear() + 1);
      }
      if (task == null) {
        task = new OaTask();
        task.setTenantId(staff.getOrgId());
        task.setOrgId(staff.getOrgId());
        task.setSourceTodoId(staff.getId());
        task.setCreatedBy(staff.getId());
        task.setPlanCategory("STAFF_BIRTHDAY");
      }
      task.setTitle("员工生日：" + safe(staff.getRealName()));
      task.setDescription("员工生日提醒，员工工号：" + safe(staff.getStaffNo()) + "，联系电话：" + safe(staff.getPhone()));
      task.setStartTime(LocalDateTime.of(nextBirthday, LocalTime.of(9, 0)));
      task.setEndTime(LocalDateTime.of(nextBirthday, LocalTime.of(18, 0)));
      task.setPriority("NORMAL");
      task.setStatus("OPEN");
      task.setAssigneeId(staff.getId());
      task.setAssigneeName(staff.getRealName());
      task.setCalendarType("WORK");
      task.setUrgency("NORMAL");
      task.setEventColor("#eb2f96");
      task.setCollaboratorIds(null);
      task.setCollaboratorNames(null);
      task.setIsRecurring(0);
      task.setRecurrenceRule(null);
      task.setRecurrenceInterval(null);
      task.setRecurrenceCount(null);
      if (task.getId() == null) {
        oaTaskMapper.insert(task);
      } else {
        oaTaskMapper.updateById(task);
      }
      if (nextBirthday.equals(today)) {
        syncBirthdayTodo(staff, nextBirthday, profile.getBirthday());
      }
    }
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }

  private LocalDate birthdayForYear(LocalDate birthday, int year) {
    int month = birthday.getMonthValue();
    int maxDay = java.time.Month.of(month).length(java.time.Year.isLeap(year));
    int day = Math.min(birthday.getDayOfMonth(), maxDay);
    return LocalDate.of(year, month, day);
  }

  private void syncBirthdayTodo(StaffAccount staff, LocalDate nextBirthday, LocalDate rawBirthday) {
    if (staff == null || staff.getId() == null) {
      return;
    }
    String marker = birthdayTodoMarker(staff.getId(), nextBirthday);
    OaTodo existed = oaTodoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(OaTodo::getOrgId, staff.getOrgId())
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, marker)
        .last("LIMIT 1"));
    if (existed != null) {
      return;
    }
    OaTodo todo = new OaTodo();
    todo.setTenantId(staff.getOrgId());
    todo.setOrgId(staff.getOrgId());
    todo.setTitle("【生日提醒】" + safe(staff.getRealName()) + " 今日生日");
    todo.setContent(marker + " 员工工号：" + safe(staff.getStaffNo()) + "，联系电话：" + safe(staff.getPhone())
        + "，生日：" + (rawBirthday == null ? "" : rawBirthday));
    todo.setDueTime(nextBirthday.atTime(9, 0));
    todo.setStatus("OPEN");
    todo.setAssigneeId(null);
    todo.setAssigneeName("人事行政");
    todo.setCreatedBy(staff.getId());
    oaTodoMapper.insert(todo);
  }

  private String birthdayTodoMarker(Long staffId, LocalDate date) {
    return "[BIRTHDAY_REMINDER:" + staffId + ":" + date + "]";
  }

  private void autoCompleteExpiredBirthdayTodos(LocalDate today) {
    LocalDateTime dayStart = today.atStartOfDay();
    List<OaTodo> todos = oaTodoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(OaTodo::getStatus, "OPEN")
        .lt(OaTodo::getDueTime, dayStart)
        .like(OaTodo::getContent, "[BIRTHDAY_REMINDER:")
        .last("LIMIT 500"));
    for (OaTodo item : todos) {
      item.setStatus("DONE");
      oaTodoMapper.updateById(item);
      OaTask task = oaTaskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
          .eq(OaTask::getIsDeleted, 0)
          .eq(item.getOrgId() != null, OaTask::getOrgId, item.getOrgId())
          .eq(OaTask::getSourceTodoId, item.getId())
          .last("LIMIT 1"));
      if (task != null && !"DONE".equalsIgnoreCase(task.getStatus())) {
        task.setStatus("DONE");
        oaTaskMapper.updateById(task);
      }
    }
  }
}
