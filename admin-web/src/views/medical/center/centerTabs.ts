export interface CenterTab {
  key: string
  label: string
  /** 原有业务页的路由，组件与权限都从这里取 */
  path: string
  note?: string
  noteType?: 'info' | 'warning' | 'error'
}

/** query.tab 指向可见标签就用它，否则用第一个可见标签 */
export function pickTab<T extends { key: string }>(tabs: T[], requested: unknown): T | undefined {
  return tabs.find((tab) => tab.key === requested) || tabs[0]
}

/**
 * 能进入主页面就等于获得其标签页的页面授权；不限页面（空列表）时保持不限。
 * 各标签自身的角色限制仍由路由 meta.roles 判断。
 */
export function tabPagePermissions(pagePermissions: string[] | undefined, tabs: Pick<CenterTab, 'path'>[]) {
  const pages = pagePermissions || []
  return pages.length ? [...pages, ...tabs.map((tab) => tab.path)] : pages
}

const P = '/medical-care/'

/** 7 个主菜单里的「标签 + 原有页面」型页面（工作台、健康档案是独立页面） */
export const MEDICAL_CENTERS: Record<string, { title: string; subTitle: string; tabs: CenterTab[] }> = {
  clinical: {
    title: '诊疗与医嘱',
    subTitle: '医生一站式完成巡诊、电子病历记录和授权医嘱开立；已签署文书保留版本历史',
    tabs: [
      { key: 'rounds', label: '巡诊', path: `${P}rounds` },
      { key: 'emr', label: '电子病历', path: `${P}emr`, note: '签署后的病历变更走修订流程，保留审计日志，不直接覆盖历史。', noteType: 'warning' },
      { key: 'orders', label: '医嘱开立与审核', path: `${P}orders`, note: '医嘱开立、审方、执行按资质和角色分权。' }
    ]
  },
  medication: {
    title: '用药与医嘱执行',
    subTitle: '医嘱核对、执行留痕、漏服拒服上报、药品库存查询；严格区分医生、护士和药事权限',
    tabs: [
      { key: 'executions', label: '医嘱执行', path: `${P}order-executions`, note: '所有执行记录关联原始医嘱和执行人员，不得仅凭勾选即视为实际完成。' },
      { key: 'registration', label: '用药登记', path: `${P}medication-registration` },
      { key: 'pharmacy', label: '药事与库存', path: `${P}pharmacy` },
      { key: 'risk', label: '执行风险总览', path: `${P}order-risk-overview` }
    ]
  },
  monitoring: {
    title: '健康监测与随访',
    subTitle: '健康巡检、生命体征趋势、慢病随访、专项评估；AI 报告需专业审核',
    tabs: [
      { key: 'inspection', label: '健康巡检', path: `${P}inspection`, note: '设备测量异常先核对患者、设备、时间和测量情境，必要时立即启动人工核实。' },
      { key: 'followup', label: '慢病随访', path: `${P}followup` },
      { key: 'tcm', label: '中医体质评估', path: `${P}assessment/tcm` },
      { key: 'cvd', label: '心血管风险评估', path: `${P}assessment/cvd` },
      { key: 'ai', label: 'AI 健康报告', path: `${P}ai-reports`, note: 'AI 用于辅助整理和提示，不自动形成诊断、处方或正式签署意见。', noteType: 'warning' }
    ]
  },
  coordination: {
    title: '医护协同与质控',
    subTitle: '跨部门任务共用一个任务ID；护理日志与交接班引用既有记录；质控指标和整改闭环',
    tabs: [
      { key: 'tasks', label: '医护协同任务', path: `${P}unified-task-center`, note: '护士执行医嘱时只创建关联执行记录，不生成第二份独立医嘱或护理任务。' },
      { key: 'board', label: '护理信息共享', path: `${P}care-task-board` },
      { key: 'log', label: '护理日志', path: `${P}nursing-log` },
      { key: 'handover', label: '交接重点', path: `${P}handovers` },
      { key: 'quality', label: '质量分析与整改', path: `${P}nursing-quality` }
    ]
  },
  alerts: {
    title: '告警与应急',
    subTitle: '统一查看智慧设备告警、人工上报、派单处置与 120 急救事件；紧急情况优先现场救助',
    tabs: [
      { key: 'device', label: '设备告警', path: `${P}smart-alerts`, note: '遇到需要紧急救助的真实情况，先启动现场应急预案并拨打 120；系统记录不能代替人工求助。', noteType: 'error' },
      { key: 'dispatch', label: '派单处置', path: `${P}smart-dispatch` },
      { key: 'emergency', label: '120 急救记录', path: `${P}emergency`, note: '应急优先：现场施救 / 联系 120 / 组织转诊，允许先处置后补录。', noteType: 'error' }
    ]
  },
  settings: {
    title: '医护系统配置',
    subTitle: '仅授权系统管理员与业务主管可见；规则变更保留版本和操作日志',
    tabs: [
      { key: 'rules', label: '异常监测规则', path: `${P}alert-rules`, note: '临床相关的监测阈值和响应规则应由具备相应权限的专业人员审核确认。', noteType: 'warning' },
      { key: 'scenes', label: '安全场景规则', path: `${P}smart-rules` },
      { key: 'devices', label: '设备接入与健康', path: `${P}smart-device-health` },
      { key: 'accounts', label: '健康服务与账户', path: `${P}integrated-account`, note: 'IT 人员维护系统配置，不默认具有修改临床病历和医嘱的权限。' }
    ]
  }
}
