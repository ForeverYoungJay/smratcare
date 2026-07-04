package com.zhiyangyun.care.medorder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medorder.entity.MedicalOrder;
import com.zhiyangyun.care.medorder.entity.MedicalOrderExecution;
import com.zhiyangyun.care.medorder.mapper.MedicalOrderExecutionMapper;
import com.zhiyangyun.care.medorder.mapper.MedicalOrderMapper;
import com.zhiyangyun.care.medorder.model.MedicalOrderExecRequest;
import com.zhiyangyun.care.medorder.model.MedicalOrderRequest;
import com.zhiyangyun.care.medorder.service.MedicalOrderScheduleGenerator;
import com.zhiyangyun.care.medorder.service.MedicalOrderService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicalOrderServiceImpl implements MedicalOrderService {

  private final MedicalOrderMapper orderMapper;
  private final MedicalOrderExecutionMapper executionMapper;

  public MedicalOrderServiceImpl(MedicalOrderMapper orderMapper,
      MedicalOrderExecutionMapper executionMapper) {
    this.orderMapper = orderMapper;
    this.executionMapper = executionMapper;
  }

  @Override
  @Transactional
  public Long createOrder(MedicalOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    MedicalOrder order = new MedicalOrder();
    order.setOrgId(orgId);
    order.setElderId(request.getElderId());
    order.setEmrId(request.getEmrId());
    order.setOrderType(StringUtils.hasText(request.getOrderType()) ? request.getOrderType() : "LONG_TERM");
    order.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory() : "DRUG");
    order.setContent(request.getContent());
    order.setDrugId(request.getDrugId());
    order.setDosage(request.getDosage());
    order.setFrequency(request.getFrequency());
    order.setRoute(request.getRoute());
    order.setQuantityPerTime(request.getQuantityPerTime());
    order.setStartTime(request.getStartTime() != null ? request.getStartTime() : now);
    order.setStopTime(request.getStopTime());
    order.setStatus("ACTIVE");
    order.setPriority(request.getPriority());
    order.setRemark(request.getRemark());
    order.setDoctorId(AuthContext.getStaffId());
    order.setCreatedBy(AuthContext.getStaffId());
    orderMapper.insert(order);

    if ("TEMPORARY".equals(order.getOrderType())) {
      insertExecution(order, order.getStartTime());
    } else {
      generateExecutions(order.getId(), order.getStartTime().toLocalDate());
    }
    return order.getId();
  }

  @Override
  @Transactional
  public void stopOrder(Long orderId) {
    MedicalOrder order = load(orderId);
    order.setStatus("STOPPED");
    order.setStopTime(LocalDateTime.now());
    orderMapper.updateById(order);
  }

  @Override
  @Transactional
  public int generateExecutions(Long orderId, LocalDate date) {
    MedicalOrder order = orderMapper.selectById(orderId);
    if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
      throw new IllegalArgumentException("医嘱不存在: " + orderId);
    }
    if (!"ACTIVE".equals(order.getStatus())) {
      return 0;
    }
    List<LocalDateTime> times = MedicalOrderScheduleGenerator.timesForDate(order.getFrequency(), date);
    int count = 0;
    for (LocalDateTime planTime : times) {
      Long exists = executionMapper.selectCount(Wrappers.lambdaQuery(MedicalOrderExecution.class)
          .eq(MedicalOrderExecution::getIsDeleted, 0)
          .eq(MedicalOrderExecution::getOrderId, orderId)
          .eq(MedicalOrderExecution::getPlanTime, planTime));
      if (exists != null && exists > 0) {
        continue;
      }
      insertExecution(order, planTime);
      count++;
    }
    return count;
  }

  @Override
  @Transactional
  public int generateDailyExecutions() {
    LocalDate today = LocalDate.now();
    List<MedicalOrder> orders = orderMapper.selectList(Wrappers.lambdaQuery(MedicalOrder.class)
        .eq(MedicalOrder::getIsDeleted, 0)
        .eq(MedicalOrder::getStatus, "ACTIVE")
        .eq(MedicalOrder::getOrderType, "LONG_TERM"));
    int count = 0;
    for (MedicalOrder order : orders) {
      count += generateExecutions(order.getId(), today);
    }
    return count;
  }

  @Override
  @Transactional
  public MedicalOrderExecution signExecution(MedicalOrderExecRequest request) {
    MedicalOrderExecution exec = executionMapper.selectById(request.getExecutionId());
    if (exec == null || Integer.valueOf(1).equals(exec.getIsDeleted())) {
      throw new IllegalArgumentException("执行单不存在: " + request.getExecutionId());
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(exec.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的执行单");
    }
    exec.setStatus(request.getStatus());
    exec.setExecTime(LocalDateTime.now());
    exec.setExecutorId(AuthContext.getStaffId());
    exec.setResult(request.getResult());
    exec.setDispenseRecordId(request.getDispenseRecordId());
    if (StringUtils.hasText(request.getRemark())) {
      exec.setRemark(request.getRemark());
    }
    executionMapper.updateById(exec);
    return exec;
  }

  @Override
  public IPage<MedicalOrder> pageOrders(int pageNo, int pageSize, Long elderId, String status, String category) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MedicalOrder.class)
        .eq(MedicalOrder::getIsDeleted, 0)
        .eq(orgId != null, MedicalOrder::getOrgId, orgId)
        .eq(elderId != null, MedicalOrder::getElderId, elderId)
        .eq(StringUtils.hasText(status), MedicalOrder::getStatus, status)
        .eq(StringUtils.hasText(category), MedicalOrder::getCategory, category)
        .orderByDesc(MedicalOrder::getId);
    return orderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public IPage<MedicalOrderExecution> pageExecutions(int pageNo, int pageSize, Long orderId, Long elderId, String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MedicalOrderExecution.class)
        .eq(MedicalOrderExecution::getIsDeleted, 0)
        .eq(orgId != null, MedicalOrderExecution::getOrgId, orgId)
        .eq(orderId != null, MedicalOrderExecution::getOrderId, orderId)
        .eq(elderId != null, MedicalOrderExecution::getElderId, elderId)
        .eq(StringUtils.hasText(status), MedicalOrderExecution::getStatus, status)
        .orderByAsc(MedicalOrderExecution::getPlanTime);
    return executionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private void insertExecution(MedicalOrder order, LocalDateTime planTime) {
    MedicalOrderExecution exec = new MedicalOrderExecution();
    exec.setOrgId(order.getOrgId());
    exec.setOrderId(order.getId());
    exec.setElderId(order.getElderId());
    exec.setPlanTime(planTime);
    exec.setStatus("PENDING");
    exec.setCreatedBy(AuthContext.getStaffId());
    executionMapper.insert(exec);
  }

  private MedicalOrder load(Long orderId) {
    MedicalOrder order = orderMapper.selectById(orderId);
    if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
      throw new IllegalArgumentException("医嘱不存在: " + orderId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(order.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的医嘱");
    }
    return order;
  }
}
