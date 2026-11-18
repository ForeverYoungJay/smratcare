import type { CenterTab } from '../../medical/center/centerTabs'
import type { BirthdayReminder } from '../../../types'

const P = '/oa/'

/** 行政管理 6 个主菜单中的「标签 + 原有页面」型页面（工作台独立） */
export const ADMIN_CENTERS: Record<string, { title: string; subTitle: string; tabs: CenterTab[] }> = {
  family: {
    title: '家属服务',
    subTitle: '家属服务申请、账号绑定与授权、反馈沟通统一受理；按事项转给责任部门办理',
    tabs: [
      { key: 'service', label: '家属服务台', path: `${P}family-service`, note: '同一申请保留稳定编号，允许转办但不丢失发起信息和沟通记录；院内备注默认不对家属可见。' },
      { key: 'bindings', label: '账号绑定与授权', path: `${P}family-users`, note: '亲属关系、紧急联系人、代理权和数据查看权限是不同概念，需分别核验；不能仅凭手机号授予健康、费用等敏感信息权限。', noteType: 'warning' },
      { key: 'feedback', label: '反馈与沟通', path: `${P}work-execution/family-communication`, note: '先确认事件是否影响长者安全；涉及医疗护理风险时立即通知对应部门，不在普通投诉队列中等待。', noteType: 'warning' }
    ]
  },
  collaboration: {
    title: '协同办公',
    subTitle: '跨部门任务与审批流程集中办理；同一事件使用同一任务编号',
    tabs: [
      { key: 'tasks', label: '任务管理', path: `${P}work-execution/task`, note: '行政负责协同与督办，护理、后勤、医务仍在本部门系统执行并回传结果，避免生成两份任务记录。' },
      { key: 'approvals', label: '审批办理', path: `${P}approval`, note: '审批节点由授权管理员配置；涉及采购和付款的流程需财务复核，申请人不得跳过必要审批节点。' },
      { key: 'flows', label: '跨部门协同流程', path: `${P}staff-collaboration` }
    ]
  },
  services: {
    title: '生活服务',
    subTitle: '保洁、维修等生活服务由行政受理、后勤执行；长者卡务与资金账户分离',
    tabs: [
      { key: 'maintenance', label: '维修工单', path: `${P}life/maintenance`, note: '行政负责受理、跟踪与回访，维修由后勤实际执行；维修员提交照片不等于验收，涉及安全的故障需通知主管。' },
      { key: 'cleaning', label: '房间保洁', path: `${P}life/room-cleaning` },
      { key: 'cards', label: '长者一卡通', path: `${P}card/account`, note: '一卡通标识与资金账户必须分离；余额、充值和退款由财务系统负责，卡务人员不直接调整账户资金。', noteType: 'warning' }
    ]
  },
  culture: {
    title: '活动与满意度',
    subTitle: '活动排期、报名签到与记录；生日关怀；满意度调查与统计',
    tabs: [
      { key: 'activities', label: '活动中心', path: `${P}activity-center/plan` },
      { key: 'records', label: '活动记录', path: `${P}activity-center/records`, note: '活动照片对外使用前需取得本人或授权人同意。' },
      { key: 'birthdays', label: '生日关怀', path: `${P}life/birthday`, note: '生日信息最小必要展示；生日通知按本人或授权范围发送，不公开完整生日与联系方式。' },
      { key: 'surveys', label: '满意度调查', path: `${P}activity-center/survey-manage` },
      { key: 'stats', label: '调查统计', path: `${P}activity-center/survey-stats`, note: '满意度需写明分母、评价量表和时间窗，未答复不计入满意。' }
    ]
  },
  library: {
    title: '制度知识与公告',
    subTitle: '制度文档、知识库、通知公告与对外官网内容；版本、审核与阅读确认可追溯',
    tabs: [
      { key: 'notices', label: '通知公告', path: `${P}notice`, note: '员工公告与家属通知须区分接收对象；不在公告中公开长者健康状况、身份证、手机号及财务信息。', noteType: 'warning' },
      { key: 'documents', label: '制度与文档', path: `${P}document`, note: '正式制度不可在原版本上静默覆盖，保留历史版本和审核信息。' },
      { key: 'knowledge', label: '知识库', path: `${P}knowledge` },
      { key: 'site', label: '官网与小程序内容', path: '/system/site-config', note: '对外发布的机构介绍、动态与预约留言：发布前核对内容，不公开长者照片、健康与身份信息；预约留言请及时转市场部跟进。', noteType: 'warning' }
    ]
  }
}

/** N 天内过生日的长者，按日期先后 */
export function upcomingBirthdays(list: BirthdayReminder[], days: number) {
  return list.filter((b) => b.daysUntil !== undefined && b.daysUntil >= 0 && b.daysUntil <= days)
    .sort((a, b) => Number(a.daysUntil) - Number(b.daysUntil))
}

/** 家属授权项：亲属关系、代理权、紧急联系人和数据查看权限分别核验 */
export const FAMILY_SCOPES = [
  { value: 'HEALTH', label: '健康信息' },
  { value: 'BILLING', label: '费用明细' },
  { value: 'PHOTO', label: '照片分享' },
  { value: 'PROXY', label: '代理办理' },
  { value: 'EMERGENCY', label: '紧急联系人' }
]

/** 授权概况；authScopes 为空是历史绑定，仍按原来全部可见 */
export function familyScopeSummary(authScopes: string | null | undefined, validUntil: string | null | undefined, today: string) {
  if (validUntil && validUntil < today) return { text: `授权已于 ${validUntil} 到期`, tone: 'danger' as const }
  if (authScopes == null) return { text: '历史绑定 · 未逐项核验（全部可见）', tone: 'warning' as const }
  const labels = FAMILY_SCOPES.filter((s) => authScopes.split(',').includes(s.value)).map((s) => s.label)
  const until = validUntil ? ` · 至 ${validUntil}` : ' · 长期'
  return { text: (labels.length ? labels.join('、') : '仅亲属关系（无数据权限）') + until, tone: 'info' as const }
}

/** 一卡通卡务：状态与可执行操作（与后端 CardLifecycle 一致） */
export const CARD_STATUS: Record<string, { label: string; color: string }> = {
  PENDING: { label: '已申领待激活', color: 'blue' },
  ACTIVE: { label: '正常', color: 'green' },
  LOST: { label: '已挂失', color: 'red' },
  FROZEN: { label: '冻结', color: 'orange' },
  REPLACED: { label: '已补卡作废', color: 'default' },
  CANCELLED: { label: '已注销', color: 'default' }
}
const CARD_ACTIONS = [
  { action: 'ACTIVATE', label: '激活', from: ['PENDING'] },
  { action: 'REPORT_LOSS', label: '挂失', from: ['ACTIVE', 'FROZEN'], needReason: true },
  { action: 'CANCEL_LOSS', label: '解挂', from: ['LOST'] },
  { action: 'REISSUE', label: '补卡', from: ['LOST'] },
  { action: 'FREEZE', label: '冻结', from: ['ACTIVE'] },
  { action: 'UNFREEZE', label: '解冻', from: ['FROZEN'] },
  { action: 'CANCEL', label: '注销', from: ['PENDING', 'ACTIVE', 'FROZEN', 'LOST'], needReason: true, danger: true }
]
export function cardStatus(card: { status?: string; lossFlag?: number }) {
  const status = card.status || 'ACTIVE'
  return status === 'ACTIVE' && card.lossFlag === 1 ? 'LOST' : status
}
export function cardActions(card: { status?: string; lossFlag?: number }) {
  const status = cardStatus(card)
  return CARD_ACTIONS.filter((item) => item.from.includes(status))
}

/** 任务看板列：待分派、执行中、待验收、已完成 */
export const TASK_COLUMNS = [
  { key: 'unassigned', label: '待分派' },
  { key: 'doing', label: '执行中' },
  { key: 'submitted', label: '待验收' },
  { key: 'done', label: '已完成' }
] as const
export function taskColumn(task: { status?: string; assigneeId?: unknown }) {
  if (task.status === 'DONE') return 'done'
  if (task.status === 'SUBMITTED') return 'submitted'
  return task.assigneeId ? 'doing' : 'unassigned'
}

/** 满意度统计口径：写明分母、量表与时间窗 */
export function surveyCaliber(summary: { scoredSubmissions?: number; unscoredSubmissions?: number }, range?: string[] | null) {
  const window = range?.length === 2 ? `${range[0]} 至 ${range[1]}` : '全部时间'
  return `统计口径：时间窗 ${window}；平均分分母为已评分提交 ${summary.scoredSubmissions || 0} 份，未评分/未答复 ${summary.unscoredSubmissions || 0} 份不计入；分值按所选模板各题分值合计。`
}
