package com.zhiyangyun.care.pharmacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.pharmacy.entity.DrugBatch;
import com.zhiyangyun.care.pharmacy.model.DrugDispensePlanItem;
import com.zhiyangyun.care.pharmacy.service.DrugFefoAllocator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** FEFO 出库分配单测：先过期先出/跨批次/跳过过期与非可用/库存不足。 */
class DrugFefoAllocatorTest {

  private DrugBatch batch(long id, String no, int qty, LocalDate expire, String status) {
    DrugBatch b = new DrugBatch();
    b.setId(id);
    b.setBatchNo(no);
    b.setQuantity(qty);
    b.setExpireDate(expire);
    b.setStatus(status);
    return b;
  }

  private final LocalDate today = LocalDate.of(2026, 7, 1);

  @Test
  void allocatesEarliestExpiryFirstAcrossBatches() {
    var batches = List.of(
        batch(1, "B-LATE", 100, today.plusMonths(6), "ACTIVE"),
        batch(2, "B-EARLY", 30, today.plusMonths(1), "ACTIVE"));
    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, 50, today);
    assertEquals(2, plan.size());
    assertEquals("B-EARLY", plan.get(0).getBatchNo());
    assertEquals(30, plan.get(0).getQuantity());
    assertEquals("B-LATE", plan.get(1).getBatchNo());
    assertEquals(20, plan.get(1).getQuantity());
  }

  @Test
  void singleBatchExactlyCovers() {
    var batches = List.of(batch(1, "B1", 40, today.plusMonths(2), "ACTIVE"));
    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, 40, today);
    assertEquals(1, plan.size());
    assertEquals(40, plan.get(0).getQuantity());
  }

  @Test
  void skipsExpiredBatch() {
    var batches = List.of(
        batch(1, "EXPIRED", 100, today.minusDays(1), "ACTIVE"),
        batch(2, "GOOD", 20, today.plusMonths(3), "ACTIVE"));
    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, 20, today);
    assertEquals(1, plan.size());
    assertEquals("GOOD", plan.get(0).getBatchNo());
  }

  @Test
  void skipsNonActiveAndZeroQty() {
    var batches = List.of(
        batch(1, "FROZEN", 100, today.plusMonths(1), "FROZEN"),
        batch(2, "EMPTY", 0, today.plusMonths(1), "ACTIVE"),
        batch(3, "OK", 15, today.plusMonths(2), "ACTIVE"));
    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, 15, today);
    assertEquals(1, plan.size());
    assertEquals("OK", plan.get(0).getBatchNo());
  }

  @Test
  void insufficientStockThrows() {
    var batches = List.of(batch(1, "B1", 10, today.plusMonths(1), "ACTIVE"));
    assertThrows(IllegalStateException.class, () -> DrugFefoAllocator.allocate(batches, 50, today));
  }

  @Test
  void invalidQtyThrows() {
    var batches = List.of(batch(1, "B1", 10, today.plusMonths(1), "ACTIVE"));
    assertThrows(IllegalArgumentException.class, () -> DrugFefoAllocator.allocate(batches, 0, today));
  }

  @Test
  void nullExpirySortsLast() {
    var batches = List.of(
        batch(1, "NOEXP", 100, null, "ACTIVE"),
        batch(2, "DATED", 5, today.plusMonths(1), "ACTIVE"));
    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, 6, today);
    assertEquals("DATED", plan.get(0).getBatchNo());
    assertEquals(5, plan.get(0).getQuantity());
    assertEquals("NOEXP", plan.get(1).getBatchNo());
    assertEquals(1, plan.get(1).getQuantity());
  }
}
