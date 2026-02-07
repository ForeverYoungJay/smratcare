package com.zhiyangyun.care.bill.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillItemDetail {
  private String itemType;
  private String itemName;
  private BigDecimal amount;
  private String basis;
}
