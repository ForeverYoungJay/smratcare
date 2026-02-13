package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.ElderAccountLogResponse;
import com.zhiyangyun.care.finance.model.ElderAccountResponse;
import com.zhiyangyun.care.finance.model.ElderAccountUpdateRequest;
import java.math.BigDecimal;
import java.util.List;

public interface ElderAccountService {
  ElderAccountResponse getOrCreate(Long orgId, Long elderId, Long operatorId);

  IPage<ElderAccountResponse> page(Long orgId, long pageNo, long pageSize, String keyword);

  IPage<ElderAccountLogResponse> logPage(Long orgId, long pageNo, long pageSize, Long elderId,
      Long accountId, String keyword);

  ElderAccountResponse adjust(Long orgId, Long operatorId, ElderAccountAdjustRequest request);

  ElderAccountResponse updateAccount(Long orgId, Long operatorId, ElderAccountUpdateRequest request);

  void chargeForTask(Long orgId, Long elderId, Long taskDailyId, BigDecimal amount, String remark, Long operatorId);

  List<ElderAccountResponse> warnings(Long orgId);
}
