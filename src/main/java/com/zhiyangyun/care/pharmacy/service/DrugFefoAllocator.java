package com.zhiyangyun.care.pharmacy.service;

import com.zhiyangyun.care.pharmacy.entity.DrugBatch;
import com.zhiyangyun.care.pharmacy.model.DrugDispensePlanItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FEFO（First Expired First Out，先过期先出）药品出库分配（纯逻辑）。
 *
 * <p>按批次效期从早到晚分配出库数量，跳过已过期与非可用批次；库存不足时抛出异常。
 * 分配结果只计算不落库，便于单测与复用。
 */
public final class DrugFefoAllocator {
  private DrugFefoAllocator() {}

  /**
   * @param batches 候选批次
   * @param requiredQty 需出库数量（&gt;0）
   * @param asOf 出库参考日期（早于此日期的批次视为过期，跳过）
   * @return 批次分配计划（按效期从早到晚）
   * @throws IllegalArgumentException 数量非法
   * @throws IllegalStateException 可用库存不足
   */
  public static List<DrugDispensePlanItem> allocate(List<DrugBatch> batches, int requiredQty,
      LocalDate asOf) {
    if (requiredQty <= 0) {
      throw new IllegalArgumentException("出库数量必须大于 0");
    }
    List<DrugBatch> usable = new ArrayList<>();
    int available = 0;
    for (DrugBatch b : batches == null ? List.<DrugBatch>of() : batches) {
      if (b.getQuantity() == null || b.getQuantity() <= 0) {
        continue;
      }
      if (b.getStatus() != null && !"ACTIVE".equalsIgnoreCase(b.getStatus())) {
        continue;
      }
      if (isExpired(b.getExpireDate(), asOf)) {
        continue;
      }
      usable.add(b);
      available += b.getQuantity();
    }
    if (available < requiredQty) {
      throw new IllegalStateException("可用库存不足：需 " + requiredQty + "，可用 " + available);
    }
    usable.sort(Comparator
        .comparing(DrugBatch::getExpireDate, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(DrugBatch::getId, Comparator.nullsLast(Comparator.naturalOrder())));

    List<DrugDispensePlanItem> plan = new ArrayList<>();
    int remaining = requiredQty;
    for (DrugBatch b : usable) {
      if (remaining <= 0) {
        break;
      }
      int take = Math.min(remaining, b.getQuantity());
      plan.add(new DrugDispensePlanItem(b.getId(), b.getBatchNo(), take));
      remaining -= take;
    }
    return plan;
  }

  private static boolean isExpired(LocalDate expireDate, LocalDate asOf) {
    if (expireDate == null) {
      return false;
    }
    LocalDate ref = asOf == null ? LocalDate.now() : asOf;
    return expireDate.isBefore(ref);
  }
}
