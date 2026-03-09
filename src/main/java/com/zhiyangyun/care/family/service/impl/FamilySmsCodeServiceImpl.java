package com.zhiyangyun.care.family.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.FamilySmsCodeSendResponse;
import com.zhiyangyun.care.auth.model.FamilySmsCodeVerifyResponse;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.entity.FamilySmsCodeLog;
import com.zhiyangyun.care.family.mapper.FamilySmsCodeLogMapper;
import com.zhiyangyun.care.family.service.FamilySmsCodeService;
import com.zhiyangyun.care.family.sms.FamilySmsSendCommand;
import com.zhiyangyun.care.family.sms.FamilySmsSendResult;
import com.zhiyangyun.care.family.sms.FamilySmsSender;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SecureRandom;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilySmsCodeServiceImpl implements FamilySmsCodeService {
  private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int VERIFY_IP_SAMPLE_LIMIT = 120;

  private static final String STATUS_SENT = "SENT";
  private static final String STATUS_VERIFIED = "VERIFIED";
  private static final String STATUS_USED = "USED";
  private static final String STATUS_EXPIRED = "EXPIRED";
  private static final String STATUS_FAILED = "FAILED";
  private static final String SCENE_LOGIN = "LOGIN";
  private static final String SCENE_SECURITY = "SECURITY";

  private final FamilySmsCodeLogMapper familySmsCodeLogMapper;
  private final FamilyUserMapper familyUserMapper;
  private final FamilyPortalProperties familyPortalProperties;
  private final Map<String, FamilySmsSender> senderMap = new HashMap<>();

  public FamilySmsCodeServiceImpl(FamilySmsCodeLogMapper familySmsCodeLogMapper,
      FamilyUserMapper familyUserMapper,
      FamilyPortalProperties familyPortalProperties,
      List<FamilySmsSender> senders) {
    this.familySmsCodeLogMapper = familySmsCodeLogMapper;
    this.familyUserMapper = familyUserMapper;
    this.familyPortalProperties = familyPortalProperties;
    if (senders != null) {
      for (FamilySmsSender sender : senders) {
        if (sender == null || sender.providerName() == null) {
          continue;
        }
        senderMap.put(sender.providerName().trim().toLowerCase(Locale.ROOT), sender);
      }
    }
  }

  @Override
  @Transactional
  public FamilySmsCodeSendResponse sendCode(Long orgId, Long familyUserId, String phone, String scene) {
    return sendCode(orgId, familyUserId, phone, scene, null);
  }

  @Override
  @Transactional
  public FamilySmsCodeSendResponse sendCode(Long orgId, Long familyUserId, String phone, String scene, String clientIp) {
    FamilyPortalProperties.SmsCode smsCode = ensureSmsCodeEnabled();
    Long normalizedOrgId = requireOrgId(orgId);
    String normalizedPhone = normalizePhone(phone);
    String normalizedScene = normalizeScene(scene, SCENE_LOGIN);
    String normalizedClientIp = normalizeClientIp(clientIp);
    enforceSendLimit(normalizedOrgId, normalizedPhone, normalizedScene, smsCode);
    enforceIpSendLimit(normalizedOrgId, normalizedClientIp, smsCode);

    String code = generateCode(smsCode.getCodeLength());
    String bizNo = buildBizNo(normalizedScene);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusSeconds(Math.max(smsCode.getExpireSeconds(), 60));

    FamilySmsSendCommand command = new FamilySmsSendCommand();
    command.setOrgId(normalizedOrgId);
    command.setFamilyUserId(familyUserId);
    command.setPhone(normalizedPhone);
    command.setScene(normalizedScene);
    command.setCode(code);
    FamilySmsSender sender = resolveSender(smsCode.getProvider());
    FamilySmsSendResult sendResult = sender.send(command);

    FamilySmsCodeLog log = new FamilySmsCodeLog();
    log.setTenantId(normalizedOrgId);
    log.setOrgId(normalizedOrgId);
    log.setFamilyUserId(familyUserId);
    log.setPhone(normalizedPhone);
    log.setScene(normalizedScene);
    log.setClientIp(normalizedClientIp);
    log.setBizNo(bizNo);
    log.setCodeHash(hashVerifyCode(normalizedPhone, normalizedScene, code, smsCode.getCodeSalt()));
    log.setStatus(sendResult.isSuccess() ? STATUS_SENT : STATUS_FAILED);
    log.setProvider(defaultText(sendResult.getProvider(), sender.providerName().toUpperCase(Locale.ROOT)));
    log.setProviderMessageId(defaultText(sendResult.getMessageId(), null));
    log.setProviderRequest(defaultText(sendResult.getRequestPayload(), null));
    log.setProviderResponse(defaultText(sendResult.getResponsePayload(), null));
    log.setVerifyAttempts(0);
    log.setMaxAttempts(Math.max(smsCode.getMaxAttempts(), 1));
    log.setExpiresAt(expiresAt);
    log.setLastError(defaultText(sendResult.getErrorMessage(), null));
    log.setCreatedBy(familyUserId);
    familySmsCodeLogMapper.insert(log);

    if (!sendResult.isSuccess()) {
      throw new IllegalStateException(defaultText(sendResult.getErrorMessage(), "短信发送失败，请稍后重试"));
    }

    FamilySmsCodeSendResponse response = new FamilySmsCodeSendResponse();
    response.setBizNo(bizNo);
    response.setExpireSeconds(Math.max(smsCode.getExpireSeconds(), 60));
    response.setRetryAfterSeconds(Math.max(smsCode.getCooldownSeconds(), 1));
    response.setProvider(defaultText(sendResult.getProvider(), sender.providerName()));
    response.setMessage("验证码发送成功");
    if (smsCode.isDebugReturnCode()) {
      response.setDebugCode(code);
    }
    return response;
  }

  @Override
  public FamilySmsCodeSendResponse sendCodeForFamilyUser(Long orgId, Long familyUserId, String scene) {
    return sendCodeForFamilyUser(orgId, familyUserId, scene, null);
  }

  @Override
  public FamilySmsCodeSendResponse sendCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String clientIp) {
    FamilyUser familyUser = findFamilyUser(orgId, familyUserId);
    String phone = familyUser == null ? null : familyUser.getPhone();
    return sendCode(orgId, familyUserId, phone, normalizeScene(scene, SCENE_SECURITY), clientIp);
  }

  @Override
  @Transactional
  public FamilySmsCodeVerifyResponse verifyCode(Long orgId, String phone, String scene, String verifyCode, boolean consume) {
    return verifyCode(orgId, phone, scene, verifyCode, consume, null);
  }

  @Override
  @Transactional
  public FamilySmsCodeVerifyResponse verifyCode(Long orgId, String phone, String scene, String verifyCode, boolean consume,
      String clientIp) {
    FamilyPortalProperties.SmsCode smsCode = ensureSmsCodeEnabled();
    Long normalizedOrgId = requireOrgId(orgId);
    String normalizedPhone = normalizePhone(phone);
    String normalizedScene = normalizeScene(scene, SCENE_LOGIN);
    String normalizedCode = normalizeCode(verifyCode);
    String normalizedClientIp = normalizeClientIp(clientIp);
    enforceIpVerifyLimit(normalizedOrgId, normalizedClientIp, smsCode);

    FamilySmsCodeLog record = familySmsCodeLogMapper.selectOne(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, normalizedOrgId)
            .eq(FamilySmsCodeLog::getPhone, normalizedPhone)
            .eq(FamilySmsCodeLog::getScene, normalizedScene)
            .in(FamilySmsCodeLog::getStatus, STATUS_SENT, STATUS_VERIFIED)
            .orderByDesc(FamilySmsCodeLog::getCreateTime)
            .last("LIMIT 1"));

    if (record == null) {
      return failVerify("请先获取验证码", smsCode.getMaxAttempts());
    }

    LocalDateTime now = LocalDateTime.now();
    if (hasText(normalizedClientIp)) {
      record.setClientIp(normalizedClientIp);
    }
    if (record.getExpiresAt() == null || !record.getExpiresAt().isAfter(now)) {
      record.setStatus(STATUS_EXPIRED);
      record.setLastError("验证码已过期");
      familySmsCodeLogMapper.updateById(record);
      return failVerify("验证码已过期，请重新获取", 0);
    }

    int attempts = record.getVerifyAttempts() == null ? 0 : record.getVerifyAttempts();
    int maxAttempts = record.getMaxAttempts() == null || record.getMaxAttempts() <= 0
        ? Math.max(smsCode.getMaxAttempts(), 1) : record.getMaxAttempts();
    if (attempts >= maxAttempts) {
      record.setStatus(STATUS_FAILED);
      record.setLastError("校验失败次数超限");
      familySmsCodeLogMapper.updateById(record);
      return failVerify("验证码错误次数过多，请重新获取", 0);
    }

    String expectedHash = hashVerifyCode(normalizedPhone, normalizedScene, normalizedCode, smsCode.getCodeSalt());
    if (!Objects.equals(expectedHash, record.getCodeHash())) {
      int nextAttempts = attempts + 1;
      int remaining = Math.max(maxAttempts - nextAttempts, 0);
      record.setVerifyAttempts(nextAttempts);
      record.setLastError("验证码错误");
      if (nextAttempts >= maxAttempts) {
        record.setStatus(STATUS_FAILED);
      }
      familySmsCodeLogMapper.updateById(record);
      return failVerify("验证码错误，请重试", remaining);
    }

    record.setVerifiedAt(now);
    record.setLastError(null);
    if (consume) {
      record.setStatus(STATUS_USED);
      record.setUsedAt(now);
    } else {
      record.setStatus(STATUS_VERIFIED);
    }
    familySmsCodeLogMapper.updateById(record);

    FamilySmsCodeVerifyResponse response = new FamilySmsCodeVerifyResponse();
    response.setPassed(true);
    response.setRemainingAttempts(Math.max(maxAttempts - attempts, 0));
    response.setMessage("验证码校验通过");
    return response;
  }

  @Override
  public FamilySmsCodeVerifyResponse verifyCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String verifyCode,
      boolean consume) {
    return verifyCodeForFamilyUser(orgId, familyUserId, scene, verifyCode, consume, null);
  }

  @Override
  public FamilySmsCodeVerifyResponse verifyCodeForFamilyUser(Long orgId, Long familyUserId, String scene, String verifyCode,
      boolean consume, String clientIp) {
    FamilyUser familyUser = findFamilyUser(orgId, familyUserId);
    String phone = familyUser == null ? null : familyUser.getPhone();
    return verifyCode(orgId, phone, normalizeScene(scene, SCENE_SECURITY), verifyCode, consume, clientIp);
  }

  private FamilyUser findFamilyUser(Long orgId, Long familyUserId) {
    if (familyUserId == null) {
      throw new IllegalArgumentException("缺少家属身份信息");
    }
    FamilyUser user = familyUserMapper.selectById(familyUserId);
    if (user == null || Integer.valueOf(1).equals(user.getIsDeleted()) || !Objects.equals(user.getOrgId(), orgId)) {
      throw new IllegalArgumentException("家属信息不存在或已失效");
    }
    if (!hasText(user.getPhone())) {
      throw new IllegalArgumentException("家属手机号未配置，无法发送验证码");
    }
    return user;
  }

  private void enforceSendLimit(Long orgId, String phone, String scene, FamilyPortalProperties.SmsCode smsCode) {
    int cooldownSeconds = Math.max(smsCode.getCooldownSeconds(), 1);
    int dailyLimit = Math.max(smsCode.getDailyLimit(), 1);
    LocalDateTime now = LocalDateTime.now();

    FamilySmsCodeLog latest = familySmsCodeLogMapper.selectOne(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getPhone, phone)
            .eq(FamilySmsCodeLog::getScene, scene)
            .orderByDesc(FamilySmsCodeLog::getCreateTime)
            .last("LIMIT 1"));
    if (latest != null && latest.getCreateTime() != null) {
      long elapsed = Duration.between(latest.getCreateTime(), now).getSeconds();
      if (elapsed >= 0 && elapsed < cooldownSeconds) {
        long waitSeconds = cooldownSeconds - elapsed;
        throw new IllegalArgumentException("请求过于频繁，请 " + waitSeconds + " 秒后再试");
      }
    }

    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    Long sentToday = familySmsCodeLogMapper.selectCount(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getPhone, phone)
            .eq(FamilySmsCodeLog::getScene, scene)
            .ge(FamilySmsCodeLog::getCreateTime, startOfDay));
    if (sentToday != null && sentToday >= dailyLimit) {
      throw new IllegalArgumentException("今日验证码发送次数已达上限，请明日再试");
    }
  }

  private void enforceIpSendLimit(Long orgId, String clientIp, FamilyPortalProperties.SmsCode smsCode) {
    if (!isIpGuardEnabled(clientIp, smsCode)) {
      return;
    }
    int cooldownSeconds = Math.max(smsCode.getIpCooldownSeconds(), 1);
    int dailyLimit = Math.max(smsCode.getIpDailyLimit(), 1);
    int banThreshold = Math.max(smsCode.getIpBanThreshold(), 1);
    int banMinutes = Math.max(smsCode.getIpBanMinutes(), 1);
    LocalDateTime now = LocalDateTime.now();

    FamilySmsCodeLog latest = familySmsCodeLogMapper.selectOne(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getClientIp, clientIp)
            .orderByDesc(FamilySmsCodeLog::getCreateTime)
            .last("LIMIT 1"));
    if (latest != null && latest.getCreateTime() != null) {
      long elapsed = Duration.between(latest.getCreateTime(), now).getSeconds();
      if (elapsed >= 0 && elapsed < cooldownSeconds) {
        throw new IllegalArgumentException("操作过于频繁，请稍后再试");
      }
    }

    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    Long ipSentToday = familySmsCodeLogMapper.selectCount(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getClientIp, clientIp)
            .ge(FamilySmsCodeLog::getCreateTime, startOfDay));
    if (ipSentToday != null && ipSentToday >= dailyLimit) {
      throw new IllegalArgumentException("当前网络请求过于频繁，请稍后再试");
    }

    Long ipFailedRecent = familySmsCodeLogMapper.selectCount(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getClientIp, clientIp)
            .eq(FamilySmsCodeLog::getStatus, STATUS_FAILED)
            .ge(FamilySmsCodeLog::getUpdateTime, now.minusMinutes(banMinutes)));
    if (ipFailedRecent != null && ipFailedRecent >= banThreshold) {
      throw new IllegalArgumentException("操作受限，请稍后再试");
    }
  }

  private void enforceIpVerifyLimit(Long orgId, String clientIp, FamilyPortalProperties.SmsCode smsCode) {
    if (!isIpGuardEnabled(clientIp, smsCode)) {
      return;
    }
    int banThreshold = Math.max(smsCode.getIpBanThreshold(), 1);
    int banMinutes = Math.max(smsCode.getIpBanMinutes(), 1);
    LocalDateTime since = LocalDateTime.now().minusMinutes(banMinutes);
    List<FamilySmsCodeLog> recent = familySmsCodeLogMapper.selectList(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getClientIp, clientIp)
            .ge(FamilySmsCodeLog::getUpdateTime, since)
            .orderByDesc(FamilySmsCodeLog::getUpdateTime)
            .last("LIMIT " + VERIFY_IP_SAMPLE_LIMIT));
    if (recent == null || recent.isEmpty()) {
      return;
    }
    int failureScore = 0;
    for (FamilySmsCodeLog log : recent) {
      if (log == null) {
        continue;
      }
      String lastError = defaultText(log.getLastError(), "");
      if (lastError.contains("验证码错误")) {
        failureScore += Math.max(log.getVerifyAttempts() == null ? 1 : log.getVerifyAttempts(), 1);
      } else if (STATUS_FAILED.equalsIgnoreCase(defaultText(log.getStatus(), ""))) {
        failureScore += 1;
      }
      if (failureScore >= banThreshold) {
        throw new IllegalArgumentException("验证码错误次数过多，请稍后再试");
      }
    }
  }

  private boolean isIpGuardEnabled(String clientIp, FamilyPortalProperties.SmsCode smsCode) {
    return smsCode != null && smsCode.isIpGuardEnabled() && hasText(clientIp);
  }

  private FamilySmsCodeVerifyResponse failVerify(String message, int remainingAttempts) {
    FamilySmsCodeVerifyResponse response = new FamilySmsCodeVerifyResponse();
    response.setPassed(false);
    response.setRemainingAttempts(Math.max(remainingAttempts, 0));
    response.setMessage(message);
    return response;
  }

  private String hashVerifyCode(String phone, String scene, String code, String salt) {
    String raw = phone + "|" + scene + "|" + code + "|" + defaultText(salt, "smartcare-family-sms");
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception ex) {
      throw new IllegalStateException("验证码哈希失败", ex);
    }
  }

  private String buildBizNo(String scene) {
    return "FAMSMS-" + scene + "-" + System.currentTimeMillis() + "-"
        + Integer.toString(SECURE_RANDOM.nextInt(10000) + 10000).substring(1);
  }

  private String generateCode(int codeLength) {
    int length = codeLength <= 0 ? 6 : Math.min(codeLength, 8);
    int max = (int) Math.pow(10, length);
    int min = (int) Math.pow(10, length - 1);
    int code = SECURE_RANDOM.nextInt(max - min) + min;
    return String.valueOf(code);
  }

  private FamilyPortalProperties.SmsCode ensureSmsCodeEnabled() {
    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode();
    if (smsCode == null || !smsCode.isEnabled()) {
      throw new IllegalStateException("短信验证码功能未启用");
    }
    return smsCode;
  }

  private FamilySmsSender resolveSender(String provider) {
    String key = defaultText(provider, "mock").toLowerCase(Locale.ROOT);
    FamilySmsSender sender = senderMap.get(key);
    if (sender != null) {
      return sender;
    }
    FamilySmsSender fallback = senderMap.get("mock");
    if (fallback != null) {
      return fallback;
    }
    throw new IllegalStateException("未找到可用的短信发送通道：" + key);
  }

  private Long requireOrgId(Long orgId) {
    if (orgId == null || orgId <= 0) {
      throw new IllegalArgumentException("机构ID不能为空");
    }
    return orgId;
  }

  private String normalizePhone(String phone) {
    if (!hasText(phone)) {
      throw new IllegalArgumentException("手机号不能为空");
    }
    String normalized = phone.trim();
    if (!PHONE_PATTERN.matcher(normalized).matches()) {
      throw new IllegalArgumentException("手机号格式不正确");
    }
    return normalized;
  }

  private String normalizeCode(String verifyCode) {
    if (!hasText(verifyCode)) {
      throw new IllegalArgumentException("验证码不能为空");
    }
    String code = verifyCode.trim();
    if (!code.matches("^\\d{4,8}$")) {
      throw new IllegalArgumentException("验证码格式不正确");
    }
    return code;
  }

  private String normalizeScene(String scene, String fallback) {
    String value = defaultText(scene, fallback);
    String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "");
    if (!hasText(normalized)) {
      return fallback;
    }
    return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
  }

  private String normalizeClientIp(String clientIp) {
    if (!hasText(clientIp)) {
      return "";
    }
    String value = clientIp.trim();
    if (value.contains(",")) {
      value = value.substring(0, value.indexOf(',')).trim();
    }
    if ("unknown".equalsIgnoreCase(value)) {
      return "";
    }
    if (value.length() > 64) {
      value = value.substring(0, 64);
    }
    return value;
  }

  private String defaultText(String value, String fallback) {
    if (!hasText(value)) {
      return fallback;
    }
    return value.trim();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
