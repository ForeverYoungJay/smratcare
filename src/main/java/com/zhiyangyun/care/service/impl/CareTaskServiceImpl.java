package com.zhiyangyun.care.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.service.CareTaskService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareTaskServiceImpl implements CareTaskService {
  private final CareTaskDailyMapper dailyMapper;
  private final CareTaskTemplateMapper templateMapper;
  private final CareTaskExecuteLogMapper logMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public CareTaskServiceImpl(
      CareTaskDailyMapper dailyMapper,
      CareTaskTemplateMapper templateMapper,
      CareTaskExecuteLogMapper logMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper) {
    this.dailyMapper = dailyMapper;
    this.templateMapper = templateMapper;
    this.logMapper = logMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  public List<CareTaskTodayItem> getTodayTasks(Long staffId, LocalDate date) {
    LambdaQueryWrapper<CareTaskDaily> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskDaily::getTaskDate, date);
    if (staffId != null) {
      wrapper.and(w -> w.eq(CareTaskDaily::getAssignedStaffId, staffId)
          .or().isNull(CareTaskDaily::getAssignedStaffId));
    }
    List<CareTaskDaily> tasks = dailyMapper.selectList(wrapper);

    Map<Long, ElderProfile> elderMap = new HashMap<>();
    Map<Long, Bed> bedMap = new HashMap<>();
    Map<Long, Room> roomMap = new HashMap<>();
    Map<Long, CareTaskTemplate> templateMap = new HashMap<>();

    for (CareTaskDaily task : tasks) {
      if (task.getElderId() != null && !elderMap.containsKey(task.getElderId())) {
        elderMap.put(task.getElderId(), elderMapper.selectById(task.getElderId()));
      }
      if (task.getBedId() != null && !bedMap.containsKey(task.getBedId())) {
        bedMap.put(task.getBedId(), bedMapper.selectById(task.getBedId()));
      }
      if (task.getTemplateId() != null && !templateMap.containsKey(task.getTemplateId())) {
        templateMap.put(task.getTemplateId(), templateMapper.selectById(task.getTemplateId()));
      }
    }

    for (Bed bed : bedMap.values()) {
      if (bed != null && bed.getRoomId() != null && !roomMap.containsKey(bed.getRoomId())) {
        roomMap.put(bed.getRoomId(), roomMapper.selectById(bed.getRoomId()));
      }
    }

    List<CareTaskTodayItem> result = new ArrayList<>();
    for (CareTaskDaily task : tasks) {
      CareTaskTodayItem item = new CareTaskTodayItem();
      item.setTaskDailyId(task.getId());
      item.setElderId(task.getElderId());
      ElderProfile elder = elderMap.get(task.getElderId());
      item.setElderName(elder != null ? elder.getFullName() : null);
      item.setBedId(task.getBedId());
      Bed bed = bedMap.get(task.getBedId());
      Room room = bed != null ? roomMap.get(bed.getRoomId()) : null;
      item.setRoomNo(room != null ? room.getRoomNo() : null);
      item.setStaffId(task.getAssignedStaffId());
      CareTaskTemplate template = templateMap.get(task.getTemplateId());
      item.setTaskName(template != null ? template.getTaskName() : null);
      item.setPlanTime(task.getPlanTime());
      item.setStatus(task.getStatus());
      result.add(item);
    }

    return result;
  }

  @Override
  @Transactional
  public ExecuteTaskResponse executeTask(ExecuteTaskRequest request) {
    ExecuteTaskResponse response = new ExecuteTaskResponse();
    response.setTaskDailyId(request.getTaskDailyId());

    CareTaskDaily task = dailyMapper.selectById(request.getTaskDailyId());
    if (task == null) {
      response.setStatus(TaskStatus.EXCEPTION.name());
      response.setAbnormal(true);
      response.setSuspicious(false);
      response.setMessage("Task not found");
      return response;
    }

    if (TaskStatus.DONE.name().equals(task.getStatus())) {
      response.setStatus(TaskStatus.DONE.name());
      response.setAbnormal(false);
      response.setSuspicious(false);
      response.setMessage("Task already completed");
      return response;
    }

    ElderProfile elder = elderMapper.selectById(task.getElderId());
    Bed elderBed = elder != null ? bedMapper.selectById(elder.getBedId()) : null;
    if (elderBed == null || elderBed.getBedQrCode() == null
        || !elderBed.getBedQrCode().equals(request.getBedQrCode())) {
      task.setStatus(TaskStatus.EXCEPTION.name());
      dailyMapper.updateById(task);

      CareTaskExecuteLog log = new CareTaskExecuteLog();
      log.setOrgId(task.getOrgId());
      log.setTaskDailyId(task.getId());
      log.setElderId(task.getElderId());
      log.setBedId(task.getBedId());
      log.setStaffId(request.getStaffId());
      log.setExecuteTime(LocalDateTime.now());
      log.setBedQrCode(request.getBedQrCode());
      log.setResultStatus(0);
      log.setSuspiciousFlag(0);
      log.setRemark(request.getRemark());
      logMapper.insert(log);

      response.setStatus(TaskStatus.EXCEPTION.name());
      response.setAbnormal(true);
      response.setSuspicious(false);
      response.setMessage("Bed QR code mismatch");
      return response;
    }

    boolean suspicious = isSuspicious(request.getStaffId(), elderBed.getRoomId());

    task.setStatus(TaskStatus.DONE.name());
    task.setAssignedStaffId(request.getStaffId());
    dailyMapper.updateById(task);

    CareTaskExecuteLog log = new CareTaskExecuteLog();
    log.setOrgId(task.getOrgId());
    log.setTaskDailyId(task.getId());
    log.setElderId(task.getElderId());
    log.setBedId(task.getBedId());
    log.setStaffId(request.getStaffId());
    log.setExecuteTime(LocalDateTime.now());
    log.setBedQrCode(request.getBedQrCode());
    log.setResultStatus(1);
    log.setSuspiciousFlag(suspicious ? 1 : 0);
    log.setRemark(request.getRemark());
    logMapper.insert(log);

    response.setStatus(TaskStatus.DONE.name());
    response.setAbnormal(false);
    response.setSuspicious(suspicious);
    response.setMessage(suspicious ? "Executed with suspicious flag" : "Executed successfully");
    return response;
  }

  private boolean isSuspicious(Long staffId, Long currentRoomId) {
    LambdaQueryWrapper<CareTaskExecuteLog> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskExecuteLog::getStaffId, staffId)
        .orderByDesc(CareTaskExecuteLog::getExecuteTime)
        .last("LIMIT 1");
    CareTaskExecuteLog lastLog = logMapper.selectOne(wrapper);
    if (lastLog == null) {
      return false;
    }

    Bed lastBed = bedMapper.selectById(lastLog.getBedId());
    if (lastBed == null || lastBed.getRoomId() == null || currentRoomId == null) {
      return false;
    }

    if (lastBed.getRoomId().equals(currentRoomId)) {
      return false;
    }

    Duration duration = Duration.between(lastLog.getExecuteTime(), LocalDateTime.now());
    return duration.toMinutes() < 3;
  }

  @Override
  @Transactional
  public int generateDailyTasks(LocalDate date) {
    return generateDailyTasks(date, false);
  }

  @Override
  @Transactional
  public int generateDailyTasks(LocalDate date, boolean force) {
    Long existingCount = dailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getTaskDate, date));
    if (!force && existingCount != null && existingCount > 0) {
      return 0;
    }

    List<ElderProfile> elders = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getStatus, 1));
    List<CareTaskTemplate> templates = templateMapper.selectList(Wrappers.lambdaQuery(CareTaskTemplate.class)
        .eq(CareTaskTemplate::getEnabled, 1));

    int count = 0;
    for (ElderProfile elder : elders) {
      for (CareTaskTemplate template : templates) {
        if (template.getCareLevelRequired() != null && elder.getCareLevel() != null
            && !template.getCareLevelRequired().equals(elder.getCareLevel())) {
          continue;
        }

        int frequency = template.getFrequencyPerDay() == null ? 1 : template.getFrequencyPerDay();
        for (int i = 0; i < frequency; i++) {
          CareTaskDaily task = new CareTaskDaily();
          task.setOrgId(elder.getOrgId());
          task.setElderId(elder.getId());
          task.setBedId(elder.getBedId());
          task.setTemplateId(template.getId());
          task.setTaskDate(date);
          task.setPlanTime(date.atStartOfDay().plusHours(8).plusMinutes(i * 60));
          task.setStatus(TaskStatus.PENDING.name());
          dailyMapper.insert(task);
          count++;
        }
      }
    }

    return count;
  }
}
