package com.zhiyangyun.care.family.service;

import com.zhiyangyun.care.auth.model.FamilySmsCodeSendResponse;
import com.zhiyangyun.care.auth.model.FamilySmsCodeVerifyResponse;

public interface FamilySmsCodeService {
  FamilySmsCodeSendResponse sendCode(Long orgId, Long familyUserId, String phone, String scene);

  default FamilySmsCodeSendResponse sendCode(Long orgId, Long familyUserId, String phone, String scene, String clientIp) {
    return sendCode(orgId, familyUserId, phone, scene);
  }

  FamilySmsCodeSendResponse sendCodeForFamilyUser(Long orgId, Long familyUserId, String scene);

  default FamilySmsCodeSendResponse sendCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String clientIp) {
    return sendCodeForFamilyUser(orgId, familyUserId, scene);
  }

  FamilySmsCodeVerifyResponse verifyCode(Long orgId, String phone, String scene, String verifyCode, boolean consume);

  default FamilySmsCodeVerifyResponse verifyCode(Long orgId, String phone, String scene, String verifyCode, boolean consume,
      String clientIp) {
    return verifyCode(orgId, phone, scene, verifyCode, consume);
  }

  FamilySmsCodeVerifyResponse verifyCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String verifyCode,
      boolean consume);

  default FamilySmsCodeVerifyResponse verifyCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String verifyCode,
      boolean consume, String clientIp) {
    return verifyCodeForFamilyUser(orgId, familyUserId, scene, verifyCode, consume);
  }
}
