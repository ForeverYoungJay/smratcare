package com.zhiyangyun.care.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.zhiyangyun.care.compliance.util.DataMaskingUtil;
import org.junit.jupiter.api.Test;

class DataMaskingUtilTest {

  @Test
  void maskPhone_should_keep_first3_last4() {
    assertEquals("138****1234", DataMaskingUtil.maskPhone("13800001234"));
    assertNull(DataMaskingUtil.maskPhone(null));
  }

  @Test
  void maskPhone_short_value_should_mask_middle() {
    assertEquals("1**4", DataMaskingUtil.maskPhone("1234"));
  }

  @Test
  void maskIdCard_should_keep_first4_last4() {
    assertEquals("3301********123X", DataMaskingUtil.maskIdCard("33010219900101123X"));
    assertNull(DataMaskingUtil.maskIdCard(null));
  }

  @Test
  void maskIdCard_short_value_should_mask_middle() {
    assertEquals("1******8", DataMaskingUtil.maskIdCard("12345678"));
  }

  @Test
  void maskName_should_keep_family_name() {
    assertEquals("张*", DataMaskingUtil.maskName("张三"));
    assertEquals("欧**", DataMaskingUtil.maskName("欧阳锋"));
    assertEquals("张", DataMaskingUtil.maskName("张"));
    assertNull(DataMaskingUtil.maskName(null));
  }

  @Test
  void maskAddress_should_keep_first6() {
    assertEquals("浙江省杭州市****", DataMaskingUtil.maskAddress("浙江省杭州市西湖区某某路1号"));
    assertEquals("杭州", DataMaskingUtil.maskAddress("杭州"));
  }

  @Test
  void maskBankCard_should_keep_last4() {
    assertEquals("**** 3456", DataMaskingUtil.maskBankCard("6222 0012 3456 3456".replace(" ", "")));
  }

  @Test
  void maskMedicalSummary_should_keep_first2() {
    assertEquals("高血****", DataMaskingUtil.maskMedicalSummary("高血压二级，伴糖尿病"));
    assertEquals("**", DataMaskingUtil.maskMedicalSummary("咳嗽"));
    assertNull(DataMaskingUtil.maskMedicalSummary(null));
  }
}
