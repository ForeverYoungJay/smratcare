package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.entity.FinanceElectricityPrice;
import com.zhiyangyun.care.finance.model.ElectricityMonthSummary;
import com.zhiyangyun.care.finance.model.ElectricityPriceRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingView;
import java.math.BigDecimal;
import java.util.List;

/** 电费管理：电价配置、按房间抄表登记、自动算电费、缴费状态。 */
public interface ElectricityFeeService {

  List<FinanceElectricityPrice> priceList();

  /** 指定账期生效的电价；未配置返回 null。 */
  BigDecimal resolveUnitPrice(String billMonth);

  FinanceElectricityPrice savePrice(ElectricityPriceRequest request, Long operatorStaffId);

  IPage<ElectricityReadingView> page(
      long pageNo,
      long pageSize,
      String billMonth,
      String building,
      String roomNo,
      String payStatus);

  /** 登记/更新某房间某账期的读数，按 (账期, 房间) 幂等 upsert。 */
  ElectricityReadingView saveReading(ElectricityReadingRequest request, Long operatorStaffId);

  /** 某房间上期读数建议值：取上一账期该房间的本期读数。 */
  BigDecimal suggestPreviousReading(String billMonth, Long roomId);

  ElectricityReadingView updatePayStatus(Long readingId, boolean paid, String payRemark, Long operatorStaffId);

  ElectricityMonthSummary summary(String billMonth);
}
