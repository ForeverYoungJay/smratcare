# 员工移动端 / 小程序补齐方案

## 背景

家属端小程序已经覆盖了信息查看、通知、缴费、沟通和服务反馈，但养老机构的一线执行仍然高度依赖手机操作：

- 护理员需要随身完成护理执行、补录体征、拍照留痕和交接班
- 医护需要随时处理查房、用药确认、异常巡检和医嘱跟进
- 后勤需要在现场完成维修派单、餐食配送、保洁巡检和回执关闭

如果这些能力继续只停留在后台网页，现场操作成本会明显偏高，也不利于闭环留痕。

## 现有落点

- 员工端已并入统一微信小程序工程：`family-miniapp`
- 员工端页面入口：`family-miniapp/miniprogram/pages/staff-home/index`
- 新增接口：`GET /api/operations/staff-mobile`
- 员工执行接口已补齐：待办、任务详情、完成回执、排班、交接、巡检扫码、异常上报
- 任务回执、交接记录与巡检扫码已新增持久化表，支持现场操作追溯
- 在管理驾驶舱能力图中新增一级域：`员工移动端 / 小程序`

## 核心角色

### 护理员

- 护理任务接单
- 现场执行与拍照
- 体征补录
- 异常上报
- 交接班备注

### 医生 / 医护

- 查房巡诊
- 用药确认
- 异常巡检处理
- 医嘱跟进

### 后勤人员

- 维修派单与到场确认
- 餐食配送与签收
- 保洁巡检
- 工单关闭

## 建议 MVP

1. 手机端统一登录和身份切换
2. 今日待办首页
3. 护理执行、巡检、用药确认、维修和配送的快速闭环
4. 照片、语音和备注回执，任务和异常事件均可留存语音证据
5. 待办推送与超时提醒

## 页面结构

- 登录页：家属 / 员工身份切换
- 员工首页：`pages/staff-home/index`，首屏作战视图聚合在岗状态、今日班次、待办数、超时待办、开放异常和最高优先级任务
- 员工待办：`pages/staff-tasks/index`，聚合护理、用药、巡检、维修、送餐专用工作台，并按任务模块自动跳转专用闭环页
- 任务详情：`pages/staff-task-detail/index`
- 护理执行：`pages/staff-care/index`
- 护理执行闭环：`pages/staff-care-execution/index`，床号/姓名核对、护理项目、皮肤状态、精神状态、风险观察、耗材使用、交接提示和拍照留痕
- 体征补录：`pages/staff-vitals/index`，现场采集血压、体温、心率、血氧、血糖等健康数据
- 医护工作：`pages/staff-medical/index`
- 医护闭环：`pages/staff-clinical/index`，用药三查七对、给药结果、不良反应记录，以及巡检到房查验、复测体征、风险等级和处理措施
- 后勤执行：`pages/staff-logistics/index`
- 维修处理：`pages/staff-repairs/index`，到场确认、扫码定位、故障类型、处理结果、用料、耗时和拍照验收
- 送餐签收：`pages/staff-meals/index`，餐别/姓名核对、禁忌标签确认、签收、餐量反馈、扫码和拍照留痕
- 物资申领：`pages/staff-material/index`，护理耗材、餐饮物资、维修备件和保洁补给移动端申请
- 员工我的：`pages/staff-profile/index`
- 我的排班：`pages/staff-schedule/index`，支持上班/下班、午休、外出移动打卡
- 我的考勤：`pages/staff-attendance/index`，查看本月出勤、请假、迟到、异常和每日打卡记录
- 请假调班：`pages/staff-approval/index`，移动端发起请假/调班审批并进入 OA 审批流
- 我的待办：`pages/staff-todo/index`，聚合 OA 提醒、审批流、换班确认和值班通知
- 员工通讯录：`pages/staff-contacts/index`，搜索员工并一键拨号协同
- 工作日报：`pages/staff-report/index`，填写当班摘要、完成事项、风险问题和下一步计划
- 通知公告：`pages/staff-notices/index`，阅读制度通知、培训安排和运营公告
- 建议反馈：`pages/staff-suggestions/index`，提交一线优化建议并查看处理状态
- 班次交接：`pages/staff-handover/index`
- 异常上报：`pages/staff-incident/index`
- 异常记录：`pages/staff-incidents/index`
- 巡检扫码：`pages/staff-patrol/index`
- 执行记录：`pages/staff-receipts/index`

## 移动端接口

- `GET /api/operations/staff-mobile`：员工工作台概览
- `GET /api/operations/staff-mobile/tasks?module=`：员工待办列表，支持 `CARE/MEDICATION/INSPECTION/LOGISTICS/MEAL`
- `GET /api/operations/staff-mobile/tasks/{id}`：任务详情
- `POST /api/operations/staff-mobile/tasks/{id}/complete`：提交现场执行回执，护理执行复用该接口保存护理项目、皮肤/精神状态、风险观察、耗材、交接提示、检查项、扫码和照片；医护闭环复用该接口保存用药结果/给药方式/不良反应，或巡检结论/风险等级/体征摘要/处理措施；维修处理复用该接口保存故障类型、处理结果、用料、耗时、检查项、扫码和照片；送餐签收复用该接口保存餐量、签收人、检查项、扫码和照片
- `GET /api/operations/staff-mobile/receipts?module=`：本人执行回执记录，支持按模块筛选
- `GET /api/health/data/page`：员工查看近期体征补录记录，支持关键字和异常状态筛选
- `POST /api/health/data`：员工现场补录体征数据，支持 `BP/HR/TEMP/SPO2/GLUCOSE/WEIGHT/OTHER`
- `GET /api/operations/staff-mobile/schedule`：我的排班
- `GET /api/attendance/overview`：今日考勤状态
- `GET /api/attendance/overview?month=`：月度考勤概览与每日记录
- `POST /api/attendance/punch?action=`：移动端打卡，支持 `IN/OUT/START_LUNCH/END_LUNCH/START_OUTING/END_OUTING`
- `GET /api/oa/approval/page`：员工本人审批记录
- `POST /api/oa/approval`：员工请假/调班审批申请
- `POST /api/oa/approval`：员工物资申领，使用 `approvalType=MATERIAL_APPLY` 进入仓库审核和出库确认流程
- `GET /api/oa/todo/summary?mineOnly=true`：员工本人待办统计
- `GET /api/oa/todo/page?mineOnly=true`：员工本人待办列表
- `PUT /api/oa/todo/{id}/done`：员工完成普通待办
- `GET /api/admin/staff/options`：员工通讯录搜索，返回姓名、工号、手机号、部门和角色
- `GET /api/oa/report/summary?mineOnly=true&reportType=DAY`：员工本人日报统计
- `GET /api/oa/report/page?mineOnly=true&reportType=DAY`：员工本人日报列表
- `POST /api/oa/report`：保存草稿或直接提交员工日报
- `PUT /api/oa/report/{id}/submit`：提交日报草稿
- `GET /api/oa/notice/page?status=PUBLISHED`：员工通知公告列表
- `GET /api/oa/notice/{id}`：员工通知公告详情
- `GET /api/oa/suggestion/page`：员工建议反馈记录，移动端按提交人关键字展示本人记录
- `POST /api/oa/suggestion`：员工提交流程、物资、餐食、排班等一线建议
- `GET /api/operations/staff-mobile/handovers`：交接记录
- `POST /api/operations/staff-mobile/handovers`：提交交接
- `GET /api/operations/staff-mobile/patrol-points`：巡检点
- `POST /api/operations/staff-mobile/patrol-scan`：提交巡检扫码结果
- `POST /api/operations/staff-mobile/incidents`：异常事件上报
- `GET /api/operations/staff-mobile/incidents?status=&level=`：本人异常上报记录，支持按状态和等级筛选

## 数据留痕

- `operations_staff_handover`：员工移动端交接记录
- `operations_staff_patrol_scan`：员工移动端巡检扫码记录
- `operations_staff_task_receipt`：员工移动端任务执行回执，保存备注、扫码、检查项、照片 URL 和语音证据 URL
- 回执记录同时保存任务标题、服务对象和位置快照，避免任务完成后上下文丢失
- 异常上报保存位置、扫码结果、现场照片 URL 和语音说明，便于安全事件复盘
- 员工端可查看本人异常记录，追踪待处理、处理中和已关闭状态
- 主库迁移：`V210__staff_mobile_handover_patrol.sql`、`V211__staff_mobile_task_receipt.sql`、`V212__staff_mobile_task_receipt_snapshot.sql`、`V213__incident_mobile_evidence_fields.sql`、`V214__staff_mobile_voice_evidence.sql`
- H2 测试迁移：`V24__staff_mobile_handover_patrol.sql`、`V25__staff_mobile_task_receipt.sql`、`V26__staff_mobile_task_receipt_snapshot.sql`、`V27__incident_mobile_evidence_fields.sql`、`V28__staff_mobile_voice_evidence.sql`

## 验收标准

- 能在同一个微信小程序登录页选择“员工”身份并进入员工工作台
- 员工首页首屏能看到今日班次、在岗状态、开放待办、超时待办、异常数量和最高优先级任务，并可一键跳转处理
- 页面能明确看到护理、医护、后勤三类场景
- 一线高频任务能对应到现有业务路由
- 员工待办中心能按护理、用药、巡检、后勤、送餐筛选任务，并从任务行直接进入对应专用闭环页
- 小程序员工端服务层真实接口优先，本地开发保留 mock 兜底
- 员工可在移动端补录血压、体温、心率、血氧、血糖等体征数据，并对异常值做现场标记
- 护理、用药、巡检、维修、送餐任务能提交现场回执；员工端可查看本人执行记录；任务回执和异常上报均支持照片与语音证据；异常上报能写入事件表
- 护理任务可在专用页面完成床号/姓名核对、护理项目、皮肤状态、精神状态、风险观察、耗材使用、交接提示、扫码和拍照留痕
- 用药任务可在专用页面完成老人身份核对、药品剂量核对、给药结果、给药方式、不良反应/异常记录、扫码和拍照留痕
- 巡检任务可在专用页面完成到房查验、体征复测、医生/主管同步、风险等级、处理措施、扫码和拍照留痕
- 维修任务可在专用页面完成到场确认、扫码定位、故障分类、处理结果、用料耗时、拍照验收和后续跟进
- 送餐任务可在专用页面完成餐别/姓名核对、禁忌标签确认、送达签收、餐量反馈、扫码和拍照留痕
- 请假、调班可在员工移动端发起，并复用后台 OA 审批链路
- 护理耗材、餐饮物资、维修备件和保洁补给可在员工移动端发起物资申领，并复用 OA 物资审批链路
- 员工可在移动端查看本人 OA 待办、换班和值班通知，并完成普通待办
- 员工可搜索院内同事并一键拨号，支撑现场协作
- 员工可在移动端提交工作日报，记录完成事项、风险和下一班计划
- 员工可阅读院内通知公告，确保制度、培训和运营消息触达
- 员工可提交一线建议反馈，并在移动端查看待处理、处理中和已处理状态
