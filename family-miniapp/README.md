# SmartCare 微信家属端小程序

本目录是独立于 `admin-web` 的微信小程序工程，面向养老机构家属端。

## 一、已完成能力（完整版）

### 1) 登录与绑定
- 家属首次注册（手机号 + 真实短信验证码 + 设置密码）
- 家属登录（手机号 + 密码），找回密码走短信验证码
- 绑定老人（支持扫码占位入口 + 手工身份证绑定）
- 多老人切换（首页滑卡 + 顶部选择器）

### 2) 首页（完整信息架构）
- 在住老人核心卡片
  - 状态标签、护理等级、入住天数、特别关注标签、最近动态
  - 快捷操作：健康档案、在线沟通、探视预约
- 今日关键信息三卡
  - 健康摘要（4 指标 + 趋势提示 + 建议）
  - 今日日程（时间轴 + 状态 + 执行人）
  - 今日膳食（饮食评价 + 标签 + 三餐 + 进食情况）
- 消息提醒
  - 紧急/二级/三级分层消息
  - 消息详情页含事件进展时间线
  - 消息中心支持“全部已读”快捷处理
  - 预警中心支持未读分层统计、类型统计、紧急事件聚合
- 常用功能入口
  - 覆盖健康、就医、评估、缴费、服务、动态、亲情互动等模块
  - 新增“家属周报”“预警中心”“待办中心”“支付保障”快捷入口

### 3) 老人动态与健康档案
- 护理日志
- 活动相册/视频（点赞互动）
- 活动相册评论互动（评论列表 + 发布评论）
- 膳食日历
- 外出记录
- 健康数据看板（7/30/90 天）
- 就医记录
- 评估报告中心（含 PDF 查看入口）
- 紧急联系人（一键拨号）
- 家属周报（周维度健康/护理/活动/沟通总结）
- 周报历史回看（近多周趋势摘要）

### 4) 我的服务
- 在线缴费（账单、明细、历史账单、自动扣费开关、在线充值）
- 自动扣费授权开关（首次开启需授权确认，后台定时结算）
- 微信支付充值闭环（预下单、拉起支付、订单轮询、回调到账）
- 支付保障中心（充值状态分布、待支付/异常单追踪、保障建议）
- 充值订单详情页（状态时间线 + 异常联系入口）
- 缴费页异常订单提醒（待支付/已关闭/失败一键查看）
- 服务增购商城（下单）
- 服务订单
- 在线沟通（图文 + 语音留言文件上传 + 探视预约，非实时音视频）
- 在线沟通模板（健康关注/账单咨询/视频预约等快捷消息模板）
- 满意度评价（评分 + 文本）
- 反馈闭环（评价/投诉双类型 + 反馈记录状态追踪）

### 5) 个人中心
- 我的家庭（绑定管理）
- 我的信息（联系方式编辑）
- 通知设置（健康/费用/紧急等）
- 能力状态（短信/通知/支付/安全/旧接口下线进度一屏可视）
- 隐私与安全说明（敏感信息二次验证策略）
- 帮助与反馈

### 6) 特色增强（额外优化）
- 亲情互动页面：语音留言、节日祝福模板、互动记录
- 首页“今日重点安排”卡片，主动降低家属焦虑
- 能力状态红点联动：首页快捷入口 + 我的页入口 + TabBar 同步显示待处理项
- 待办中心页面：消息/缴费/日程/安全待办统一聚合，支持一键跳转处理
- 待办中心动作闭环：支持“完成/稍后提醒2小时”，并在服务端持久化状态
- 预警中心页面：紧急事件聚合 + 分层统计 + 一键已读联动
- 消息中心筛选：支持“全部/未读/紧急/费用/健康”快速过滤
- 家属周报分享：支持一键复制摘要文案，便于家庭群同步
- 敏感信息访问验证：健康档案/就医记录/评估报告按安全设置触发二次验证码验证（10分钟内免重复）
- 独立密码验证：支持设置/校验敏感信息访问独立密码（与短信校验并行）
- 缴费体验增强：快捷金额、充值后余额预估、大额充值确认、订单状态筛选
- 充值回退治理：默认关闭“支付失败自动人工记账回退”，可由全局开关单独启用
- 沟通提效增强：按沟通对象+类型自动恢复草稿、内容字数计数
- 评估能力增强：家属端可触发 AI 中医体质/心血管评估生成并回看 PDF

## 二、工程结构

- `project.config.json`：微信开发者工具项目配置
- `miniprogram/app.*`：全局配置、Tab、全局样式
- `miniprogram/utils/request.js`：统一请求（含 GET 参数/超时处理）
- `miniprogram/utils/auth.js`：登录态缓存
- `miniprogram/services/family.js`：家属端统一服务层
- `miniprogram/mocks/family-app.js`：完整 mock 数据中心
- `miniprogram/pages/*`：业务页面

## 三、接口策略（真实 API + Mock 兜底）

已直接对接：
- `GET /api/auth/family/bootstrap`
- `POST /api/auth/family/sms-code/send`
- `POST /api/auth/family/sms-code/verify`
- `POST /api/auth/family/register`
- `POST /api/auth/family/password/reset`
- `POST /api/auth/family/login`

家属端聚合接口（本次补齐）：
- `GET /api/family/dashboard/home`
- `GET /api/family/dashboard/weekly-brief`
- `GET /api/family/dashboard/weekly-brief/history`
- `GET /api/family/messages/page`
- `GET /api/family/messages/page?level=&type=&unreadOnly=`（新增筛选参数）
- `GET /api/family/messages/summary`
- `GET /api/family/messages/{id}`
- `POST /api/family/messages/{id}/read`
- `POST /api/family/messages/read-all`
- `GET /api/family/health/trend`
- `GET /api/family/medical-records`
- `GET /api/family/assessment-reports`
- `GET /api/family/schedules/today`
- `GET /api/family/meals/calendar`
- `GET /api/family/care-logs`
- `GET /api/family/activity-albums`
- `POST /api/family/activity-albums/{id}/like`
- `GET /api/family/activity-albums/{id}/comments`
- `POST /api/family/activity-albums/{id}/comments`
- `GET /api/family/outing-records`
- `GET /api/family/emergency-contacts`
- `GET /api/family/payment/summary`
- `GET /api/family/payment/guard`
- `GET /api/family/payment/history`
- `POST /api/family/payment/recharge`
- `POST /api/family/payment/wechat/prepay`
- `POST /api/family/wechat/notify/bind-openid`
- `GET /api/family/payment/recharge-orders`
- `GET /api/family/payment/recharge-orders/{outTradeNo}`
- `PUT /api/family/payment/auto-pay`
- `GET /api/family/service/catalog`
- `GET /api/family/service/orders`
- `POST /api/family/service/orders`
- `GET /api/family/mall/products`
- `POST /api/family/mall/orders/preview`
- `POST /api/family/mall/orders/submit`
- `GET /api/family/mall/orders`
- `GET /api/family/mall/orders/{orderId}`
- `POST /api/family/mall/orders/{orderId}/cancel`
- `POST /api/family/mall/orders/{orderId}/refund`
- `POST /api/family/feedback`
- `GET /api/family/feedback/records`
- `POST /api/family/visit/video/book`
- `POST /api/family/bindings`
- `GET /api/family/bindings`
- `DELETE /api/family/bindings/{elderId}`
- `GET /api/family/profile`
- `PUT /api/family/profile`
- `GET /api/family/notification-settings`
- `PUT /api/family/notification-settings`
- `GET /api/family/capabilities/status`
- `GET /api/family/security-settings`
- `PUT /api/family/security-settings`
- `POST /api/family/security/sms-code/send`
- `POST /api/family/security/sms-code/verify`
- `POST /api/family/security/password/set`
- `POST /api/family/security/password/verify`
- `GET /api/family/affection/moments`
- `POST /api/family/affection/moments`
- `GET /api/family/affection/templates`
- `GET /api/family/communication/messages`
- `POST /api/family/communication/messages`
- `GET /api/family/communication/templates`
- `GET /api/family/help/faqs`
- `GET /api/family/todo-center`
- `POST /api/family/todo-center/action`
- `POST /api/family/assessment-reports/generate-ai`
- `POST /api/files/upload`（语音留言文件上传，`bizType=family-voice`）

支付回调与报告文件：
- `POST /api/family/payment/wechat/notify`（微信支付异步回调）
- `POST /api/assessment/records/{id}/report-file`（管理端上传评估报告 PDF，家属端通过 `assessment-reports` 获取真实 URL）

说明：
- 登录模式已调整为“密码登录”，短信验证码仅用于“首次注册/找回密码/安全短信场景”。
- 敏感操作默认走“登录密码二次验证”，设置独立安全密码后优先使用独立密码。
- `payment/auto-pay` 已升级为“按老人维度”配置，入参包含 `elderId`，避免多位老人场景下误开/误关自动扣费。
- `payment/recharge` 作为机构内部补账/人工充值通道保留，家属微信支付主链路使用 `wechat/prepay + notify + recharge-orders`。
- 沟通消息 `POST /api/family/communication/messages` 已支持语音附件字段：
  `mediaUrl/mediaName/mediaDurationSec/transcript`。
- 短信验证码与语音上传链路默认强依赖真实后端接口，不再自动降级到 mock，避免绕过安全校验。

## 四、运行方式

1. 打开微信开发者工具
2. 导入项目目录：`/Users/yangjiyi/Downloads/smratcare/family-miniapp`
3. 使用测试号 `appid`（当前配置 `touristappid`）
4. 编译运行

## 五、联调配置

- 修改 `miniprogram/app.js` 中 `globalData.baseUrl`
- 默认值：`http://localhost:8080`
- 本地调试默认开启 `globalData.localDevBypassLogin=true`（localhost 环境自动跳过登录）
- `globalData.allowManualRechargeFallback` 默认 `false`（建议仅测试环境临时开启）
