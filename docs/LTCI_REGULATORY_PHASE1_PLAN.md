# 一期落地方案：长护险与监管对接（LTCI & Regulatory）

> 版本：v1.0　编制日期：2026-07-03　对应总体方案 §3.A
> Flyway 版本段：`V215 – V229`（当前基线 V214）
> 新增后端模块包：`com.zhiyangyun.care.ltci`（长护险与失能评估）、`com.zhiyangyun.care.govreport`（监管上报/医保对接）

---

## 1. 范围与目标

| 子域 | 目标 | 政策依据 |
|------|------|----------|
| 失能等级评估 | 内置国家标准量表，支持初评/复评/争议复核，输出失能等级 0–5 | 《长期护理失能等级评估标准（试行）》2021-08-03 |
| 长护险待遇与结算 | 参保登记、待遇资格、护理服务包/限额、按月结算与分摊、结算清单 | 《长期护理保险失能等级评估管理办法（试行）》2023-12 |
| 民政上报 | 对接"金民工程"全国养老服务信息系统，机构/床位/长者/服务数据上报，回执与重试 | 民政部全国养老服务信息系统 |
| 医保对接 | 结算清单/电子凭证接口适配层（配置化，先支持试点城市） | 各地医保平台 |

**非目标（一期不做）**：与具体某地医保生产环境的真连（仅做适配层+沙箱），跨机构长护险资金清分。

---

## 2. 失能等级评估（评估标准落地）

### 2.1 量表结构

国家标准量表 = 3 个一级指标、17 个二级指标，组合法判定等级：

- **日常生活活动能力（ADL）**：进食、洗澡、修饰、穿衣、大便控制、小便控制、如厕、床椅转移、平地行走、上下楼梯（10 项，Barthel 基础）。
- **认知能力**：时间/空间定向、人物定向、记忆（近事）。
- **感知觉与沟通**：意识水平、视力、听力、沟通/理解表达。

判定结果：`0 能力完好 / 1 轻度失能 / 2 中度失能 / 3 中度失能 / 4 重度失能 / 5 重度失能`。评分与组合规则做成**可配置模板**（`ltci_assess_template`），避免标准修订时改代码。

### 2.2 评估业务流程

`申请 → 受理 → 分配评估员 → 现场评估打分 → 系统组合判级 → 结果告知 → (争议) 复核 → 生效 → 到期复评`

状态机 `assess_status`：`APPLIED / ACCEPTED / ASSIGNED / SCORING / JUDGED / NOTIFIED / DISPUTED / REVIEWING / EFFECTIVE / EXPIRED / CANCELLED`。

---

## 3. 数据模型（表设计）

> 统一含 `id BIGINT PK`、`tenant_id`、`org_id`、`create_time`、`update_time`、`is_deleted`，唯一键含 `is_deleted`。以下仅列业务字段要点。

### 3.1 失能评估（`V215`, `V216`）

**`ltci_assess_template`（评估模板/组合规则）**
`template_code`、`template_name`、`standard_version`（如 `NATIONAL_2021`）、`indicators_json`（一级/二级指标与分值）、`combine_rule_json`（组合判级规则）、`status`。

**`ltci_assess_apply`（评估申请）**
`elder_id`、`apply_type`（`FIRST` 初评 / `REVIEW` 复评 / `DISPUTE` 争议复核）、`apply_source`、`applicant_name`、`applicant_phone`、`assess_status`、`accepted_by`、`accepted_at`。

**`ltci_assessment`（评估记录）**
`apply_id`、`elder_id`、`template_id`、`assessor_id`、`assess_date`、`adl_score`、`cognitive_score`、`perception_score`、`total_score`、`disability_level`（0–5）、`level_label`、`answers_json`（逐项作答）、`effective_start`、`effective_end`、`assess_status`、`review_of_id`（复核指向原记录）。

**`ltci_assessor`（评估员）**
`staff_id`、`name`、`cert_no`（评估员资质证号）、`cert_expire`、`org_belong`（评估机构）、`status`。

### 3.2 长护险待遇与结算（`V217`–`V220`）

**`ltci_insured`（参保登记）**
`elder_id`、`insured_no`（长护险参保号）、`id_card`（脱敏存储）、`city_code`、`insure_status`、`start_date`、`end_date`。

**`ltci_benefit`（待遇资格）**
`insured_id`、`elder_id`、`disability_level`、`benefit_type`（`INSTITUTION` 机构护理 / `HOME` 居家 / `DEVICE` 辅具）、`daily_quota`（日限额，分）、`pay_ratio`（统筹支付比例）、`valid_start`、`valid_end`、`assessment_id`。

**`ltci_service_package`（护理服务包）**
`package_code`、`package_name`、`level_scope`（适用失能等级）、`items_json`（服务项目与频次）、`price`、`fund_covered`（是否基金覆盖）。

**`ltci_service_record`（护理服务记录）**
`elder_id`、`benefit_id`、`package_id`、`service_date`、`item_code`、`quantity`、`operator_id`、`fee`（分）、`sign_url`（长者/家属签字或影像佐证）。

**`ltci_settlement`（月度结算单）**
`elder_id`、`benefit_id`、`settle_month`（YYYYMM）、`total_fee`、`fund_pay`（统筹支付）、`self_pay`（个人自付）、`over_quota`（超限额自付）、`settle_status`（`DRAFT/SUBMITTED/SETTLED/REJECTED`）、`settle_no`、`detail_json`。

### 3.3 监管上报与医保对接（`V221`–`V224`）

**`gov_report_task`（上报任务）**
`report_type`（`ORG_INFO`/`BED`/`ELDER`/`SERVICE`/`LTCI_SETTLE`）、`channel`（`MZ_JINMIN` 民政金民 / `YB_MEDICAL` 医保）、`period`（如 `2026Q2`）、`task_status`（`PENDING/BUILDING/SENT/ACKED/FAILED`）、`record_count`、`trigger_type`（`SCHEDULE/MANUAL/EVENT`）。

**`gov_report_snapshot`（报文快照）**
`task_id`、`payload_json`（上报报文全量快照，便于追溯）、`payload_hash`、`field_mapping_version`。

**`gov_report_receipt`（回执）**
`task_id`、`receipt_code`、`receipt_status`、`error_detail`、`received_at`、`retry_count`。

**`gov_channel_config`（对接渠道配置）**
`channel`、`city_code`、`endpoint`、`app_id`、`secret_ref`（密钥引用，不明文）、`field_mapping_json`（本地字段→上报字段映射）、`enabled`。适配器模式的配置载体。

---

## 4. 后端接口清单

统一 `Result<T>`，管理端前缀 `/api/ltci`、`/api/govreport`，统计 `/stats/ltci`。

### 4.1 失能评估

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/ltci/templates/page` | 评估模板分页 | MEDICAL/NURSING_MINISTER+ |
| POST | `/api/ltci/templates` | 新建/编辑模板 | MEDICAL_MINISTER, DIRECTOR |
| POST | `/api/ltci/applies` | 提交评估申请 | NURSING_EMPLOYEE+ |
| POST | `/api/ltci/applies/{id}/accept` | 受理并分配评估员 | *_MINISTER |
| POST | `/api/ltci/assessments/score` | 提交逐项打分，系统组合判级 | 评估员（`LTCI_ASSESS`） |
| POST | `/api/ltci/assessments/{id}/notify` | 结果告知 | *_MINISTER |
| POST | `/api/ltci/assessments/{id}/dispute` | 发起争议复核 | NURSING_EMPLOYEE+ |
| GET | `/api/ltci/assessments/page` | 评估记录分页/筛选 | MEDICAL/NURSING+ |
| GET | `/api/ltci/elders/{elderId}/level` | 查询长者当前生效失能等级 | STAFF+ |

**打分接口判级逻辑**：`total_score = adl + cognitive + perception`，按 `combine_rule_json` 组合表映射 `disability_level`；结果写 `ltci_assessment` 并置 `assess_status=JUDGED`。判级为纯函数 `LtciGradingService.judge(answers, template)`，便于单测覆盖国家标准样例。

### 4.2 长护险待遇与结算

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/ltci/insured` | 参保登记 | FINANCE/NURSING_MINISTER |
| POST | `/api/ltci/benefits` | 依据评估等级核定待遇 | *_MINISTER |
| GET | `/api/ltci/packages` | 护理服务包列表 | STAFF+ |
| POST | `/api/ltci/service-records` | 录入护理服务记录 | NURSING_EMPLOYEE+ |
| POST | `/api/ltci/settlements/generate` | 生成指定月度结算单 | FINANCE_MINISTER |
| GET | `/api/ltci/settlements/page` | 结算单分页 | FINANCE+ |
| POST | `/api/ltci/settlements/{id}/submit` | 提交结算（触发医保上报任务） | FINANCE_MINISTER |

**结算算法**：按 `ltci_service_record` 汇总当月 `total_fee`；`fund_pay = min(total_fee, daily_quota×服务天数) × pay_ratio`；`self_pay = total_fee − fund_pay`；`over_quota` 单列。定时任务 `LtciSettlementScheduler` 每月 1 日生成上月草稿。

### 4.3 监管上报与医保对接

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/govreport/tasks/build` | 按类型+周期构建上报报文 | `GOV_REPORT` |
| POST | `/api/govreport/tasks/{id}/send` | 发送到渠道（民政/医保适配器） | `GOV_REPORT` |
| GET | `/api/govreport/tasks/page` | 上报任务分页+状态 | DIRECTOR, *_MINISTER |
| GET | `/api/govreport/tasks/{id}/snapshot` | 查看报文快照 | `GOV_REPORT` |
| POST | `/api/govreport/receipts/callback` | 渠道回执回调（可匿名+签名校验） | 白名单/签名 |
| GET | `/api/govreport/channels` | 渠道配置列表 | SYS_ADMIN, DIRECTOR |

**适配器设计**：`ReportChannelAdapter` 接口 + `JinminMzAdapter`、`MedicalYbAdapter` 实现，通过 `gov_channel_config.field_mapping_json` 做本地→上报字段映射；`ReportRetryScheduler` 对 `FAILED/未回执` 任务按退避重试（参考现有 `FamilyWechatNotifyRetryScheduler`）。

---

## 5. Flyway 迁移草案（V215–V224）

| 版本 | 文件名 | 内容 |
|------|--------|------|
| V215 | `V215__ltci_assess_template.sql` | 评估模板表 + 内置国家标准模板 seed |
| V216 | `V216__ltci_assessment_core.sql` | 申请/评估记录/评估员表 |
| V217 | `V217__ltci_insured_benefit.sql` | 参保登记 + 待遇资格 |
| V218 | `V218__ltci_service_package.sql` | 护理服务包 + seed |
| V219 | `V219__ltci_service_record.sql` | 护理服务记录 |
| V220 | `V220__ltci_settlement.sql` | 月度结算单 |
| V221 | `V221__gov_report_task.sql` | 上报任务 |
| V222 | `V222__gov_report_snapshot_receipt.sql` | 报文快照 + 回执 |
| V223 | `V223__gov_channel_config.sql` | 渠道配置 |
| V224 | `V224__ltci_rbac_permissions.sql` | 新增权限点 `LTCI_ASSESS`/`GOV_REPORT` 及角色映射 |

> 每个 CREATE 均 `IF NOT EXISTS`，索引带 `org_id` 前缀，风格对齐 `V205__smart_device_alert_center.sql`。

---

## 6. 前端页面（admin-web）

新增 `src/views/ltci/` 与 `src/views/govreport/`，在 `router/routes.ts` + `routeAccess.ts` 注册，一级菜单建议挂"医养管理"或新增"长护险/监管"：

- **失能评估**：申请列表、评估打分表单（按模板动态渲染 17 项）、评估结果与等级、复核流程。
- **长护险待遇**：参保登记、待遇核定、服务包配置、服务记录录入。
- **结算管理**：月度结算单列表、明细下钻、生成/提交、导出结算清单。
- **监管上报**：上报任务看板（按渠道/周期/状态）、报文快照查看、回执与重试、渠道配置（SYS_ADMIN）。

失能等级、结算状态用统一 Tag 颜色；敏感字段（身份证/长护险号）默认脱敏，导出需二次确认并留痕（对齐 §F 合规）。

---

## 7. 验收清单

- [ ] 国家标准量表 seed 完整（3 一级 + 17 二级），`LtciGradingService.judge` 对标准样例判级正确（单测覆盖 0–5 各档）。
- [ ] 评估流程状态机可走通：申请→受理→打分→判级→告知→争议复核→生效→到期复评。
- [ ] 待遇核定随失能等级联动；结算算法（基金支付/个人自付/超限额）与手工核算一致。
- [ ] 月度结算定时任务生成上月草稿，提交后触发医保上报任务。
- [ ] 民政/医保上报任务可构建、发送（沙箱）、落快照、收回执、失败重试。
- [ ] 渠道配置密钥不明文；字段映射改配置不改代码。
- [ ] 新权限点 `LTCI_ASSESS`/`GOV_REPORT` 生效，越权返回 403（纳入 `rbac_regression_smoke.sh`）。
- [ ] Flyway `V215–V224` 无版本冲突（`check_flyway_versions.sh`），全量迁移在干净库可执行。
- [ ] 敏感数据脱敏展示、导出留痕。
- [ ] 新增 Postman 集合 `postman_ltci_regulatory.collection.json` 回归通过。

---

## 8. 交付物与工时估算

| 交付物 | 说明 | 估算 |
|--------|------|------|
| Flyway V215–V224 | 10 个迁移 + seed | 3–4 人日 |
| 失能评估后端 | 模板/申请/评估/判级/评估员 | 6–8 人日 |
| 长护险待遇结算后端 | 参保/待遇/服务包/记录/结算+定时 | 6–8 人日 |
| 监管上报后端 | 任务/快照/回执/适配器/重试 | 6–8 人日 |
| 前端页面 | 评估/待遇/结算/上报四大模块 | 8–10 人日 |
| 测试与回归 | 单测+Postman+RBAC 冒烟 | 4–5 人日 |
| **合计** | | **约 6–8 周（1–2 人）** |

---

## 9. 下一步

确认本方案后，建议从 **`V215` + `V216` + `LtciGradingService` 判级内核 + 对应单测**起步（评估是长护险一切下游的根），跑通"打分→判级"最小闭环再向待遇/结算/上报扩展。
