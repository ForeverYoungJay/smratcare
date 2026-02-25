package com.zhiyangyun.care.life.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class BirthdayReminderResponse {
  private Long elderId;
  private String elderName;
  private LocalDate birthDate;
  private LocalDate nextBirthday;
  private Long daysUntil;
  private Integer ageOnNextBirthday;
  private String roomNo;
  private String bedNo;
}
