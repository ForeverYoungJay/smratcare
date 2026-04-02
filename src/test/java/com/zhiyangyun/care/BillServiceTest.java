package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.bill.entity.BillingCareLevelFee;
import com.zhiyangyun.care.bill.entity.BillingConfigEntry;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.bill.service.BillingConfigService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BillServiceTest {
  @Autowired
  private BillService billService;

  @Autowired
  private BillingConfigService billingConfigService;

  @Autowired
  private ElderProfileMapper elderProfileMapper;

  @Autowired
  private ElderAdmissionMapper elderAdmissionMapper;

  @Autowired
  private ElderDischargeApplyMapper elderDischargeApplyMapper;

  @Autowired
  private CrmContractMapper crmContractMapper;

  @Test
  void ensure_monthly_bill_should_prorate_by_contract_admission_and_discharge_window() {
    ElderProfile elder = new ElderProfile();
    elder.setOrgId(1L);
    elder.setFullName("分摊长者");
    elder.setCareLevel("L2");
    elder.setStatus(1);
    elderProfileMapper.insert(elder);

    String contractNo = "CT-" + System.nanoTime();
    ElderAdmission admission = new ElderAdmission();
    admission.setOrgId(1L);
    admission.setElderId(elder.getId());
    admission.setAdmissionDate(LocalDate.of(2026, 2, 10));
    admission.setContractNo(contractNo);
    elderAdmissionMapper.insert(admission);

    ElderDischargeApply dischargeApply = new ElderDischargeApply();
    dischargeApply.setOrgId(1L);
    dischargeApply.setElderId(elder.getId());
    dischargeApply.setElderName(elder.getFullName());
    dischargeApply.setApplyDate(LocalDate.of(2026, 2, 18));
    dischargeApply.setPlannedDischargeDate(LocalDate.of(2026, 2, 20));
    dischargeApply.setStatus("APPROVED");
    elderDischargeApplyMapper.insert(dischargeApply);

    CrmContract contract = new CrmContract();
    contract.setOrgId(1L);
    contract.setElderId(elder.getId());
    contract.setContractNo(contractNo);
    contract.setStatus("SIGNED");
    contract.setEffectiveFrom(LocalDate.of(2026, 2, 15));
    crmContractMapper.insert(contract);

    upsertConfig("BED_FEE_MONTHLY", BigDecimal.valueOf(3000), "2026-02");
    upsertConfig("MEAL_FEE_PER_DAY", BigDecimal.valueOf(20), "2026-02");
    upsertCareFee("L2", BigDecimal.valueOf(1500), "2026-02");

    BillDetailResponse response = billService.ensureMonthlyBillForElder(
        1L,
        elder.getId(),
        "2026-02",
        contract.getId(),
        contract.getContractNo());

    Map<String, BigDecimal> itemMap = response.getItems().stream()
        .collect(Collectors.toMap(item -> item.getItemType().toUpperCase(), item -> item.getAmount(), (a, b) -> a));

    assertEquals(0, BigDecimal.valueOf(642.86).compareTo(itemMap.getOrDefault("BED", BigDecimal.ZERO)));
    assertEquals(0, BigDecimal.valueOf(321.43).compareTo(itemMap.getOrDefault("NURSING", BigDecimal.ZERO)));
    assertEquals(0, BigDecimal.valueOf(120).compareTo(itemMap.getOrDefault("MEAL", BigDecimal.ZERO)));
    assertEquals(0, BigDecimal.valueOf(1084.29).compareTo(response.getTotalAmount()));
  }

  private void upsertConfig(String key, BigDecimal amount, String effectiveMonth) {
    BillingConfigEntry entry = new BillingConfigEntry();
    entry.setOrgId(1L);
    entry.setConfigKey(key);
    entry.setConfigValue(amount);
    entry.setEffectiveMonth(effectiveMonth);
    entry.setStatus(1);
    billingConfigService.upsertConfig(entry);
  }

  private void upsertCareFee(String careLevel, BigDecimal amount, String effectiveMonth) {
    BillingCareLevelFee fee = new BillingCareLevelFee();
    fee.setOrgId(1L);
    fee.setCareLevel(careLevel);
    fee.setFeeMonthly(amount);
    fee.setEffectiveMonth(effectiveMonth);
    fee.setStatus(1);
    billingConfigService.upsertCareLevelFee(fee);
  }
}
