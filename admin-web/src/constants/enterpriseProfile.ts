export interface EnterpriseQuickStat {
  value: string
  label: string
  tip?: string
}

export interface EnterpriseSectionItem {
  title: string
  desc: string
}

export interface EnterpriseProfile {
  name: string
  shortName: string
  slogan: string
  subtitle: string
  mission: string
  vision: string
  heroTitle: string
  heroDesc: string
  quickStats: EnterpriseQuickStat[]
  serviceGroups: EnterpriseSectionItem[]
  strengths: EnterpriseSectionItem[]
  carePrinciples: EnterpriseSectionItem[]
  environment: EnterpriseSectionItem[]
  admissionFlow: string[]
  contact: {
    phone: string
    hotlineTip: string
    email: string
    address: string
    visitingTime: string
  }
  legal: {
    icp?: string
    publicSecurityRecord?: string
  }
}

export const enterpriseProfile: EnterpriseProfile = {
  name: '弋阳龟峰颐养中心',
  shortName: '龟峰颐养',
  slogan: '医养结合 · 失能照护 · 认知关怀',
  subtitle: '打造“住得安心、养得舒心、家属放心”的现代化养老服务机构',
  mission: '以专业照护与温暖陪伴，守护长者生命质量。',
  vision: '成为弋阳及周边地区可信赖的区域型养老服务标杆。',
  heroTitle: '让每一位长者\n都被专业、温暖地照护',
  heroDesc:
    '弋阳龟峰颐养中心聚焦“医疗协同 + 生活照料 + 康复训练 + 精神慰藉”，构建全周期养老服务体系，服务自理、半失能、失能及认知障碍长者。',
  quickStats: [
    { value: '7×24', label: '全天候照护', tip: '护理、巡查、应急响应' },
    { value: '多学科', label: '医护协同', tip: '医生、护士、康复、社工联动' },
    { value: '1人1档', label: '个案管理', tip: '评估—计划—执行—复盘' },
    { value: '可视化', label: '智慧运营', tip: '后台统一管理与留痕' }
  ],
  serviceGroups: [
    {
      title: '基础生活照料',
      desc: '覆盖起居协助、饮食照护、清洁护理、睡眠管理，保障长者日常生活质量。'
    },
    {
      title: '健康与慢病管理',
      desc: '建立健康档案，跟踪血压、血糖、用药及体征变化，提供风险预警与干预。'
    },
    {
      title: '康复训练与功能维护',
      desc: '根据评估结果制定康复计划，关注步态、平衡、关节功能及生活能力维持。'
    },
    {
      title: '认知症与情绪关怀',
      desc: '提供认知训练、情绪疏导、行为干预与家庭支持，提升认知障碍长者生活体验。'
    },
    {
      title: '营养膳食服务',
      desc: '由营养与护理团队协同制定个性化餐食方案，兼顾慢病饮食与吞咽需求。'
    },
    {
      title: '文娱社交活动',
      desc: '组织节庆活动、兴趣社团、康乐课程，促进长者社交参与与心理健康。'
    }
  ],
  strengths: [
    {
      title: '医养结合服务体系',
      desc: '与医疗资源联动，形成“健康评估—照护计划—执行复盘”的连续照护闭环。'
    },
    {
      title: '标准化质量管理',
      desc: '围绕护理流程、巡检机制、事件记录、服务复盘建立质量管控与持续改进机制。'
    },
    {
      title: '智慧化管理后台',
      desc: '统一管理长者档案、床位、护理任务、后勤物资、收费账务，提升服务效率与透明度。'
    },
    {
      title: '家属协同沟通',
      desc: '通过定期回访、重点事件通知、阶段评估反馈，构建机构与家属之间的信任桥梁。'
    }
  ],
  carePrinciples: [
    {
      title: '尊重个体差异',
      desc: '根据身体状况、生活习惯、家庭诉求制定个性化照护目标。'
    },
    {
      title: '安全优先',
      desc: '聚焦跌倒、误吸、压疮、走失、用药等高风险场景，建立日常预防机制。'
    },
    {
      title: '功能维持优先',
      desc: '在安全前提下，尽可能保留长者自理能力与社会参与能力。'
    },
    {
      title: '持续沟通反馈',
      desc: '定期同步照护进展和异常情况，帮助家属共同参与照护决策。'
    }
  ],
  environment: [
    {
      title: '适老化居住空间',
      desc: '无障碍通行、防滑扶手、夜间照明与紧急呼叫系统，降低居住风险。'
    },
    {
      title: '护理与康复区域',
      desc: '设置功能训练区、康乐活动区、评估区，满足日常照护与康复训练需求。'
    },
    {
      title: '公共活动与会客区',
      desc: '营造温馨交流空间，支持亲友探访、集体活动与情绪疗愈。'
    }
  ],
  admissionFlow: ['预约咨询', '到院评估', '照护方案确认', '签约入住', '周期复评'],
  contact: {
    phone: '0793-1234567',
    hotlineTip: '（示例）工作日 08:00-18:00',
    email: 'service@yiyang-guifeng-care.cn',
    address: '江西省上饶市弋阳县龟峰大道（示例地址，请替换为真实地址）',
    visitingTime: '周一至周日 08:00-18:00'
  },
  legal: {
    icp: '赣ICP备XXXXXXXX号-1',
    publicSecurityRecord: '赣公网安备 XXXXXXXXXXXXXX号'
  }
}
