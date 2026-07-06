# 家属端订阅消息推送链路 · 端到端验证

家属端微信订阅消息的完整链路：

```
小程序关键动作(缴费/探视/首页)
  → wx.requestSubscribeMessage 收集用户订阅授权
  → 业务发生时后端 FamilyWechatNotifyService.notifyFamily(command)
  → 换取 access_token → 调用 subscribe/send → 落库 family_notify_log(SUCCESS/FAILED/SKIPPED)
  → 失败进入 FamilyWechatNotifyRetryScheduler 定时重试
```

## 一、前端订阅授权采集点

| 场景 | 模板 key | 触发位置 |
| --- | --- | --- |
| 缴费/充值到账 | `familyPayment` | `pages/payment/index.js` 用户确认充值时 |
| 探视预约结果 | `familyVisit` | `pages/communication/index.js` 提交探视预约时 |
| 通用/健康预警 | `familyGeneral`/`familyHealth` | `pages/home/index.js` 首页每天最多一次 |

模板 ID 在 `miniprogram/app.js` 的 `globalData.subscribeTemplates` 配置。
**留空时 `utils/subscribe.js` 会静默跳过**，不影响业务；上线前需在微信公众平台
（小程序后台 → 功能 → 订阅消息）申请一次性模板并回填。

## 二、后端配置

`app.family.wechat-notify`（见 `application.yml` / `application-prod.yml`）：

- `enabled=true` 打开真实推送
- `app-id` / `app-secret`：小程序凭据（可复用 `app.family.mini-app` / `wechat-pay`）
- `template-id`：与前端 `subscribeTemplates` 对应的模板 ID
- `data-template-json`：模板字段占位（默认 `thing1/thing2/time3`）
- `max-retry` / `retry-interval-minutes` / `retry-cron`：失败重试策略

家属需先绑定 openId：登录后 `app.js#ensureWechatNotifyBind` 自动完成，无 openId 时通知记为 `SKIPPED`。

## 三、一键端到端自测

```bash
# 为指定家属真实投递一条测试订阅消息，并打印投递结果
FAMILY_USER_ID=123 ./scripts/family_subscribe_push_e2e.sh
```

底层接口：`POST /api/admin/family/ops/test-notify`（需 FINANCE_MINISTER/DIRECTOR/SYS_ADMIN/ADMIN 角色）

请求体：

```json
{ "familyUserId": 123, "elderId": 1, "eventType": "GENERAL", "level": "normal",
  "title": "推送链路自测", "content": "测试内容", "pagePath": "pages/messages/index" }
```

返回体 `summary` 字段直接给出结论：

- `boundOpenId=false`：家属未绑定 openId，通知被跳过 → 让家属在小程序登录一次
- `status=SUCCESS`：链路打通，微信客户端应收到消息
- `status=FAILED`：查 `lastError`，核对模板 ID / access_token / openId
- `status=SKIPPED`：家属关闭了该类型提醒（见通知设置）

## 四、上线前检查清单

- [ ] `app.family.wechat-notify.enabled=true` 且 app-id/app-secret/template-id 已配置
- [ ] `app.js#subscribeTemplates` 中对应 key 已填入真实模板 ID
- [ ] 用真实家属账号执行 `family_subscribe_push_e2e.sh` 返回 `SUCCESS`
- [ ] 微信客户端确认收到订阅消息，点击可跳转到对应页面
- [ ] 巡检 `/api/admin/family/ops/health` 中 notify 失败/待重试数为 0
