# 前端编码规范（管理端 admin-web）

> 来源：2026-07 全站员工视角测试与修复（十轮，55+ 项缺陷）。以下规范针对测试中反复出现、且已提供统一工具的问题类别，代码评审时按此清单检查。

## 1. 提交防重（必须）

**问题**：antd 弹窗的 `confirm-loading` 只在重渲染后禁用按钮，渲染前落地的连击会重复触发提交（实测曾在电子病历三连击产生 3 条重复记录）。

**规范**：所有提交处理函数第一行加重入守卫，或直接使用组合式：

```ts
// 方式一：守卫（存量代码已批量补齐 160+ 处）
async function submit() {
  if (saving.value) return
  saving.value = true
  try { ... } finally { saving.value = false }
}

// 方式二：新代码统一用组合式
import { useSubmitLock } from '@/composables/useSubmitLock'
const { locked: saving, run } = useSubmitLock()
async function submit() {
  await run(async () => { await api(...) })
}
```

## 2. 时间展示（必须）

**问题**：后端返回 ISO 字符串（`2026-07-07T16:56:13`）直出到表格，员工难读。

**规范**：列表/详情中的时间一律格式化为 `YYYY-MM-DD HH:mm`（纯日期 `YYYY-MM-DD`）：

```ts
function formatXxxTime(value?: string) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}
```

中文长格式场景（打印、驾驶舱）用 `utils/dateLocale.ts` 的 `formatChineseDateTime`。

## 3. 枚举展示（必须）

**问题**：`PENDING`、`ONLINE`、`SINGLE`、`LANDING` 等原始枚举直出界面。

**规范**：
- 营销域枚举统一走 `utils/marketingEnums.ts`（`CONTRACT_FLOW_STAGE_LABELS`、`marketingInfoSourceLabel` 等），禁止页面内私建重复映射。
- 其他域在页面内映射时必须兜底透传原值（历史数据可能已是中文）：`MAP[value] || value || '-'`。
- 新增枚举字段时，评审确认表格单元格、Tag、导出 CSV 三处都走同一映射函数。

## 4. 错误提示（必须）

- 后端新代码抛业务异常一律用中文文案；存量英文报文由 `GlobalExceptionHandler.localizeBusinessMessage`（后端）与 `request.ts` 翻译表（前端）双侧兜底。
- 调用方 `message.error(error?.message || '兜底文案')` 即可——`request.ts` 已把 `error.message` 重写为业务中文。
- 校验失败禁止静默 `return`，必须给 `message.warning` 或表单 inline 错误。

## 5. 表单与弹窗（必须）

- 实际必填的字段必须有红星：规则里加 `required: true`（自定义 validator 并存时同样要加，星号取自该标志）。
- 超长表单弹窗加 `centered` + `:body-style="{ maxHeight: 'calc(100vh - 220px)', overflowY: 'auto' }"`，保证提交按钮矮屏可达。
- 危险/不可逆操作（审批通过、删除、完工、批量处理）必须 `Modal.confirm`；驳回类需填原因的用应用内弹窗，禁止 `window.prompt`。
- 新增记录保存成功后，若列表默认筛选可能滤掉新记录（如按“今天”过滤而记录在明天），要把筛选切到该记录可见的范围。

## 6. 文案（建议）

- 界面文案（含装饰性眉标）一律中文；面向一线员工避免流程术语直译（如 “ABNORMAL/FOLLOWING”）。
- 空状态要指出下一步（“该房间暂无空闲床位，可更换房间或到床位管理新增/释放床位”），不要裸 “暂无数据”。

## 7. 回归基线（发布前必过）

```bash
cd admin-web
npm run test:unit          # 24 文件 / 63 用例
npm run check:navigation   # 0 unresolved
npm run build              # 已内置 --max-old-space-size=8192
```

CI（`.github/workflows/ci.yml`）已包含以上三项 + 后端 `mvn test`。

## 8. 测试环境数据基线

以下基础数据已在 QA 环境写入，重建库后需重新准备：
- 班次模板：早班（DAY 08:00-16:00）、晚班（NIGHT 16:00-23:59）——AI 排班依赖；
- 计费标准（orgId=1, 2026-07 起）：`BED_FEE_MONTHLY=1500`、`MEAL_FEE_MONTHLY=900`、护理费“一级护理”=2000/月——账单金额依赖；
- 问卷题库：SAT-Q1/Q2（单选计分）、SAT-Q3（文本）。

另：QA 库中存有 2 条主诉含“双击防重测试”的病历，为防重缺陷修复前的实测产物，病历按合规不可删除，请勿据此统计。
