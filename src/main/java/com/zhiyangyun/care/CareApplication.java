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
    "com.zhiyangyun.care.mapper"
})
public class CareApplication {
  public static void main(String[] args) {
    SpringApplication.run(CareApplication.class, args);
  }
}
