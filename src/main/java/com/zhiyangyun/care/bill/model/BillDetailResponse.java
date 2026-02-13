package com.zhiyangyun.care.bill.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BillDetailResponse {
  private Long billId;
  private Long elderId;
  private String elderName;
  private String billMonth;
  private BigDecimal totalAmount;
  private List<BillItemDetail> items = new ArrayList<>();
  private String message;
}
