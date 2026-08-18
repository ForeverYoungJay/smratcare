package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.entity.FinanceDepositStandard;
import com.zhiyangyun.care.finance.model.DepositAccountView;
import com.zhiyangyun.care.finance.model.DepositStandardRequest;
import com.zhiyangyun.care.finance.model.DepositSummary;
import com.zhiyangyun.care.finance.model.DepositTransactionRequest;
import com.zhiyangyun.care.finance.model.DepositTransactionView;
import java.math.BigDecimal;
import java.util.List;

/** 押金管理：标准配置、缴纳/扣款/退还登记、账户与流水查询。 */
public interface DepositService {

  List<FinanceDepositStandard> standardList();

  FinanceDepositStandard saveStandard(DepositStandardRequest request, Long operatorStaffId);

  void deleteStandard(Long standardId);

  /** 按护理等级解析应缴押金标准；无匹配时回落到通用标准，都没有则为 0。 */
  BigDecimal resolveStandardAmount(String careLevel);

  IPage<DepositAccountView> page(long pageNo, long pageSize, String status, String keyword);

  DepositAccountView detail(Long elderId);

  List<DepositTransactionView> transactions(Long elderId);

  DepositAccountView registerPay(DepositTransactionRequest request, Long operatorStaffId);

  DepositAccountView registerDeduct(DepositTransactionRequest request, Long operatorStaffId);

  DepositAccountView registerRefund(DepositTransactionRequest request, Long operatorStaffId);

  /** 按当前护理等级标准刷新某长者的应缴金额。 */
  DepositAccountView refreshStandard(Long elderId);

  DepositSummary summary();
}
