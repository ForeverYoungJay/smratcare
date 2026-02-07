package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.auth.model.LoginResponse;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffInfo;
import com.zhiyangyun.care.auth.model.FamilyLoginRequest;
import com.zhiyangyun.care.auth.model.FamilyLoginResponse;
import com.zhiyangyun.care.auth.security.TokenBlacklistService;
import com.zhiyangyun.care.auth.security.TokenProvider;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final TokenBlacklistService tokenBlacklistService;
  private final FamilyUserMapper familyUserMapper;

  public AuthController(AuthenticationManager authenticationManager,
      TokenProvider tokenProvider,
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      TokenBlacklistService tokenBlacklistService,
      FamilyUserMapper familyUserMapper) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.tokenBlacklistService = tokenBlacklistService;
    this.familyUserMapper = familyUserMapper;
  }

  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    StaffAccount staff = staffMapper.selectOne(
        Wrappers.lambdaQuery(StaffAccount.class)
            .eq(StaffAccount::getUsername, request.getUsername())
            .eq(StaffAccount::getIsDeleted, 0));
    List<String> roles = roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId());

    String token = tokenProvider.generateToken(staff.getId(), staff.getUsername(), staff.getOrgId(), roles);
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setRoles(roles);
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
  public Result<FamilyLoginResponse> familyLogin(@Valid @RequestBody FamilyLoginRequest request) {
    // 验证码校验占位
    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, request.getOrgId())
            .eq(FamilyUser::getPhone, request.getPhone())
            .eq(FamilyUser::getIsDeleted, 0));
    if (user == null) {
      user = new FamilyUser();
      user.setOrgId(request.getOrgId());
      user.setPhone(request.getPhone());
      user.setRealName(request.getPhone());
      user.setStatus(1);
      familyUserMapper.insert(user);
    }

    String token = tokenProvider.generateToken(user.getId(), user.getPhone(), user.getOrgId(), List.of("FAMILY"));
    FamilyLoginResponse response = new FamilyLoginResponse();
    response.setToken(token);
    response.setFamilyUserId(user.getId());
    response.setPhone(user.getPhone());
    response.setRealName(user.getRealName());
    return Result.ok(response);
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
    StaffInfo info = new StaffInfo();
    info.setId(staff.getId());
    info.setOrgId(staff.getOrgId());
    info.setDepartmentId(staff.getDepartmentId());
    info.setUsername(staff.getUsername());
    info.setRealName(staff.getRealName());
    info.setPhone(staff.getPhone());
    info.setStatus(staff.getStatus());
    return info;
  }
}
