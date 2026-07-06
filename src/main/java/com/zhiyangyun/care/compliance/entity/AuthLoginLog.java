package com.zhiyangyun.care.compliance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 登录留痕：成功/失败均记录，支撑登录失败锁定与等保审计（留存 >= 6 个月）。
 */
@Data
@TableName("auth_login_log")
public class AuthLoginLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long staffId;
  private String username;
  /** PASSWORD / 2FA_CHALLENGE / 2FA_VERIFY */
  private String loginType;
  /** SUCCESS / FAIL */
  private String result;
  private String failReason;
  private String ip;
  private String userAgent;
  private String requestId;
  private LocalDateTime createTime;
  private Integer isDeleted;
}
