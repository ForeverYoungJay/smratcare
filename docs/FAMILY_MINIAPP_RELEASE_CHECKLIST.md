# 家属端小程序正式上线检查清单

## 1. 生产配置冻结
- `spring.profiles.active=prod`，并确认 `JWT_SECRET` 已替换为 32 位以上随机值。
- 已配置真实家属端能力：
  - `FAMILY_WECHAT_PAY_*`
  - `FAMILY_WECHAT_NOTIFY_*`
  - `FAMILY_SMS_CODE_*`
  - `APP_FILE_STORAGE_PROVIDER=oss`
  - `OSS_*`
- 生产配置中不得出现：
  - `localhost` / `127.0.0.1`
  - `skip-notify-signature-verify=true`
  - `FAMILY_SMS_CODE_PROVIDER=mock`
  - `FAMILY_SMS_CODE_DEBUG_RETURN_CODE=true`

## 2. 小程序发版前确认
- `family-miniapp/miniprogram/app.js` 运行时提示为“正式发布运行模式”，未启用 mock fallback、本地免登录、人工充值回退。
- 登录页可展示机构信息、服务协议版本、隐私说明版本、客服热线。
- 首页、在线缴费、在线沟通、评估报告、帮助中心在接口失败时可展示错误提示和重试入口。
- 健康档案、就医记录、评估报告已开启敏感信息访问校验。

## 3. 联调回归
- 执行家属账号与商城回归：
  - `./scripts/family_account_mall_regression.sh`
- 执行家属链路运维巡检：
  - `./scripts/family_ops_health_check.sh`
- 人工补充验证：
  - 注册 / 密码登录 / 找回密码
  - 绑定老人 / 多老人切换
  - 首页消息与周报
  - 评估报告 PDF 打开
  - 微信充值、查单、异常订单查看
  - 沟通消息、语音上传、探视预约
  - 通知绑定、通知设置、能力状态
  - 独立安全密码设置与验证

## 4. 真机与提审
- iOS 微信真机通过。
- Android 微信真机通过。
- 弱网、支付取消、上传失败、登录过期、首次拒绝授权后重试均验证通过。
- 微信提审资料已准备：
  - 测试账号
  - 测试老人数据
  - 支付测试说明
  - 隐私收集说明
  - 主要功能路径说明

## 5. 上线后首周巡检
- 每日巡检短信、通知、支付、上传失败数。
- 每日查看 `/api/admin/family/ops/health`。
- 若微信支付或通知异常明显上升，优先降级高风险链路并同步客服。
