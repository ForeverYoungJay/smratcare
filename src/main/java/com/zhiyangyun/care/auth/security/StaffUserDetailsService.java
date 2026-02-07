package com.zhiyangyun.care.auth.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffUserDetailsService implements UserDetailsService {
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;

  public StaffUserDetailsService(StaffMapper staffMapper, RoleMapper roleMapper) {
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    StaffAccount staff = staffMapper.selectOne(
        Wrappers.lambdaQuery(StaffAccount.class)
            .eq(StaffAccount::getUsername, username)
            .eq(StaffAccount::getStatus, 1)
            .eq(StaffAccount::getIsDeleted, 0));
    if (staff == null) {
      throw new UsernameNotFoundException("User not found");
    }

    List<String> roleCodes = roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId());
    List<GrantedAuthority> authorities = roleCodes.stream()
        .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    return User.builder()
        .username(staff.getUsername())
        .password(staff.getPasswordHash())
        .authorities(authorities)
        .build();
  }
}
