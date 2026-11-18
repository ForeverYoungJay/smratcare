import dayjs from 'dayjs'
import type { CenterTab } from '../../medical/center/centerTabs'
import type { FireSafetyRecord, FireSafetyRecordType } from '../../../types'

const P = '/fire/'

/** 消防 5 个主菜单里的「标签 + 原有页面」型页面（工作台独立） */
export const FIRE_CENTERS: Record<string, { title: string; subTitle: string; tabs: CenterTab[] }> = {
  inspection: {
    title: '防火巡查与检查',
    subTitle: '日间、夜间巡查与每月防火检查；发现问题直接登记，形成隐患整改',
    tabs: [
      { key: 'day', label: '日间巡查', path: `${P}day-patrol`, note: '老年人居室、公共活动用房、厨房等重点部位白天至少巡查 2 次，其他部位每日至少 1 次；每条记录保留位置、时间、人员和发现情况。' },
      { key: 'night', label: '夜间巡查', path: `${P}night-patrol`, note: '夜间至少每 2 小时巡查一次，风险区域可提高频次。系统提醒不代替现场巡查，禁止事后补填巡查时间。', noteType: 'warning' },
      { key: 'monthly', label: '每月防火检查', path: `${P}monthly-check`, note: '月度、重要节假日及重大活动前的检查单独建记录；发现的问题须登记问题描述，在隐患整改中跟踪到关闭。' }
    ]
  },
  facilities: {
    title: '消防设施与维保',
    subTitle: '设施台账、维保计划与报告、故障维修共用同一设施编号',
    tabs: [
      { key: 'assets', label: '消防设施台账', path: `${P}facility-management`, note: '设施状态以维修、维保和巡查结果为准，不在多个页面分别手工修改。停用、测试和控制只能由专业人员按流程实施。' },
      { key: 'maintenance', label: '维护保养与报告', path: `${P}maintenance-report` },
      { key: 'signing', label: '报告签署', path: `${P}report-signing`, note: '维保报告由提交人签署、另一位主管审核签署；签署时留存文件哈希，签署记录只增不改。' },
      { key: 'faults', label: '故障维修', path: `${P}fault-maintenance`, note: '消防设施不得擅自停用。故障期间的临时防范措施由授权人员按预案实施；关闭工单前须写明修复结果和功能复核。', noteType: 'error' }
    ]
  },
  'control-center': {
    title: '监控与控制室',
    subTitle: '公共区域视频与授权调阅；消防控制室值班与交接',
    tabs: [
      { key: 'live', label: '视频画面', path: `${P}video-wall`, note: '仅在公共区域设置监控点位，严禁在卧室、卫生间、更衣室等涉及隐私的区域安装。', noteType: 'warning' },
      { key: 'access', label: '录像调阅', path: `${P}video-access`, note: '录像查看、导出须写明事由并经主管批准，审批人不能是申请人本人；每次查看都记录审计日志。', noteType: 'warning' },
      { key: 'video', label: '摄像头与录像管理', path: `${P}video` },
      { key: 'duty', label: '控制室值班', path: `${P}control-room-duty`, note: '养老机构实行 24 小时值班。本系统不提供消音、屏蔽、复位等主机操作。' },
      { key: 'handover', label: '值班交接', path: `${P}handover`, note: '交接班由交班与接班人员分别确认，记录控制器状态、未解决故障和待处理事项。' }
    ]
  },
  safety: {
    title: '隐患处置与分析',
    subTitle: '各来源登记的问题统一跟踪整改；巡查、整改与设施数据统计',
    tabs: [
      { key: 'hazards', label: '隐患整改与应急', path: `${P}hazards`, note: '确认发生火灾时：立即拨打 119、通知值班负责人、启动灭火和应急疏散预案。本系统只辅助留痕，不替代现场应急行动，也不会自动报警。', noteType: 'error' },
      { key: 'drills', label: '培训与演练', path: `${P}drills` },
      { key: 'analytics', label: '统计报表与档案', path: `${P}data-stats` }
    ]
  }
}

export const RECORD_TYPE_LABEL: Record<FireSafetyRecordType, string> = {
  DAY_PATROL: '日间巡查',
  NIGHT_PATROL: '夜间巡查',
  MONTHLY_CHECK: '月度检查',
  FACILITY: '设施台账',
  MAINTENANCE_REPORT: '维保报告',
  FAULT_MAINTENANCE: '故障维修',
  CONTROL_ROOM_DUTY: '控制室值班'
}

/** 隐患所在的原业务标签，办理时回到原记录页 */
export const RECORD_TYPE_PATH: Record<FireSafetyRecordType, string> = {
  DAY_PATROL: '/fire/inspection?tab=day',
  NIGHT_PATROL: '/fire/inspection?tab=night',
  MONTHLY_CHECK: '/fire/inspection?tab=monthly',
  FACILITY: '/fire/facilities?tab=assets',
  MAINTENANCE_REPORT: '/fire/facilities?tab=maintenance',
  FAULT_MAINTENANCE: '/fire/facilities?tab=faults',
  CONTROL_ROOM_DUTY: '/fire/control-center?tab=duty'
}

/** 火警应急参考：只给出步骤，系统不会自动拨打 119 */
export const EMERGENCY_STEPS = [
  { title: '报警与核实', text: '以现场情况和消防主机信息为准，确认火灾立即拨打 119。' },
  { title: '报告与启动', text: '通知值班负责人和消防安全管理人，启动既定灭火和应急疏散预案。' },
  { title: '人员疏散', text: '优先协助行动不便的长者疏散，清点人数，引导到安全区域。' },
  { title: '事件留痕', text: '在不耽误救援的前提下，事后补录时间线、处置过程和相关证据。' }
]

/** 隐患关闭只给主管/管理层；一线巡查人员登记和处置，不自行关闭 */
export function canCloseHazard(roles: string[]) {
  return roles.some((role) => ['LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(role).toUpperCase()))
}

/** 近 N 天每日的巡查完成数与新登记问题数（按检查时间） */
export function dailyTrend(records: Pick<FireSafetyRecord, 'recordType' | 'checkTime' | 'issueDescription'>[], days: number, today = dayjs()) {
  const buckets = Array.from({ length: days }, (_, i) => {
    const day = today.subtract(days - 1 - i, 'day')
    return { key: day.format('YYYY-MM-DD'), label: day.format('MM/DD'), patrols: 0, issues: 0 }
  })
  const byKey = new Map(buckets.map((b) => [b.key, b]))
  for (const record of records) {
    const bucket = record.checkTime ? byKey.get(dayjs(record.checkTime).format('YYYY-MM-DD')) : undefined
    if (!bucket) continue
    if (['DAY_PATROL', 'NIGHT_PATROL', 'MONTHLY_CHECK'].includes(record.recordType)) bucket.patrols += 1
    if (record.issueDescription?.trim()) bucket.issues += 1
  }
  return buckets
}

/** 控制室交接清单（设计稿：控制器运行与报警、未关闭故障、联动设备、值班日志与预案位置） */
export const HANDOVER_ITEMS = ['控制器运行与报警记录', '未关闭故障与处置措施', '消防联动设备运行状态', '值班日志及应急预案位置']

export function buildChecklist(checked: string[], items = HANDOVER_ITEMS) {
  return JSON.stringify(items.map((item) => ({ item, checked: checked.includes(item) })))
}

export function parseChecklist(value?: string): { item: string; checked: boolean }[] {
  try {
    const parsed = JSON.parse(value || '[]')
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

/** 检查模板逐项结果：未通过的项写进问题描述，形成隐患；全部通过返回空 */
export function templateIssueLine(items: string[], passed: string[]) {
  const failed = items.filter((item) => !passed.includes(item))
  return failed.length ? `检查未通过：${failed.join('、')}` : ''
}

export function splitTemplateItems(items?: string) {
  return (items || '').split('\n').map((line) => line.trim()).filter(Boolean)
}
