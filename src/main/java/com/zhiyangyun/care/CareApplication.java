package com.zhiyangyun.care;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({
    "com.zhiyangyun.care.auth.mapper",
    "com.zhiyangyun.care.elder.mapper",
    "com.zhiyangyun.care.store.mapper",
    "com.zhiyangyun.care.bill.mapper",
    "com.zhiyangyun.care.vital.mapper",
    "com.zhiyangyun.care.visit.mapper",
    "com.zhiyangyun.care.finance.mapper",
    "com.zhiyangyun.care.mapper",
    "com.zhiyangyun.care.asset.mapper",
    "com.zhiyangyun.care.audit.mapper",
    "com.zhiyangyun.care.crm.mapper",
    "com.zhiyangyun.care.hr.mapper",
    "com.zhiyangyun.care.survey.mapper",
    "com.zhiyangyun.care.standard.mapper",
    "com.zhiyangyun.care.life.mapper",
    "com.zhiyangyun.care.oa.mapper",
    "com.zhiyangyun.care.schedule.mapper",
    "com.zhiyangyun.care.report.mapper",
    "com.zhiyangyun.care.assessment.mapper",
    "com.zhiyangyun.care.health.mapper",
    "com.zhiyangyun.care.nursing.mapper",
    "com.zhiyangyun.care.baseconfig.mapper",
    "com.zhiyangyun.care.fire.mapper",
    "com.zhiyangyun.care.medicalcare.mapper",
    "com.zhiyangyun.care.logistics.mapper",
    "com.zhiyangyun.care.family.mapper",
    "com.zhiyangyun.care.smart.mapper",
    "com.zhiyangyun.care.operations.mapper",
    "com.zhiyangyun.care.cockpit.mapper",
    "com.zhiyangyun.care.compliance.mapper",
    "com.zhiyangyun.care.ltci.mapper",
    "com.zhiyangyun.care.govreport.mapper",
    "com.zhiyangyun.care.emr.mapper",
    "com.zhiyangyun.care.medorder.mapper",
    "com.zhiyangyun.care.pharmacy.mapper"
})
public class CareApplication {
  public static void main(String[] args) {
    SpringApplication.run(CareApplication.class, args);
  }
}
