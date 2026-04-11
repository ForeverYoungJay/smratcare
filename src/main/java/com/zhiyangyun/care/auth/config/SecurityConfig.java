package com.zhiyangyun.care.auth.config;

import com.zhiyangyun.care.auth.security.JwtAuthenticationFilter;
import com.zhiyangyun.care.common.web.RequestTraceFilter;
import com.zhiyangyun.care.common.web.ResponseContentTypeFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  private static final String[] STAFF_ROLES = {
      "STAFF",
      "HR_EMPLOYEE", "HR_MINISTER",
      "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
      "NURSING_EMPLOYEE", "NURSING_MINISTER",
      "FINANCE_EMPLOYEE", "FINANCE_MINISTER",
      "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
      "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] MINISTER_OR_HIGHER_ROLES = {
      "HR_MINISTER",
      "MEDICAL_MINISTER",
      "NURSING_MINISTER",
      "FINANCE_MINISTER",
      "LOGISTICS_MINISTER",
      "MARKETING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] MEDICAL_AND_NURSING_ROLES = {
      "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
      "NURSING_EMPLOYEE", "NURSING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] LOGISTICS_ROLES = {
      "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] MARKETING_ROLES = {
      "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] STATS_ROLES = {
      "HR_EMPLOYEE", "HR_MINISTER",
      "FINANCE_EMPLOYEE", "FINANCE_MINISTER",
      "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
      "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private static final String[] SURVEY_ROLES = {
      "HR_EMPLOYEE", "HR_MINISTER",
      "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
      "NURSING_EMPLOYEE", "NURSING_MINISTER",
      "DIRECTOR", "SYS_ADMIN", "ADMIN"
  };
  private final JwtAuthenticationFilter jwtFilter;
  private final RequestTraceFilter requestTraceFilter;
  private final ResponseContentTypeFilter responseContentTypeFilter;
  private final RestAuthenticationEntryPoint authenticationEntryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;

  public SecurityConfig(JwtAuthenticationFilter jwtFilter,
      RequestTraceFilter requestTraceFilter,
      ResponseContentTypeFilter responseContentTypeFilter,
      RestAuthenticationEntryPoint authenticationEntryPoint,
      RestAccessDeniedHandler accessDeniedHandler) {
    this.jwtFilter = jwtFilter;
    this.requestTraceFilter = requestTraceFilter;
    this.responseContentTypeFilter = responseContentTypeFilter;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/api/auth/login",
                "/api/auth/family/bootstrap",
                "/api/auth/family/login",
                "/api/auth/family/register",
                "/api/auth/family/password/reset",
                "/api/auth/family/sms-code/**",
                "/api/family/register").permitAll()
            .requestMatchers("/api/family/payment/wechat/notify").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/api/files/**").authenticated()
            .requestMatchers("/api/auth/**").authenticated()
            .requestMatchers("/api/admin/**").hasAnyRole(MINISTER_OR_HIGHER_ROLES)
            .requestMatchers("/api/base-config/**").hasAnyRole(MINISTER_OR_HIGHER_ROLES)
            .requestMatchers("/api/medical-care/**", "/api/health/**", "/api/vital/**").hasAnyRole(MEDICAL_AND_NURSING_ROLES)
            .requestMatchers(
                "/api/asset/**",
                "/api/material/**",
                "/api/material-center/**",
                "/api/inventory/**",
                "/api/store/**",
                "/api/logistics/**",
                "/api/life/dining/**").hasAnyRole(LOGISTICS_ROLES)
            .requestMatchers("/api/marketing/**").hasAnyRole(MARKETING_ROLES)
            .requestMatchers("/api/stats/**").hasAnyRole(STATS_ROLES)
            .requestMatchers("/api/survey/**").hasAnyRole(SURVEY_ROLES)
            .requestMatchers("/api/oa/**").hasAnyRole(
                "STAFF",
                "HR_EMPLOYEE", "HR_MINISTER",
                "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER",
                "NURSING_EMPLOYEE", "NURSING_MINISTER",
                "FINANCE_EMPLOYEE", "FINANCE_MINISTER",
                "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
                "MARKETING_EMPLOYEE", "MARKETING_MINISTER",
                "DIRECTOR", "SYS_ADMIN", "ADMIN")
            .requestMatchers("/api/fire/**").hasAnyRole(
                "GUARD",
                "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER",
                "DIRECTOR", "SYS_ADMIN", "ADMIN")
            .requestMatchers("/api/guard/**").hasAnyRole("GUARD", "ADMIN")
            .requestMatchers("/api/family/**").hasRole("FAMILY")
            .requestMatchers("/api/**").hasAnyRole(STAFF_ROLES)
            .anyRequest().authenticated())
        .addFilterBefore(responseContentTypeFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(requestTraceFilter, JwtAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public FilterRegistrationBean<RequestTraceFilter> requestTraceFilterRegistration(RequestTraceFilter filter) {
    FilterRegistrationBean<RequestTraceFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}
