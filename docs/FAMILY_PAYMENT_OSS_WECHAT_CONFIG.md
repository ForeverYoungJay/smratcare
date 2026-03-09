# 家属端支付与报告文件配置说明

## 1) 报告 PDF -> OSS

### 后端开关
- `app.file-storage.provider=oss`

### 必填配置
- `app.file-storage.oss.endpoint`
- `app.file-storage.oss.bucket`
- `app.file-storage.oss.access-key-id`
- `app.file-storage.oss.access-key-secret`

### 推荐配置
- `app.file-storage.public-base-url`：CDN 或 OSS 自定义域名（用于返回家属端可直接访问的 PDF URL）
- `app.file-storage.oss.base-path`：按业务隔离目录前缀，默认 `smartcare`

### 业务链路
1. 管理端上传评估报告：`POST /api/assessment/records/{id}/report-file`
2. 后端存储 `report_file_url/report_file_name`
3. 家属聚合查询：`GET /api/family/assessment-reports` 返回真实 URL

---

## 2) 充值 -> 微信支付回调到账

### 家属端支付主链路
1. 家属端预下单：`POST /api/family/payment/wechat/prepay`
2. 小程序拉起 `wx.requestPayment`
3. 微信回调：`POST /api/family/payment/wechat/notify`
4. 家属端查单：`GET /api/family/payment/recharge-orders/{outTradeNo}`

### 必填配置（生产）
- `app.family.wechat-pay.enabled=true`
- `app.family.wechat-pay.app-id`
- `app.family.wechat-pay.app-secret`
- `app.family.wechat-pay.merchant-id`
- `app.family.wechat-pay.merchant-serial-no`
- `app.family.wechat-pay.private-key-pem`
- `app.family.wechat-pay.api-v3-key`
- `app.family.wechat-pay.notify-url`（必须是公网 HTTPS 回调地址）

### 验签与平台证书
- `app.family.wechat-pay.skip-notify-signature-verify`
  - 开发建议：`true`（便于本地联调）
  - 生产建议：`false`
- `app.family.wechat-pay.certificates-url`：默认 `https://api.mch.weixin.qq.com/v3/certificates`
- 可选固定证书模式：
  - `app.family.wechat-pay.platform-serial-no`
  - `app.family.wechat-pay.platform-certificate-pem`
- `app.family.wechat-pay.notify-allowed-skew-seconds`：回调时间戳允许偏移，默认 `300`

### 幂等保障
- 回调到账前检查 `elder_account_log(source_type=FAMILY_WECHAT_RECHARGE, source_id=orderId)`，避免重复加款。
- 订单状态采用“先原子抢占为 PAID 再记账”的处理，降低高并发重复入账风险。
- 回调验签通过后执行“防重放指纹校验”（`timestamp + nonce + signature + payloadDigest`），
  在 15 分钟时间窗内重复通知直接幂等返回成功，避免重复处理。

### 自动关单与异常跟进
- `app.family.recharge.auto-close-enabled`：是否开启预下单超时自动关单
- `app.family.recharge.auto-close-cron`：自动关单定时 cron（默认每 5 分钟）
- `app.family.recharge.prepay-timeout-minutes`：预下单超时时间（默认 30 分钟）
- `app.family.recharge.auto-close-batch-size`：每轮处理上限（默认 200）
- `app.family.recharge.create-abnormal-todo`：关闭/失败订单是否自动创建 OA 跟进待办

实现说明：
- 任务组件：`FamilyRechargeOrderScheduler`
- 处理逻辑：先查微信状态，再对长期 `PREPAY_CREATED` 订单自动关闭
- 异常派单：生成 `oa_todo`，标题 `充值异常跟进 - 老人姓名`

---

## 3) 旧接口下线配置

- `app.family.legacy-api-enabled`
- `app.family.legacy-api-sunset-date`

说明：旧接口 `bindElder/my-elders` 到期后自动 `410 Gone`，详细流程见 `docs/FAMILY_LEGACY_API_SUNSET_PLAN.md`。

---

## 4) 短信验证码真实化（登录 + 敏感信息二次验证）

### 对外接口
- `POST /api/auth/family/sms-code/send`
- `POST /api/auth/family/sms-code/verify`
- `POST /api/auth/family/login`（已切换为真实 OTP 校验）
- `POST /api/family/security/sms-code/send`
- `POST /api/family/security/sms-code/verify`

### 核心配置
- `app.family.sms-code.enabled`
- `app.family.sms-code.provider`：`mock` / `aliyun`
- `app.family.sms-code.expire-seconds`
- `app.family.sms-code.cooldown-seconds`
- `app.family.sms-code.daily-limit`
- `app.family.sms-code.max-attempts`
- `app.family.sms-code.code-salt`

### 阿里云短信（provider=aliyun）必填
- `app.family.sms-code.sign-name`
- `app.family.sms-code.login-template-code`
- `app.family.sms-code.aliyun.access-key-id`
- `app.family.sms-code.aliyun.access-key-secret`
- 可选：`security-template-code/default-template-code/region-id/endpoint`

### 数据留痕
- 表：`family_sms_code_log`
- 记录发送通道、过期时间、失败次数、校验状态、通道回执，便于审计与风控。

---

## 5) 微信通知真实投递（家属服务通知）

### 触发场景（已接入）
- 微信充值到账（`PAYMENT_RECHARGE_SUCCESS`）
- 充值异常/关闭/失败（`PAYMENT_RECHARGE_ABNORMAL`）
- 语音留言已转交（`COMMUNICATION_VOICE_SENT`）

### openId 绑定（投递前置）
- `POST /api/family/wechat/notify/bind-openid`
- 请求体支持：
  - `loginCode`：小程序 `wx.login` 返回 code（推荐）
  - `openId`：已拿到 openId 时可直接提交
- 建议在家属登录成功后立即调用一次，避免通知因“未绑定 openId”被跳过。

### 核心配置
- `app.family.wechat-notify.enabled`
- `app.family.wechat-notify.app-id`（为空时回退 `wechat-pay.app-id`）
- `app.family.wechat-notify.app-secret`（为空时回退 `wechat-pay.app-secret`）
- `app.family.wechat-notify.template-id`
- `app.family.wechat-notify.page`
- `app.family.wechat-notify.data-template-json`
- `app.family.wechat-notify.max-retry`
- `app.family.wechat-notify.retry-interval-minutes`
- `app.family.wechat-notify.retry-cron`

### 投递与重试
- 表：`family_notify_log`
- 定时任务：`FamilyWechatNotifyRetryScheduler`
- 失败后按 `max-retry` 与 `retry-interval-minutes` 自动重试。

---

## 6) 家属链路能力状态总览（新增）

### 对外接口
- `GET /api/family/capabilities/status`

### 用途
- 聚合展示短信验证码、微信通知绑定、微信支付开关、独立密码校验、旧接口下线进度。
- 推荐小程序“我的-能力状态”页面作为运营巡检入口，避免手工逐项排查配置。

---

## 7) 管理员链路运维巡检（新增）

### 对外接口
- `GET /api/admin/family/ops/health?hours=24`

### 返回内容
- 短信验证码：`SENT/VERIFIED/USED/EXPIRED/FAILED` 统计
- 微信通知：`SUCCESS/FAILED/PENDING/SKIPPED/pendingRetry` 统计
- 微信充值：`INIT/PREPAY_CREATED/PAID/CLOSED/FAILED` 统计及金额汇总
- Top 异常原因：按来源（`SMS/WECHAT_NOTIFY/WECHAT_PAY`）聚合

### 建议巡检方式
- 本地脚本：`./scripts/family_ops_health_check.sh`
- 环境变量可覆盖：
  - `BASE_URL`
  - `ADMIN_USERNAME`
  - `ADMIN_PASSWORD`
  - `WINDOW_HOURS`
