package com.zhiyangyun.care.auth.model;

import lombok.Data;

/** 家属端微信一键登录请求：手机号授权 code + 登录 code。 */
@Data
public class FamilyWechatLoginRequest {
  private Long orgId;
  /** getPhoneNumber 返回的 code，用于换取手机号。 */
  private String phoneCode;
  /** wx.login 返回的 code，用于换取 openId（可选，用于同时绑定通知）。 */
  private String loginCode;
  /** 首次登录可携带昵称，作为默认真实姓名。 */
  private String nickName;
}
