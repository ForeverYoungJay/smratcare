export interface EnterpriseQuickStat {
  value: string
  label: string
  tip?: string
}

export interface EnterpriseSectionItem {
  title: string
  desc: string
}

export interface CityTierItem {
  tier: string
  cityGroup: string
  model: string
}

export interface EnterpriseNewsItem {
  title: string
  date: string
  tag: string
  desc: string
}

export interface EnterpriseActivityItem {
  title: string
  schedule: string
  location: string
  desc: string
}

export interface EnterpriseJobItem {
  title: string
  department: string
  requirement: string
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
  serviceSystem: EnterpriseSectionItem[]
  residentSystem: EnterpriseSectionItem[]
  residentHighlights: EnterpriseSectionItem[]
  communityUpdates: EnterpriseSectionItem[]
  newsList: EnterpriseNewsItem[]
  residentActivities: EnterpriseActivityItem[]
  aboutUs: EnterpriseSectionItem[]
  jobs: EnterpriseJobItem[]
  cooperation: EnterpriseSectionItem[]
  cityTiers: CityTierItem[]
  admissionFlow: string[]
  vrCommunityUrl: string
  joinUsUrl: string
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
  slogan: '医养结合 · 专业照护 · 温暖陪伴',
  subtitle: '面向长者全生命周期，提供高品质养老、护理与康复服务',
  mission: '以专业照护与人文关怀，守护每一位长者的尊严与生活质量。',
  vision: '成为上饶地区值得信赖的示范型智慧养老机构。',
  heroTitle: '弋阳龟峰颐养中心\n让长者安享有品质的晚年生活',
  heroDesc:
    '参考头部养老社区的服务理念，我们构建“服务体系 + 居民体系 + 社区运营 + 企业治理”的完整企业首页框架，同时保留管理后台入口，支持机构对外展示与对内运营一体化。',
  quickStats: [
    { value: '7×24', label: '全天候照护', tip: '护理、巡查、应急响应' },
    { value: '多学科', label: '专业团队', tip: '医生、护士、康复、社工协作' },
    { value: '1人1案', label: '个案管理', tip: '评估—计划—执行—复盘' },
    { value: '可视化', label: '智慧运营', tip: '全流程系统留痕' }
  ],
  serviceSystem: [
    {
      title: '活力生活',
      desc: '以生活方式管理为核心，构建社交、文娱、营养、运动等日常活力场景。'
    },
    {
      title: '专业护理',
      desc: '面向自理、半失能、失能长者提供分级护理服务，落实护理质量标准。'
    },
    {
      title: '记忆照护',
      desc: '针对认知障碍长者开展非药物干预、情绪安抚、行为支持及家属协同照护。'
    },
    {
      title: '康复医院',
      desc: '联动康复医疗资源，覆盖慢病管理、术后康复、功能训练与健康促进。'
    },
    {
      title: '生命关怀',
      desc: '尊重生命全周期需求，提供心理支持、舒缓关怀与家庭陪伴服务。'
    }
  ],
  residentSystem: [
    {
      title: '龟峰居民体系',
      desc: '围绕“居民参与 + 共建共享 + 价值再创造”，建设有归属感的长者社区。'
    },
    {
      title: '居民价值再创造',
      desc: '鼓励长者通过兴趣社团、志愿分享、经验传承持续创造社区价值。'
    },
    {
      title: '居民风采',
      desc: '展示长者在文体、艺术、公益等方面的精神风貌与个人故事。'
    },
    {
      title: '居民故事',
      desc: '以人物故事记录居民的生活转变，传递积极养老理念。'
    },
    {
      title: '居民组织',
      desc: '建立居民议事会、兴趣社团和互助小组，提升社区自治与参与感。'
    }
  ],
  residentHighlights: [
    {
      title: '社区动态',
      desc: '发布社区活动、健康讲座、节庆计划与服务升级信息。'
    },
    {
      title: '新闻资讯',
      desc: '同步机构新闻、行业趋势与政策信息，让家属及时掌握服务进展。'
    },
    {
      title: '居民活动',
      desc: '每月组织多类型活动，覆盖健康、兴趣、社交、陪伴等场景。'
    },
    {
      title: 'VR看社区',
      desc: '通过线上全景方式展示院内环境与公共空间，提升参访决策效率。'
    }
  ],
  communityUpdates: [
    {
      title: '本周活动：春季养生课堂',
      desc: '邀请康复治疗师与营养师联合开展春季养生专题分享。'
    },
    {
      title: '长者风采：合唱队展演',
      desc: '龟峰颐养合唱队开展社区公益演出，展现积极老龄生活方式。'
    },
    {
      title: '服务升级：夜间巡护优化',
      desc: '新增夜间重点照护巡检节点，提升失能长者夜间安全保障。'
    }
  ],
  newsList: [
    {
      title: '弋阳龟峰颐养中心春季服务升级发布',
      date: '2026-03-01',
      tag: '机构新闻',
      desc: '围绕失能照护、记忆照护与夜间巡护流程完成新一轮服务升级。'
    },
    {
      title: '健康讲堂启动：慢病管理与家庭照护',
      date: '2026-02-20',
      tag: '健康资讯',
      desc: '邀请医生、康复师与护理团队为家属开展慢病与康复管理专题分享。'
    },
    {
      title: '院内适老化环境优化完成阶段验收',
      date: '2026-02-08',
      tag: '社区动态',
      desc: '完成公共区域照明、扶手、防滑与紧急呼叫点位优化。'
    }
  ],
  residentActivities: [
    {
      title: '银龄合唱活动',
      schedule: '每周二、周四 15:00',
      location: '多功能活动厅',
      desc: '通过音乐互动提升社交参与和情绪活力。'
    },
    {
      title: '康复体能训练营',
      schedule: '每周一至周五 09:30',
      location: '康复训练区',
      desc: '针对步态、平衡与关节功能进行个性化训练。'
    },
    {
      title: '记忆工坊',
      schedule: '每周三 10:00',
      location: '认知关怀室',
      desc: '开展认知刺激训练与回忆疗法，延缓认知功能下降。'
    }
  ],
  aboutUs: [
    {
      title: '企业介绍',
      desc: '弋阳龟峰颐养中心是集颐养、护理、康复、社区运营于一体的综合养老机构。'
    },
    {
      title: '联系我们',
      desc: '支持电话咨询、到院参观、家属会谈与照护方案定制。'
    },
    {
      title: '加入我们',
      desc: '持续招募护士、护理员、康复治疗师、社工、运营专员等岗位。'
    }
  ],
  jobs: [
    {
      title: '护理主管',
      department: '护理部',
      requirement: '3年以上机构护理管理经验，熟悉失能照护流程。'
    },
    {
      title: '康复治疗师',
      department: '康复中心',
      requirement: '具备康复治疗相关资质，具备老年康复经验优先。'
    },
    {
      title: '养老护理员',
      department: '照护中心',
      requirement: '具备护理员证书，责任心强，接受排班管理。'
    }
  ],
  cooperation: [
    {
      title: '业务合作',
      desc: '欢迎与医疗机构、康复团队、供应链伙伴、社区组织开展长期合作。'
    },
    {
      title: '机构联动',
      desc: '可共建失能照护示范点、记忆照护专区、健康管理联合项目。'
    }
  ],
  cityTiers: [
    { tier: '核心城市', cityGroup: '上饶主城区', model: '城市综合型养老服务中心' },
    { tier: '重点县域', cityGroup: '弋阳县/横峰县/铅山县', model: '医养结合区域服务站' },
    { tier: '周边辐射', cityGroup: '周边乡镇', model: '社区支持与居家延伸服务' }
  ],
  admissionFlow: ['预约咨询', '到院参观', '综合评估', '方案确认', '签约入住', '周期复评'],
  vrCommunityUrl: '#',
  joinUsUrl: '#',
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
