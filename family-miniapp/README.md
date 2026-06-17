# SmartCare 微信一体小程序

本目录是独立于 `admin-web` 的微信小程序工程，同时承载家属端和员工端。

- 家属登录后进入原家属首页，查看长者动态、健康、缴费、沟通和服务。
- 员工登录后进入员工工作台，处理护理执行、查房巡检、用药确认、维修和餐食配送。

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

### 7) 员工端工作台
- 登录页支持“家属 / 员工”身份切换
- 员工账号密码登录复用后台员工登录接口
- 员工工作台：现场压力指数、任务分组、马上要处理
- 员工首页作战视图：首屏聚合在岗状态、今日班次、待办数、超时待办、开放异常和最高优先级任务
- 员工待办：护理、用药、巡检、后勤、送餐筛选，并聚合护理/医护/维修/送餐专用工作台入口
- 待办智能跳转：按任务模块自动进入护理执行、用药确认、巡检复核、维修处理或送餐签收专用闭环页
- 任务详情：现场回执、备注和完成动作
- 任务详情增强：检查项、扫码核验、拍照留痕、语音证据和现场备注
- 护理执行：护理任务列表
- 护理执行闭环：床号/姓名核对、护理项目、皮肤状态、精神状态、风险观察、耗材使用、交接提示和拍照留痕
- 体征补录：现场采集血压、体温、心率、血氧、血糖等数据并自动标记异常
- 医护工作：用药确认和异常巡检
- 医护闭环：用药三查七对、给药结果、不良反应记录；巡检到房查验、复测体征、风险等级和处理措施
- 后勤执行：维修工单和餐食配送
- 物资申领：护理耗材、餐饮物资、维修备件和保洁补给移动端申请，进入 OA 物资审批流
- 维修处理：到场确认、扫码定位、故障类型、处理结果、用料、耗时和拍照验收
- 送餐签收：餐别/姓名核对、禁忌标签确认、签收、餐量反馈、扫码和拍照留痕
- 我的排班：查看近期班次、时间和责任区域，并支持上班/下班、午休、外出移动打卡
- 我的考勤：查看本月出勤、请假、迟到、异常和每日打卡记录
- 请假调班：移动端发起请假/调班审批，进入后台 OA 审批流
- 我的待办：聚合 OA 提醒、审批待办、换班确认和值班通知
- 通讯录：搜索员工、查看角色/部门/工号并一键拨号
- 工作日报：填写当班摘要、完成事项、风险问题和下一班计划
- 通知公告：阅读院内制度通知、培训安排和运营公告
- 建议反馈：提交流程、物资、餐食、排班等一线优化建议并追踪处理状态
- 班次交接：查看重点事项并提交移动端交接补充
- 异常上报：跌倒、用药、设备、餐食等异常统一上报
- 异常上报增强：扫码定位、拍照上传、语音说明和现场说明一起留痕
- 异常记录：按处理状态和紧急程度查看本人上报事件
- 巡检扫码：巡房、药房、餐车和消防巡检点扫码核验
- 执行记录：按任务模块查看本人提交的回执、扫码、检查项、照片和语音证据
- 员工我的：员工资料、岗位角色和退出登录

## 二、工程结构

- `project.config.json`：微信开发者工具项目配置
- `miniprogram/app.*`：全局配置、Tab、全局样式
- `miniprogram/utils/request.js`：统一请求（含 GET 参数/超时处理）
- `miniprogram/utils/auth.js`：登录态缓存
- `miniprogram/services/family.js`：家属端统一服务层
- `miniprogram/services/staff.js`：员工端统一服务层
- `miniprogram/mocks/family-app.js`：完整 mock 数据中心
- `miniprogram/mocks/staff-app.js`：员工端 mock 数据中心
- `miniprogram/pages/*`：业务页面

## 三、接口策略（真实 API + Mock 兜底）

已直接对接：
- `GET /api/auth/family/bootstrap`
- `POST /api/auth/family/sms-code/send`
- `POST /api/auth/family/sms-code/verify`
- `POST /api/auth/family/register`
- `POST /api/auth/family/password/reset`
- `POST /api/auth/family/login`
- `POST /api/auth/login`（员工账号密码登录）
- `GET /api/auth/me`（员工信息）
- `GET /api/operations/staff-mobile`（员工移动端任务概览）
- `GET /api/operations/staff-mobile/tasks?module=`（员工待办）
- `GET /api/operations/staff-mobile/tasks/{id}`（员工任务详情）
- `POST /api/operations/staff-mobile/tasks/{id}/complete`（员工任务完成回执）
- `POST /api/operations/staff-mobile/tasks/{id}/complete`（护理执行复用员工任务回执，保存护理项目、皮肤/精神状态、风险观察、耗材、交接提示、检查项、扫码和照片）
- `POST /api/operations/staff-mobile/tasks/{id}/complete`（医护闭环复用员工任务回执，保存用药结果/给药方式/不良反应，或巡检结论/风险等级/体征摘要/处理措施）
- `POST /api/operations/staff-mobile/tasks/{id}/complete`（维修处理复用员工任务回执，保存故障类型、处理结果、用料、耗时、检查项、扫码和照片）
- `POST /api/operations/staff-mobile/tasks/{id}/complete`（送餐签收复用员工任务回执，保存餐量、签收人、检查项、扫码和照片）
- `GET /api/operations/staff-mobile/receipts?module=`（员工执行记录）
- `GET /api/health/data/page`（员工查看近期体征补录记录）
- `POST /api/health/data`（员工现场补录健康体征数据）
- `GET /api/operations/staff-mobile/schedule`（员工排班）
- `GET /api/attendance/overview`（员工今日考勤状态）
- `GET /api/attendance/overview?month=`（员工月度考勤概览）
- `POST /api/attendance/punch?action=`（员工移动端打卡）
- `GET /api/oa/approval/page`（员工本人审批记录）
- `POST /api/oa/approval`（员工请假/调班审批申请）
- `POST /api/oa/approval`（员工物资申领，`approvalType=MATERIAL_APPLY`）
- `GET /api/oa/todo/summary?mineOnly=true`（员工待办统计）
- `GET /api/oa/todo/page?mineOnly=true`（员工待办列表）
- `PUT /api/oa/todo/{id}/done`（员工完成普通待办）
- `GET /api/admin/staff/options`（员工通讯录搜索）
- `GET /api/oa/report/summary?mineOnly=true&reportType=DAY`（员工日报统计）
- `GET /api/oa/report/page?mineOnly=true&reportType=DAY`（员工日报列表）
- `POST /api/oa/report`（员工保存/提交日报）
- `PUT /api/oa/report/{id}/submit`（员工提交日报草稿）
- `GET /api/oa/notice/page?status=PUBLISHED`（员工通知公告列表）
- `GET /api/oa/notice/{id}`（员工通知公告详情）
- `GET /api/oa/suggestion/page`（员工建议反馈记录）
- `POST /api/oa/suggestion`（员工提交一线建议）
- `GET /api/operations/staff-mobile/handovers`（交接记录）
- `POST /api/operations/staff-mobile/handovers`（提交交接）
- `GET /api/operations/staff-mobile/patrol-points`（巡检点）
- `POST /api/operations/staff-mobile/patrol-scan`（巡检扫码回执）
- `POST /api/operations/staff-mobile/incidents`（异常上报）
- `GET /api/operations/staff-mobile/incidents?status=&level=`（异常记录）

员工端留痕表：
- `operations_staff_handover`：班次交接记录
- `operations_staff_patrol_scan`：巡检扫码记录
- `operations_staff_task_receipt`：任务执行回执，含任务快照、检查项、扫码、备注、照片 URL 和语音证据 URL

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
- 员工端当前真实接口优先；任务和异常照片/语音会先上传为文件 URL，再随回执/事件提交。任务回执会保存任务标题、服务对象、位置、检查项、扫码、照片 URL 和语音证据 URL，异常上报会保存位置、扫码、照片 URL 和语音说明，员工可回看本人异常记录，交接和巡检扫码也已落库留痕。员工体征补录复用健康数据接口，支持血压、心率、体温、血氧、血糖、体重等现场采集并自动标记异常；员工物资申领复用 OA `MATERIAL_APPLY` 审批链路，避免现场人员绕过仓库出库权限；员工建议反馈复用 OA 建议接口，便于把一线优化意见纳入后台处理闭环。本地开发环境保留 mock 兜底，方便没有后端或没有测试数据时继续演示。

## 四、运行方式

1. 打开微信开发者工具
2. 导入项目目录：`/Users/yangjiyi/Downloads/smratcare/family-miniapp`
3. 使用测试号 `appid`（当前配置 `touristappid`）
4. 编译运行

## 五、联调配置

- 修改 `miniprogram/app.js` 中 `globalData.baseUrl`
- 默认值：`http://localhost:8080`
- 小程序现已改为“生产默认关闭开发兜底”：
  - `globalData.useMockFallback=false`
  - `globalData.localDevBypassLogin=false`
  - `globalData.allowManualRechargeFallback=false`
- 如需本地联调显式开启开发能力，请在微信开发者工具 Storage 中写入：

```json
{
  "enableMockFallback": true,
  "enableLocalDevBypassLogin": true,
  "allowManualRechargeFallback": false,
  "baseUrlOverride": "http://localhost:8080"
}
```

- Storage Key：`smartcare_family_runtime_flags`
- `trial/release` 环境若仍使用 localhost、mock 或免登录开关，登录页会直接阻止继续使用，避免误提审或误上线。
