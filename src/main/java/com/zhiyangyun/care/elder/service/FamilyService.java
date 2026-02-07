package com.zhiyangyun.care.elder.service;

import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyRegisterRequest;

public interface FamilyService {
  FamilyUser register(FamilyRegisterRequest request);

  ElderFamily bindElder(FamilyBindRequest request);
}
