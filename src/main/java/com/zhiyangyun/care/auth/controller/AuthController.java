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
import com.zhiyangyun.care.auth.security.TokenBlacklistService;
import com.zhiyangyun.care.auth.security.TokenProvider;
import com.zhiyangyun.care.auth.security.PermissionRegistry;
import com.zhiyangyun.care.auth.security.PagePermissionPathHelper;
import com.zhiyangyun.care.auth.security.RoleCodeHelper;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.family.service.FamilySmsCodeService;
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
      ObjectMapper objectMapper) {
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
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    String loginId = defaultText(request.getUsername(), "");
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginId, request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    StaffAccount staff = staffMapper.selectOne(
        Wrappers.lambdaQuery(StaffAccount.class)
            .and(w -> w.eq(StaffAccount::getUsername, loginId).or().eq(StaffAccount::getStaffNo, loginId))
            .eq(StaffAccount::getStatus, 1)
            .eq(StaffAccount::getIsDeleted, 0));
    if (staff == null) {
      throw new IllegalArgumentException("账号不存在或已停用");
    }
    staff.setLastLoginTime(java.time.LocalDateTime.now());
    staffMapper.updateById(staff);
    List<String> roles = RoleCodeHelper.normalizeRoles(
        roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId()));
    List<Role> roleEntities = roleMapper.selectRolesByStaff(staff.getId(), staff.getOrgId());

    String token = tokenProvider.generateToken(staff.getId(), staff.getUsername(), staff.getOrgId(), roles);
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setRoles(roles);
    response.setPermissions(permissionRegistry.getPermissionsByRoles(roles));
    response.setPagePermissions(mergeRoutePermissions(roleEntities));
    response.setStaffInfo(toStaffInfo(staff));
    return Result.ok(response);
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

  private FamilyLoginResponse buildFamilyLoginResponse(FamilyUser user) {
    String token = tokenProvider.generateToken(user.getId(), user.getPhone(), user.getOrgId(), List.of("FAMILY"));
    FamilyLoginResponse response = new FamilyLoginResponse();
    response.setToken(token);
    response.setFamilyUserId(user.getId());
    response.setOrgId(user.getOrgId());
    response.setPhone(user.getPhone());
    response.setRealName(user.getRealName());
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
    Long staffId = com.zhiyangyun.care.auth.security.AuthContext.getStaffId();
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    if (staff == null) {
      String username = com.zhiyangyun.care.auth.security.AuthContext.getUsername();
      staff = staffMapper.selectOne(
          Wrappers.lambdaQuery(StaffAccount.class)
              .eq(StaffAccount::getUsername, username));
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
      merged.addAll(PagePermissionPathHelper.parseAndNormalize(objectMapper, role.getRoutePermissionsJson()));
    }
    return new ArrayList<>(merged);
  }
}
