package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.auth.model.LoginResponse;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffInfo;
import com.zhiyangyun.care.auth.model.FamilyLoginRequest;
import com.zhiyangyun.care.auth.model.FamilyLoginResponse;
import com.zhiyangyun.care.auth.model.FamilyAccountRegisterRequest;
import com.zhiyangyun.care.auth.model.FamilyAuthBootstrapResponse;
import com.zhiyangyun.care.auth.model.FamilyPasswordResetRequest;
import com.zhiyangyun.care.auth.model.FamilySmsCodeSendRequest;
import com.zhiyangyun.care.auth.model.FamilySmsCodeSendResponse;
import com.zhiyangyun.care.auth.model.FamilySmsCodeVerifyRequest;
import com.zhiyangyun.care.auth.model.FamilySmsCodeVerifyResponse;
import com.zhiyangyun.care.auth.model.FamilyWechatLoginRequest;
import com.zhiyangyun.care.auth.model.TwoFactorResendRequest;
import com.zhiyangyun.care.auth.model.TwoFactorVerifyRequest;
import com.zhiyangyun.care.auth.security.TokenBlacklistService;
import com.zhiyangyun.care.auth.security.TokenProvider;
import com.zhiyangyun.care.auth.security.PermissionRegistry;
import com.zhiyangyun.care.auth.security.PagePermissionPathHelper;
import com.zhiyangyun.care.auth.security.RoleCodeHelper;
import com.zhiyangyun.care.auth.security.RolePagePermissionPresetHelper;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import com.zhiyangyun.care.compliance.service.LoginSecurityService;
import com.zhiyangyun.care.compliance.util.DataMaskingUtil;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.service.FamilySmsCodeService;
import com.zhiyangyun.care.family.service.WechatMiniAppAuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final TokenProvider tokenProvider;
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;
  private final OrgMapper orgMapper;
  private final TokenBlacklistService tokenBlacklistService;
  private final FamilyUserMapper familyUserMapper;
  private final PermissionRegistry permissionRegistry;
  private final FamilySmsCodeService familySmsCodeService;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final ComplianceSecurityPolicyService securityPolicyService;
  private final LoginSecurityService loginSecurityService;
  private final WechatMiniAppAuthService wechatMiniAppAuthService;
  private final FamilyPortalProperties familyPortalProperties;

  public AuthController(AuthenticationManager authenticationManager,
      TokenProvider tokenProvider,
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      OrgMapper orgMapper,
      TokenBlacklistService tokenBlacklistService,
      FamilyUserMapper familyUserMapper,
      PermissionRegistry permissionRegistry,
      FamilySmsCodeService familySmsCodeService,
      PasswordEncoder passwordEncoder,
      ObjectMapper objectMapper,
      ComplianceSecurityPolicyService securityPolicyService,
      LoginSecurityService loginSecurityService,
      WechatMiniAppAuthService wechatMiniAppAuthService,
      FamilyPortalProperties familyPortalProperties) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.orgMapper = orgMapper;
    this.tokenBlacklistService = tokenBlacklistService;
    this.familyUserMapper = familyUserMapper;
    this.permissionRegistry = permissionRegistry;
    this.familySmsCodeService = familySmsCodeService;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
    this.securityPolicyService = securityPolicyService;
    this.loginSecurityService = loginSecurityService;
    this.wechatMiniAppAuthService = wechatMiniAppAuthService;
    this.familyPortalProperties = familyPortalProperties;
  }

  @GetMapping("/family/bootstrap")
  public Result<FamilyAuthBootstrapResponse> familyBootstrap() {
    Long orgId = resolveFamilyOrgId(null);
    Org org = orgMapper.selectById(orgId);
    FamilyAuthBootstrapResponse response = new FamilyAuthBootstrapResponse();
    response.setOrgId(orgId);
    response.setOrgName(org == null ? "默认机构" : defaultText(org.getOrgName(), "默认机构"));
    response.setLoginMode("PASSWORD");
    response.setRegisterRequireSms(true);
    response.setResetPasswordRequireSms(true);
    return Result.ok(response);
  }

  @PostMapping("/family/sms-code/send")
  public Result<FamilySmsCodeSendResponse> sendFamilySmsCode(@Valid @RequestBody FamilySmsCodeSendRequest request,
      HttpServletRequest httpServletRequest) {
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    return Result.ok(familySmsCodeService.sendCode(
        orgId, null, request.getPhone(), request.getScene(), resolveClientIp(httpServletRequest)));
  }

  @PostMapping("/family/sms-code/verify")
  public Result<FamilySmsCodeVerifyResponse> verifyFamilySmsCode(@Valid @RequestBody FamilySmsCodeVerifyRequest request,
      HttpServletRequest httpServletRequest) {
    boolean consume = request.getConsume() == null || request.getConsume();
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    return Result.ok(familySmsCodeService.verifyCode(
        orgId, request.getPhone(), request.getScene(), request.getVerifyCode(), consume,
        resolveClientIp(httpServletRequest)));
  }

  @PostMapping("/family/register")
  public Result<FamilyLoginResponse> registerFamily(@Valid @RequestBody FamilyAccountRegisterRequest request,
      HttpServletRequest httpServletRequest) {
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    String phone = defaultText(request.getPhone(), null);
    if (!hasText(phone)) {
      throw new IllegalArgumentException("手机号不能为空");
    }
    FamilySmsCodeVerifyResponse verifyResult = familySmsCodeService.verifyCode(
        orgId, phone, "REGISTER", request.getVerifyCode(), true,
        resolveClientIp(httpServletRequest));
    if (!Boolean.TRUE.equals(verifyResult.getPassed())) {
      throw new IllegalArgumentException(defaultText(verifyResult.getMessage(), "验证码校验失败"));
    }

    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getPhone, phone)
            .eq(FamilyUser::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (user == null) {
      user = new FamilyUser();
      user.setOrgId(orgId);
      user.setPhone(phone);
      user.setUsername(phone);
      user.setRealName(defaultText(request.getRealName(), phone));
      user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
      user.setStatus(1);
      familyUserMapper.insert(user);
    } else {
      if (hasText(user.getPasswordHash())) {
        throw new IllegalArgumentException("该手机号已注册，请直接登录");
      }
      user.setUsername(defaultText(user.getUsername(), phone));
      user.setRealName(defaultText(request.getRealName(), defaultText(user.getRealName(), phone)));
      user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
      user.setStatus(1);
      familyUserMapper.updateById(user);
    }
    return Result.ok(buildFamilyLoginResponse(user));
  }

  @PostMapping("/family/password/reset")
  public Result<Boolean> resetFamilyPassword(@Valid @RequestBody FamilyPasswordResetRequest request,
      HttpServletRequest httpServletRequest) {
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    String phone = defaultText(request.getPhone(), null);
    if (!hasText(phone)) {
      throw new IllegalArgumentException("手机号不能为空");
    }
    FamilySmsCodeVerifyResponse verifyResult = familySmsCodeService.verifyCode(
        orgId, phone, "RESET_PASSWORD", request.getVerifyCode(), true,
        resolveClientIp(httpServletRequest));
    if (!Boolean.TRUE.equals(verifyResult.getPassed())) {
      throw new IllegalArgumentException(defaultText(verifyResult.getMessage(), "验证码校验失败"));
    }

    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getPhone, phone)
            .eq(FamilyUser::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (user == null) {
      throw new IllegalArgumentException("账号不存在，请先注册");
    }
    user.setUsername(defaultText(user.getUsername(), user.getPhone()));
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
    familyUserMapper.updateById(user);
    return Result.ok(true);
  }

  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
      HttpServletRequest httpServletRequest) {
    String loginId = defaultText(request.getUsername(), "");

    // 机构安全策略：登录失败锁定（策略读取/校验失败均不阻断登录，见 LoginSecurityService）
    StaffAccount policyStaff = staffMapper.selectOne(
        Wrappers.lambdaQuery(StaffAccount.class)
            .and(w -> w.eq(StaffAccount::getUsername, loginId).or().eq(StaffAccount::getStaffNo, loginId))
            .eq(StaffAccount::getIsDeleted, 0)
            .last("LIMIT 1"));
    SecurityPolicyView policy = securityPolicyService.getEffectivePolicy(
        policyStaff == null ? null : policyStaff.getOrgId());
    loginSecurityService.assertNotLocked(loginId, policy);

    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginId, request.getPassword()));
    } catch (Exception ex) {
      loginSecurityService.record(policyStaff == null ? null : policyStaff.getOrgId(),
          policyStaff == null ? null : policyStaff.getId(), loginId, "PASSWORD", false, "BAD_CREDENTIALS");
      throw ex;
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);

    StaffAccount staff = staffMapper.selectOne(
        Wrappers.lambdaQuery(StaffAccount.class)
            .and(w -> w.eq(StaffAccount::getUsername, loginId).or().eq(StaffAccount::getStaffNo, loginId))
            .eq(StaffAccount::getStatus, 1)
            .eq(StaffAccount::getIsDeleted, 0));
    if (staff == null) {
      loginSecurityService.record(null, null, loginId, "PASSWORD", false, "ACCOUNT_DISABLED");
      throw new IllegalArgumentException("账号不存在或已停用");
    }

    // 机构安全策略：密码有效期（password_max_days=0 表示不启用；无密码更新时间时不判定）
    if (policy.getPasswordMaxDays() != null && policy.getPasswordMaxDays() > 0
        && staff.getPasswordSnapshotUpdatedAt() != null
        && staff.getPasswordSnapshotUpdatedAt().plusDays(policy.getPasswordMaxDays())
            .isBefore(java.time.LocalDateTime.now())) {
      loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(),
          "PASSWORD", false, "PASSWORD_EXPIRED");
      throw new IllegalArgumentException(
          "密码已超过有效期（" + policy.getPasswordMaxDays() + " 天），请联系管理员重置密码后再登录");
    }

    List<String> roles = RoleCodeHelper.normalizeRoles(
        roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId()));

    // 机构安全策略：双因子登录（返回挑战令牌，不签发正式 token）
    if (requiresTwoFactor(policy, roles)) {
      if (hasText(staff.getPhone())) {
        familySmsCodeService.sendCode(staff.getOrgId(), null, staff.getPhone(), "STAFF_2FA",
            resolveClientIp(httpServletRequest));
        loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(),
            "2FA_CHALLENGE", true, null);
        LoginResponse challengeResponse = new LoginResponse();
        challengeResponse.setRequireTwoFactor(true);
        challengeResponse.setChallengeToken(
            tokenProvider.generateChallengeToken(staff.getId(), staff.getUsername(), staff.getOrgId()));
        challengeResponse.setTwoFactorPhone(DataMaskingUtil.maskPhone(staff.getPhone()));
        return Result.ok(challengeResponse);
      }
      // 未配置手机号时降级为单因子放行（避免管理员被锁死），留痕以便审计
      loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(),
          "2FA_CHALLENGE", false, "2FA_PHONE_MISSING");
    }

    staff.setLastLoginTime(java.time.LocalDateTime.now());
    staffMapper.updateById(staff);
    List<Role> roleEntities = roleMapper.selectRolesByStaff(staff.getId(), staff.getOrgId());

    // 机构安全策略：会话超时（session_timeout_minutes=0 表示使用全局 JWT 默认）
    String token = tokenProvider.generateToken(staff.getId(), staff.getUsername(), staff.getOrgId(), roles,
        policy.getSessionTimeoutMinutes() == null ? 0 : policy.getSessionTimeoutMinutes());
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setRoles(roles);
    response.setPermissions(permissionRegistry.getPermissionsByRoles(roles));
    response.setPagePermissions(mergeRoutePermissions(roleEntities));
    response.setStaffInfo(toStaffInfo(staff));
    loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(), "PASSWORD", true, null);
    return Result.ok(response);
  }

  /** 2FA 第二步：凭挑战令牌 + 短信验证码换取正式登录令牌。 */
  @PostMapping("/2fa/verify")
  public Result<LoginResponse> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request,
      HttpServletRequest httpServletRequest) {
    StaffAccount staff = resolveChallengeStaff(request.getChallengeToken());
    Claims claims = parseChallengeClaims(request.getChallengeToken());

    FamilySmsCodeVerifyResponse verifyResult = familySmsCodeService.verifyCode(
        staff.getOrgId(), staff.getPhone(), "STAFF_2FA", request.getVerifyCode(), true,
        resolveClientIp(httpServletRequest));
    if (!Boolean.TRUE.equals(verifyResult.getPassed())) {
      loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(),
          "2FA_VERIFY", false, "2FA_CODE_ERROR");
      throw new IllegalArgumentException(defaultText(verifyResult.getMessage(), "验证码校验失败"));
    }

    // 挑战令牌一次性使用：校验通过后立即拉黑
    long ttl = tokenProvider.getExpirationMillis(claims) - System.currentTimeMillis();
    tokenBlacklistService.blacklist(claims.getId(), Math.max(ttl, 0));

    staff.setLastLoginTime(java.time.LocalDateTime.now());
    staffMapper.updateById(staff);
    List<String> roles = RoleCodeHelper.normalizeRoles(
        roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId()));
    List<Role> roleEntities = roleMapper.selectRolesByStaff(staff.getId(), staff.getOrgId());

    SecurityPolicyView policy = securityPolicyService.getEffectivePolicy(staff.getOrgId());
    String token = tokenProvider.generateToken(staff.getId(), staff.getUsername(), staff.getOrgId(), roles,
        policy.getSessionTimeoutMinutes() == null ? 0 : policy.getSessionTimeoutMinutes());
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setRoles(roles);
    response.setPermissions(permissionRegistry.getPermissionsByRoles(roles));
    response.setPagePermissions(mergeRoutePermissions(roleEntities));
    response.setStaffInfo(toStaffInfo(staff));
    loginSecurityService.record(staff.getOrgId(), staff.getId(), staff.getUsername(), "2FA_VERIFY", true, null);
    return Result.ok(response);
  }

  /** 2FA 第二步：重发短信验证码（沿用家属短信通道的冷却/限流）。 */
  @PostMapping("/2fa/resend")
  public Result<FamilySmsCodeSendResponse> resendTwoFactorCode(@Valid @RequestBody TwoFactorResendRequest request,
      HttpServletRequest httpServletRequest) {
    StaffAccount staff = resolveChallengeStaff(request.getChallengeToken());
    return Result.ok(familySmsCodeService.sendCode(staff.getOrgId(), null, staff.getPhone(), "STAFF_2FA",
        resolveClientIp(httpServletRequest)));
  }

  private Claims parseChallengeClaims(String challengeToken) {
    Claims claims;
    try {
      claims = tokenProvider.parseToken(challengeToken);
    } catch (Exception ex) {
      throw new IllegalArgumentException("二次验证会话已失效，请重新登录");
    }
    if (!TokenProvider.TOKEN_TYPE_2FA_CHALLENGE.equals(
        claims.get(TokenProvider.CLAIM_TOKEN_TYPE, String.class))) {
      throw new IllegalArgumentException("无效的二次验证会话，请重新登录");
    }
    if (tokenBlacklistService.isBlacklisted(claims.getId())) {
      throw new IllegalArgumentException("二次验证会话已使用，请重新登录");
    }
    return claims;
  }

  private StaffAccount resolveChallengeStaff(String challengeToken) {
    Claims claims = parseChallengeClaims(challengeToken);
    Long staffId;
    try {
      staffId = Long.valueOf(claims.getSubject());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("无效的二次验证会话，请重新登录");
    }
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null || !Integer.valueOf(1).equals(staff.getStatus())
        || Integer.valueOf(1).equals(staff.getIsDeleted())) {
      throw new IllegalArgumentException("账号不存在或已停用");
    }
    if (!hasText(staff.getPhone())) {
      throw new IllegalArgumentException("账号未配置手机号，无法完成二次验证");
    }
    return staff;
  }

  private boolean requiresTwoFactor(SecurityPolicyView policy, List<String> roles) {
    if (policy == null || !Boolean.TRUE.equals(policy.getTwoFactorEnabled())) {
      return false;
    }
    List<String> scopedRoles = policy.getTwoFactorRoles();
    if (scopedRoles == null || scopedRoles.isEmpty()) {
      return true;
    }
    if (roles == null || roles.isEmpty()) {
      return false;
    }
    for (String role : roles) {
      if (role != null && scopedRoles.contains(role.toUpperCase(java.util.Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  @PostMapping("/logout")
  public Result<Void> logout(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        var claims = tokenProvider.parseToken(token);
        long exp = tokenProvider.getExpirationMillis(claims);
        long ttl = exp - System.currentTimeMillis();
        tokenBlacklistService.blacklist(claims.getId(), ttl);
      } catch (Exception ignored) {
        // ignore invalid token
      }
    }
    return Result.ok(null);
  }

  @PostMapping("/family/login")
  public Result<FamilyLoginResponse> familyLogin(@Valid @RequestBody FamilyLoginRequest request,
      HttpServletRequest httpServletRequest) {
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    String phone = defaultText(request.getPhone(), null);
    if (!hasText(phone)) {
      throw new IllegalArgumentException("手机号不能为空");
    }
    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getPhone, phone)
            .eq(FamilyUser::getIsDeleted, 0));
    if (user == null || user.getStatus() == null || user.getStatus() != 1
        || !hasText(user.getPasswordHash())
        || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("账号或密码错误");
    }
    user.setUsername(defaultText(user.getUsername(), user.getPhone()));
    familyUserMapper.updateById(user);
    return Result.ok(buildFamilyLoginResponse(user));
  }

  /** 微信手机号一键登录：授权手机号换账号，缺账号时按配置自动创建。 */
  @PostMapping("/family/wechat-login")
  public Result<FamilyLoginResponse> familyWechatLogin(@Valid @RequestBody FamilyWechatLoginRequest request) {
    if (!wechatMiniAppAuthService.isLoginEnabled()) {
      throw new IllegalStateException("微信一键登录暂未开启，请使用手机号密码登录");
    }
    Long orgId = resolveFamilyOrgId(request.getOrgId());
    String phone = wechatMiniAppAuthService.resolvePhoneNumber(request.getPhoneCode());
    if (!hasText(phone)) {
      throw new IllegalArgumentException("未能获取微信手机号，请改用手机号密码登录");
    }
    String openId = null;
    try {
      openId = wechatMiniAppAuthService.resolveOpenId(request.getLoginCode());
    } catch (Exception ex) {
      // openId 仅用于通知绑定，获取失败不阻断登录
    }

    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getPhone, phone)
            .eq(FamilyUser::getIsDeleted, 0)
            .last("LIMIT 1"));
    boolean created = false;
    if (user == null) {
      FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
      if (miniApp == null || !miniApp.isAutoCreateAccount()) {
        throw new IllegalArgumentException("该手机号尚未注册家属账号，请先注册");
      }
      user = new FamilyUser();
      user.setOrgId(orgId);
      user.setPhone(phone);
      user.setUsername(phone);
      user.setRealName(defaultText(request.getNickName(), phone));
      user.setStatus(1);
      if (openId != null) {
        user.setOpenId(openId);
      }
      familyUserMapper.insert(user);
      created = true;
    } else {
      if (user.getStatus() == null || user.getStatus() != 1) {
        throw new IllegalArgumentException("账号已停用，请联系机构管理员");
      }
      boolean dirty = false;
      if (!hasText(user.getUsername())) {
        user.setUsername(phone);
        dirty = true;
      }
      if (openId != null && !openId.equals(defaultText(user.getOpenId(), null))) {
        user.setOpenId(openId);
        dirty = true;
      }
      if (dirty) {
        familyUserMapper.updateById(user);
      }
    }
    FamilyLoginResponse response = buildFamilyLoginResponse(user);
    response.setNewAccount(created);
    return Result.ok(response);
  }

  private FamilyLoginResponse buildFamilyLoginResponse(FamilyUser user) {
    String token = tokenProvider.generateToken(user.getId(), user.getPhone(), user.getOrgId(), List.of("FAMILY"));
    FamilyLoginResponse response = new FamilyLoginResponse();
    response.setToken(token);
    response.setFamilyUserId(user.getId());
    response.setOrgId(user.getOrgId());
    response.setPhone(user.getPhone());
    response.setRealName(user.getRealName());
    response.setNewAccount(Boolean.FALSE);
    return response;
  }

  private Long resolveFamilyOrgId(Long orgId) {
    if (orgId != null && orgId > 0) {
      return orgId;
    }
    Org first = orgMapper.selectOne(
        Wrappers.lambdaQuery(Org.class)
            .eq(Org::getIsDeleted, 0)
            .eq(Org::getStatus, 1)
            .orderByAsc(Org::getId)
            .last("LIMIT 1"));
    if (first != null && first.getId() != null) {
      return first.getId();
    }
    throw new IllegalStateException("未找到可用机构，请联系管理员");
  }

  private String defaultText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String resolveClientIp(HttpServletRequest request) {
    if (request == null) {
      return "";
    }
    String[] candidateHeaders = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };
    for (String header : candidateHeaders) {
      String value = request.getHeader(header);
      if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim())) {
        return value.trim();
      }
    }
    String remoteAddr = request.getRemoteAddr();
    return remoteAddr == null ? "" : remoteAddr.trim();
  }

  @GetMapping("/me")
  public Result<StaffInfo> me() {
    if (com.zhiyangyun.care.auth.security.AuthContext.hasRole("FAMILY")) {
      return Result.ok(null);
    }
    Long staffId = com.zhiyangyun.care.auth.security.AuthContext.getStaffId();
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    if (staff == null) {
      String username = com.zhiyangyun.care.auth.security.AuthContext.getUsername();
      Long orgId = com.zhiyangyun.care.auth.security.AuthContext.getOrgId();
      staff = staffMapper.selectOne(
          Wrappers.lambdaQuery(StaffAccount.class)
              .eq(StaffAccount::getIsDeleted, 0)
              .eq(StaffAccount::getStatus, 1)
              .eq(orgId != null, StaffAccount::getOrgId, orgId)
              .eq(StaffAccount::getUsername, username)
              .last("LIMIT 1"));
    }
    return Result.ok(toStaffInfo(staff));
  }

  private StaffInfo toStaffInfo(StaffAccount staff) {
    if (staff == null) {
      return null;
    }
    StaffInfo info = new StaffInfo();
    info.setId(staff.getId());
    info.setOrgId(staff.getOrgId());
    info.setDepartmentId(staff.getDepartmentId());
    info.setStaffNo(staff.getStaffNo());
    info.setUsername(staff.getUsername());
    info.setRealName(staff.getRealName());
    info.setPhone(staff.getPhone());
    info.setDirectLeaderId(staff.getDirectLeaderId());
    info.setIndirectLeaderId(staff.getIndirectLeaderId());
    info.setStatus(staff.getStatus());
    return info;
  }

  private List<String> mergeRoutePermissions(List<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    LinkedHashSet<String> merged = new LinkedHashSet<>();
    for (Role role : roles) {
      if (role == null) {
        continue;
      }
      merged.addAll(RolePagePermissionPresetHelper.resolveEffectivePaths(objectMapper, role));
    }
    return new ArrayList<>(merged);
  }
}
