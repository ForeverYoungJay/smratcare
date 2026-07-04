package com.zhiyangyun.care.medorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.medorder.entity.MedicalOrder;
import com.zhiyangyun.care.medorder.entity.MedicalOrderExecution;
import com.zhiyangyun.care.medorder.model.MedicalOrderExecRequest;
import com.zhiyangyun.care.medorder.model.MedicalOrderRequest;
import java.time.LocalDate;

/** 医嘱业务：开立、停嘱、生成执行单、执行签核。 */
public interface MedicalOrderService {

  /** 开立医嘱并生成当日执行计划，返回医嘱 id。 */
  Long createOrder(MedicalOrderRequest request);

  /** 停嘱。 */
  void stopOrder(Long orderId);

  /** 为指定医嘱生成某日执行单（幂等），返回新增条数。 */
  int generateExecutions(Long orderId, LocalDate date);

  /** 为所有生效长期医嘱生成当日执行单（供定时任务调用），返回新增条数。 */
  int generateDailyExecutions();

  /** 执行签核（DONE/SKIPPED）。 */
  MedicalOrderExecution signExecution(MedicalOrderExecRequest request);

  IPage<MedicalOrder> pageOrders(int pageNo, int pageSize, Long elderId, String status, String category);

  IPage<MedicalOrderExecution> pageExecutions(int pageNo, int pageSize, Long orderId, Long elderId, String status);
}
