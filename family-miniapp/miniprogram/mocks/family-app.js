const baseElders = [
  {
    elderId: 101,
    elderName: '王秀兰',
    age: 82,
    gender: '女',
    roomNo: '3号楼 205室 2床',
    careLevel: '二级护理',
    status: '在院',
    statusType: 'normal',
    checkinDays: 128,
    healthOverview: '平稳',
    latestUpdateTime: '10:25',
    specialTag: '血压监测中',
    dynamicPreview: '上午完成血压测量，状态平稳',
    locationText: '护理单元A-205'
  },
  {
    elderId: 102,
    elderName: '李正国',
    age: 86,
    gender: '男',
    roomNo: '2号楼 102室 1床',
    careLevel: '一级护理',
    status: '活动中',
    statusType: 'active',
    checkinDays: 64,
    healthOverview: '稍需关注',
    latestUpdateTime: '09:50',
    specialTag: '术后恢复中',
    dynamicPreview: '今日参加了音乐疗愈活动',
    locationText: '文娱活动室'
  },
  {
    elderId: 103,
    elderName: '周桂芬',
    age: 89,
    gender: '女',
    roomNo: '1号楼 308室 1床',
    careLevel: '三级护理',
    status: '就医中',
    statusType: 'warning',
    checkinDays: 233,
    healthOverview: '复查中',
    latestUpdateTime: '08:40',
    specialTag: '今日复查',
    dynamicPreview: '由责任护士陪同前往人民医院复查',
    locationText: '人民医院心内科'
  }
];

const healthTrends = {
  '7d': [
    { date: '03-02', sbp: 132, dbp: 82, hr: 75, temp: 36.4, sugar: 6.1 },
    { date: '03-03', sbp: 136, dbp: 84, hr: 78, temp: 36.5, sugar: 6.3 },
    { date: '03-04', sbp: 138, dbp: 86, hr: 76, temp: 36.6, sugar: 6.2 },
    { date: '03-05', sbp: 142, dbp: 87, hr: 82, temp: 36.5, sugar: 6.5 },
    { date: '03-06', sbp: 135, dbp: 85, hr: 77, temp: 36.4, sugar: 6.2 },
    { date: '03-07', sbp: 138, dbp: 88, hr: 79, temp: 36.5, sugar: 6.2 },
    { date: '03-08', sbp: 138, dbp: 88, hr: 78, temp: 36.5, sugar: 6.2 }
  ],
  '30d': [
    { date: 'W1', sbp: 136, dbp: 84, hr: 77, temp: 36.5, sugar: 6.2 },
    { date: 'W2', sbp: 139, dbp: 85, hr: 79, temp: 36.4, sugar: 6.3 },
    { date: 'W3', sbp: 137, dbp: 84, hr: 78, temp: 36.5, sugar: 6.2 },
    { date: 'W4', sbp: 138, dbp: 86, hr: 78, temp: 36.5, sugar: 6.2 }
  ],
  '90d': [
    { date: 'M1', sbp: 140, dbp: 87, hr: 80, temp: 36.5, sugar: 6.4 },
    { date: 'M2', sbp: 138, dbp: 86, hr: 79, temp: 36.5, sugar: 6.3 },
    { date: 'M3', sbp: 137, dbp: 85, hr: 78, temp: 36.5, sugar: 6.2 }
  ]
};

const weeklyBrief = {
  elderId: 101,
  elderName: '王秀兰',
  weekRange: '2026-03-03 ~ 2026-03-09',
  overallLevel: 'warning',
  overallText: '本周存在少量异常指标，护理团队已持续跟进，请家属关注复测提醒。',
  healthCheckCount: 18,
  abnormalCount: 2,
  nursingLogCount: 26,
  activityCount: 4,
  communicationCount: 3,
  unreadMessageCount: 2,
  vitalBadges: [
    { name: '血压', value: '138/88 mmHg', status: '需关注' },
    { name: '心率', value: '78 次/分', status: '正常' },
    { name: '体温', value: '36.5 ℃', status: '正常' },
    { name: '血糖', value: '6.2 mmol/L', status: '正常' }
  ],
  highlights: [
    '本周累计健康检测 18 次，护理日志 26 条',
    '本周参与活动 4 场，精神状态较上周更积极',
    '家属端本周沟通 3 次，当前仍有 2 条未读提醒'
  ],
  suggestions: [
    '建议未来 48 小时持续关注午后血压复测结果',
    '优先处理紧急消息，避免错过关键进展'
  ]
};

const weeklyBriefHistory = [
  {
    weekRange: '2026-03-03 ~ 2026-03-09',
    overallLevel: 'warning',
    overallText: '本周存在异常指标',
    healthCheckCount: 18,
    abnormalCount: 2,
    nursingLogCount: 26,
    activityCount: 4,
    communicationCount: 3,
    quickText: '异常 2 项，建议优先关注健康复测'
  },
  {
    weekRange: '2026-02-24 ~ 2026-03-02',
    overallLevel: 'normal',
    overallText: '本周整体平稳',
    healthCheckCount: 16,
    abnormalCount: 0,
    nursingLogCount: 24,
    activityCount: 5,
    communicationCount: 2,
    quickText: '健康检测 16 次，护理记录 24 条'
  },
  {
    weekRange: '2026-02-17 ~ 2026-02-23',
    overallLevel: 'normal',
    overallText: '本周整体平稳',
    healthCheckCount: 15,
    abnormalCount: 1,
    nursingLogCount: 23,
    activityCount: 3,
    communicationCount: 1,
    quickText: '异常 1 项，建议优先关注健康复测'
  }
];

const messageList = [
  {
    id: 1,
    level: 'urgent',
    levelText: '一级提醒',
    type: '紧急事件',
    title: '血压异常已处理',
    content: '老人今晨血压异常，护士已跟进处理，当前状态已稳定。',
    elderId: 101,
    elderName: '王秀兰',
    time: '今天 08:15',
    unread: true,
    phone: '13800138000',
    timeline: [
      '08:12 发现血压偏高（156/95）',
      '08:20 护士完成复测并安抚',
      '08:35 联系值班医生调整观察方案',
      '09:10 指标回落至 142/90，继续跟踪'
    ]
  },
  {
    id: 2,
    level: 'warning',
    levelText: '二级提醒',
    type: '费用提醒',
    title: '账户余额不足',
    content: '老人账户余额低于预警线 500 元，请及时充值。',
    elderId: 101,
    elderName: '王秀兰',
    time: '今天 09:20',
    unread: true,
    phone: '13800138000',
    timeline: ['09:20 系统触发低余额预警', '09:21 已推送微信服务通知']
  },
  {
    id: 3,
    level: 'normal',
    levelText: '三级提醒',
    type: '活动通知',
    title: '生日会活动预告',
    content: '下午 3 点将举办集体生日会，欢迎线上点赞互动。',
    elderId: 102,
    elderName: '李正国',
    time: '今天 11:00',
    unread: false,
    phone: '13800138000',
    timeline: ['11:00 活动发布', '14:30 活动签到开始']
  },
  {
    id: 4,
    level: 'warning',
    levelText: '二级提醒',
    type: '就诊安排',
    title: '门诊复查提醒',
    content: '周桂芬将于今日下午进行心内科复查。',
    elderId: 103,
    elderName: '周桂芬',
    time: '今天 07:30',
    unread: false,
    phone: '13800138000',
    timeline: ['07:30 已确认复查安排', '13:30 护士陪同外出']
  }
];

const todaySchedules = [
  { id: 1, time: '08:00', name: '早餐', type: '膳食', status: '已完成', owner: '配餐组' },
  { id: 2, time: '09:30', name: '康复训练', type: '健康管理', status: '已完成', owner: '康复师 张老师' },
  { id: 3, time: '10:30', name: '血压测量', type: '护理', status: '进行中', owner: '责任护士 王护' },
  { id: 4, time: '14:00', name: '棋牌活动', type: '活动', status: '待开始', owner: '活动师 李老师' },
  { id: 5, time: '16:00', name: '散步训练', type: '活动', status: '待开始', owner: '护工 刘阿姨' },
  { id: 6, time: '19:00', name: '晚间护理', type: '护理', status: '待开始', owner: '晚班护理组' }
];

const mealCalendar = [
  {
    date: '2026-03-08',
    review: '今日饮食清淡、营养均衡，已安排低糖餐，适合当前状态。',
    tags: ['低糖餐', '软食', '营养加餐'],
    breakfast: ['小米粥', '鸡蛋羹', '清炒时蔬'],
    lunch: ['米饭', '清蒸鱼', '南瓜汤', '时令水果'],
    dinner: ['面条', '炖豆腐', '青菜汤'],
    eatingStatus: '早餐进食正常，午餐进食约 80%，晚餐需协助进食。',
    photos: [
      'https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=600&q=60',
      'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=600&q=60'
    ]
  },
  {
    date: '2026-03-09',
    review: '明日膳食以高蛋白与软食搭配为主，兼顾消化负担。',
    tags: ['软食', '高蛋白'],
    breakfast: ['燕麦粥', '蔬菜蛋卷'],
    lunch: ['杂粮饭', '红烧鸡腿', '冬瓜汤'],
    dinner: ['馄饨', '山药排骨汤'],
    eatingStatus: '计划观察晚餐进食速度。',
    photos: []
  }
];

const careLogs = [
  {
    id: 1,
    date: '2026-03-08',
    items: [
      {
        time: '08:20',
        project: '晨间护理',
        staff: '护工 刘阿姨',
        status: '已完成',
        note: '完成洗漱及衣物更换，情绪稳定。',
        photos: ['https://images.unsplash.com/photo-1511690656952-34342bb7c2f2?auto=format&fit=crop&w=500&q=60']
      },
      { time: '10:30', project: '血压检测', staff: '责任护士 王护', status: '已记录', note: '138/88 mmHg，持续观察。', photos: [] },
      {
        time: '14:20',
        project: '康复训练',
        staff: '康复师 张老师',
        status: '已完成',
        note: '下肢肌力训练 20 分钟。',
        photos: ['https://images.unsplash.com/photo-1543353071-087092ec393a?auto=format&fit=crop&w=500&q=60']
      }
    ]
  }
];

const activityAlbums = [
  {
    id: 1001,
    title: '音乐疗愈上午场',
    date: '2026-03-08 10:10',
    elderName: '李正国',
    description: '参与节奏互动与合唱环节，状态积极。',
    mediaType: 'photo',
    coverText: '活动照片 3 张',
    likes: 23,
    comments: 8,
    liked: false
  },
  {
    id: 1002,
    title: '生日会彩排',
    date: '2026-03-08 14:00',
    elderName: '王秀兰',
    description: '与同伴一起练习生日歌，互动良好。',
    mediaType: 'video',
    coverText: '短视频 01:12',
    likes: 31,
    comments: 12,
    liked: false
  }
];

const activityAlbumComments = {
  1001: [
    { id: 50001, albumId: 1001, commenter: '李女士', content: '状态看起来很好，辛苦老师们。', time: '2026-03-08 10:30', mine: true },
    { id: 50002, albumId: 1001, commenter: '王先生', content: '活动安排很丰富，点赞。', time: '2026-03-08 11:20', mine: false }
  ],
  1002: [
    { id: 50003, albumId: 1002, commenter: '张女士', content: '期待生日会照片更新。', time: '2026-03-08 14:40', mine: false }
  ]
};

const outingRecords = [
  {
    id: 501,
    elderName: '周桂芬',
    startTime: '2026-03-08 13:20',
    reason: '医院复查',
    companion: '责任护士 王护',
    destination: '人民医院心内科',
    returnTime: '2026-03-08 16:40',
    status: '已返院'
  },
  {
    id: 502,
    elderName: '王秀兰',
    startTime: '2026-03-07 15:00',
    reason: '家属短时接出',
    companion: '家属 李女士',
    destination: '家庭聚会',
    returnTime: '2026-03-07 18:10',
    status: '已返院'
  }
];

const medicalRecords = [
  {
    id: 901,
    date: '2026-03-08',
    hospital: '人民医院心内科',
    reason: '血压偏高复查',
    diagnosis: '轻度高血压',
    advice: '继续服用降压药，早晚监测血压',
    meds: '缬沙坦 80mg qd'
  },
  {
    id: 902,
    date: '2026-02-21',
    hospital: '院内门诊',
    reason: '睡眠质量下降',
    diagnosis: '睡眠紊乱倾向',
    advice: '调整作息并减少午后浓茶摄入',
    meds: '无新增用药'
  }
];

const assessmentReports = [
  {
    id: 3001,
    reportName: '老人综合能力评估报告',
    date: '2026-03-01',
    orgName: '智养云照护中心',
    summary: '生活能力中度依赖，建议维持二级护理。',
    score: '78 分',
    risk: '中等风险',
    type: '能力评估',
    fileName: 'elder-comprehensive-20260301.pdf',
    fileUrl: 'https://example.com/mock-reports/elder-comprehensive-20260301.pdf'
  },
  {
    id: 3002,
    reportName: 'AI中医体质评估',
    date: '2026-03-03',
    orgName: '智养云照护中心',
    summary: '判定为气虚体质，建议温补调养与早睡。',
    score: 'B+',
    risk: '一般风险',
    type: '中医体质',
    fileName: 'tcm-ai-20260303.pdf',
    fileUrl: 'https://example.com/mock-reports/tcm-ai-20260303.pdf'
  },
  {
    id: 3003,
    reportName: '心血管功能评估',
    date: '2026-03-05',
    orgName: '智养云照护中心',
    summary: '心血管健康评分 78，建议低盐饮食。',
    score: '78 分',
    risk: '中等风险',
    type: '心血管评估',
    fileName: 'cvd-assessment-20260305.pdf',
    fileUrl: 'https://example.com/mock-reports/cvd-assessment-20260305.pdf'
  }
];

const emergencyContacts = [
  { id: 1, role: '责任护士', name: '王护', phone: '13800138000', duty: '白班 08:00-20:00' },
  { id: 2, role: '生活管家', name: '陈管家', phone: '13800138001', duty: '全时段支持' },
  { id: 3, role: '客服中心', name: '客服专线', phone: '4008009000', duty: '7x24 在线' }
];

const billSummary = {
  elderName: '王秀兰',
  month: '2026-03',
  total: 6200,
  paid: 4000,
  outstanding: 2200,
  accountBalance: 380,
  autoPayEnabled: false,
  details: [
    { item: '住宿费', amount: 3000 },
    { item: '护理费', amount: 1800 },
    { item: '餐费', amount: 900 },
    { item: '增值服务费', amount: 500 }
  ]
};

const billHistory = [
  { month: '2026-02', total: 5980, paid: 5980, status: '已结清' },
  { month: '2026-01', total: 6010, paid: 6010, status: '已结清' },
  { month: '2025-12', total: 5880, paid: 5880, status: '已结清' }
];

const rechargeOrders = [
  {
    outTradeNo: 'FMWX202603080001',
    elderId: 101,
    elderName: '王秀兰',
    amount: 300,
    channel: 'WECHAT_JSAPI',
    status: 'PAID',
    statusText: '已支付',
    prepayId: 'wx201234567890',
    wxTransactionId: '4200001234202603081234567890',
    createTime: '2026-03-08 10:20',
    paidTime: '2026-03-08 10:21',
    remark: '微信充值到账'
  },
  {
    outTradeNo: 'FMWX202603080002',
    elderId: 101,
    elderName: '王秀兰',
    amount: 200,
    channel: 'WECHAT_JSAPI',
    status: 'PREPAY_CREATED',
    statusText: '待支付',
    prepayId: 'wx201234567891',
    wxTransactionId: '',
    createTime: '2026-03-08 11:05',
    paidTime: '',
    remark: '等待支付'
  }
];

const serviceCatalog = [
  { id: 1, category: '护理服务', name: '推拿服务', price: 120, unit: '次', desc: '缓解肌肉紧张，改善循环' },
  { id: 2, category: '护理服务', name: '康复理疗', price: 180, unit: '次', desc: '针对术后康复个性训练' },
  { id: 3, category: '生活服务', name: '理发服务', price: 38, unit: '次', desc: '每周固定上门理发' },
  { id: 4, category: '生活服务', name: '代购用品', price: 20, unit: '单', desc: '按清单采购日用品' },
  { id: 5, category: '特色餐饮', name: '营养加餐', price: 28, unit: '份', desc: '高蛋白低盐加餐组合' }
];

const serviceOrders = [
  { id: 701, name: '推拿服务', amount: 120, status: '已完成', date: '2026-03-06' },
  { id: 702, name: '理发服务', amount: 38, status: '待服务', date: '2026-03-10' }
];

const mallProducts = [
  {
    id: 1101,
    productCode: 'P-NUTRI-001',
    productName: '高蛋白营养粉',
    category: '营养品',
    unit: '罐',
    price: 128,
    pointsPrice: 128,
    currentStock: 18,
    stockStatus: 'OK',
    statusText: '可下单',
    businessDomain: 'ELDER',
    itemType: 'CONSUMABLE'
  },
  {
    id: 1102,
    productCode: 'P-CARE-012',
    productName: '防压疮护理垫',
    category: '护理用品',
    unit: '件',
    price: 86,
    pointsPrice: 86,
    currentStock: 6,
    stockStatus: 'LOW',
    statusText: '库存紧张',
    businessDomain: 'ELDER',
    itemType: 'CONSUMABLE'
  },
  {
    id: 1103,
    productCode: 'P-DAILY-088',
    productName: '长者保暖披肩',
    category: '生活用品',
    unit: '件',
    price: 59,
    pointsPrice: 59,
    currentStock: 0,
    stockStatus: 'OUT',
    statusText: '库存不足',
    businessDomain: 'ELDER',
    itemType: 'ASSET'
  }
];

const mallOrders = [
  {
    orderId: 99001,
    orderNo: 'SO202603090001',
    elderId: 101,
    elderName: '王秀兰',
    productId: 1101,
    productName: '高蛋白营养粉',
    quantity: 1,
    unitPrice: 128,
    totalAmount: 128,
    pointsUsed: 128,
    orderStatus: 1,
    orderStatusText: '待处理',
    payStatus: 0,
    payStatusText: '待支付',
    createTime: '2026-03-09 10:12',
    payTime: ''
  }
];

const affectionMoments = [
  { id: 1, type: 'photo', title: '家庭合照上传', time: '2026-03-08 09:30', desc: '已由护工协助老人查看照片。' },
  { id: 2, type: 'voice', title: '语音留言', time: '2026-03-08 12:00', desc: '“妈妈我想你了，周末来看你。”' },
  { id: 3, type: 'greeting', title: '节日祝福模板', time: '2026-03-07 18:30', desc: '中秋祝福已收藏，可一键发送。' }
];

const festivalTemplates = ['生日祝福', '春节问候', '中秋团圆', '母亲节祝福'];

const appState = {
  loginPassword: 'Family@123',
  wechatNotifyOpenId: '',
  notificationSettings: {
    healthAlert: true,
    paymentAlert: true,
    activityAlert: false,
    urgentAlert: true,
    smsFallback: true,
    subscribeTemplateIds: ['TEMPLATE_HEALTH_ALERT', 'TEMPLATE_PAYMENT_ALERT'],
    subscribeAuthorized: false,
    subscribeAuthorizedTime: ''
  },
  profile: {
    realName: '李女士',
    phone: '13900001111',
    address: '上海市浦东新区***路',
    preferredContact: '微信消息'
  },
  securitySettings: {
    verifyHealthData: true,
    verifyMedicalRecords: true,
    verifyReports: true,
    verifyWithSmsCode: false,
    verifyWithPassword: true,
    hasIndependentPassword: false,
    maskSensitiveData: true,
    visibleScope: '仅子女可查看完整健康数据'
  },
  securityPassword: '',
  communicationMessages: [
    { id: 9001, targetRole: '责任护士', msgType: 'text', content: '今天午后血压复测结果怎么样？', time: '2026-03-08 14:10', sender: '李女士', status: '已发送' },
    {
      id: 9002,
      targetRole: '客服中心',
      msgType: 'voice',
      content: '已发送语音留言，请协助播放给老人。',
      mediaUrl: 'https://mock.smartcare.local/family/voice/9002.m4a',
      mediaName: 'family-voice-9002.m4a',
      mediaDurationSec: 24,
      transcript: '妈妈我想你了，周末来看你。',
      time: '2026-03-08 13:22',
      sender: '李女士',
      status: '已发送'
    }
  ],
  helpFaqs: [
    { category: '绑定与家庭', question: '如何绑定多位老人？', answer: '进入“我的家庭”后可重复添加绑定，并设置主要联系人。' },
    { category: '提醒机制', question: '紧急事件如何第一时间通知？', answer: '建议开启“紧急通知”和“短信兜底提醒”，系统将双通道触达。' },
    { category: '隐私安全', question: '健康档案如何保护？', answer: '可配置健康档案、就医记录、评估报告查看时二次验证。' }
  ],
  familyBindings: [
    { elderId: 101, elderName: '王秀兰', relation: '女儿', isPrimary: true },
    { elderId: 102, elderName: '李正国', relation: '儿媳', isPrimary: false }
  ],
  feedbackRecords: [
    {
      id: 81001,
      feedbackType: 'EVALUATION',
      serviceType: '护理服务',
      score: 5,
      content: '护理过程细致，沟通及时。',
      status: 'DONE',
      reply: '感谢认可，我们会持续保持服务质量。',
      createTime: '2026-03-06 12:00',
      updateTime: '2026-03-07 09:30'
    }
  ],
  idCardBindingMap: {},
  todoActionState: {}
};

const smsState = {
  byPhone: {},
  securityCode: ''
};

const communicationTemplates = [
  {
    id: 'tpl-health-recheck',
    title: '血压复测反馈',
    targetRole: '责任护士',
    msgType: 'text',
    content: '请反馈今日午后血压复测结果，辛苦了。',
    scene: '健康关注'
  },
  {
    id: 'tpl-meal-followup',
    title: '进食情况确认',
    targetRole: '责任护工',
    msgType: 'text',
    content: '请协助确认今天午餐进食情况和是否需要额外协助。',
    scene: '膳食关注'
  },
  {
    id: 'tpl-video-assist',
    title: '协助探视预约',
    targetRole: '生活管家',
    msgType: 'video',
    content: '我想预约探视时段，请协助老人按预约时间参与。',
    scene: '探视预约'
  },
  {
    id: 'tpl-voice-greeting',
    title: '语音问候播放',
    targetRole: '责任护工',
    msgType: 'voice',
    content: '请协助播放家属语音问候，感谢。',
    scene: '亲情互动'
  },
  {
    id: 'tpl-payment-question',
    title: '账单咨询',
    targetRole: '客服中心',
    msgType: 'service',
    content: '请协助说明本月账单明细及待缴项目，谢谢。',
    scene: '费用咨询'
  }
];

function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function stableHash(input) {
  const text = String(input || '');
  let hash = 0;
  for (let i = 0; i < text.length; i += 1) {
    hash = ((hash << 5) - hash + text.charCodeAt(i)) | 0;
  }
  return Math.abs(hash).toString(16);
}

function buildTodoKey(item = {}) {
  const raw = `${item.type || 'GENERAL'}|${item.title || ''}|${item.actionPath || ''}|${item.description || ''}`;
  return `TODO-${stableHash(raw).toUpperCase().slice(0, 12)}`;
}

function formatDateTime(date) {
  const value = date instanceof Date ? date : new Date(date);
  if (Number.isNaN(value.getTime())) {
    return '';
  }
  const yyyy = value.getFullYear();
  const mm = `${value.getMonth() + 1}`.padStart(2, '0');
  const dd = `${value.getDate()}`.padStart(2, '0');
  const hh = `${value.getHours()}`.padStart(2, '0');
  const min = `${value.getMinutes()}`.padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}

function getTodayMeal() {
  return clone(mealCalendar[0]);
}

function getHomeDashboard() {
  return {
    elders: clone(baseElders),
    healthSummary: {
      stateText: '今日状态平稳，有 1 项指标需关注',
      level: 'warning',
      updateTime: '10:25',
      metrics: [
        { name: '体温', value: '36.5', unit: '℃', status: '正常', trend: '与昨日持平' },
        { name: '血压', value: '138/88', unit: 'mmHg', status: '轻度偏高', trend: '较昨日↑' },
        { name: '心率', value: '78', unit: '次/分', status: '正常', trend: '较昨日↓' },
        { name: '血糖', value: '6.2', unit: 'mmol/L', status: '正常', trend: '与昨日持平' }
      ],
      suggestion: '今日血压略高，院方已安排继续观察。'
    },
    schedules: clone(todaySchedules),
    meal: getTodayMeal(),
    notices: clone(messageList.slice(0, 3)),
    focusEvents: [
      '今日 15:00 家属可预约视频探视',
      '周桂芬复查后将更新医嘱结果',
      '王秀兰晚间护理新增血压复测'
    ]
  };
}

function getWeeklyBrief() {
  return clone(weeklyBrief);
}

function getWeeklyBriefHistory(weeks = 8) {
  const size = Math.max(1, Number(weeks) || 8);
  const list = [];
  for (let i = 0; i < size; i += 1) {
    list.push(clone(weeklyBriefHistory[i % weeklyBriefHistory.length]));
  }
  return list;
}

function getMessages(options = {}) {
  const level = (options.level || '').toLowerCase();
  const type = options.type || '';
  const unreadOnly = !!options.unreadOnly;
  const filtered = messageList.filter((item) => {
    if (level && (item.level || '').toLowerCase() !== level) {
      return false;
    }
    if (type && (item.type || '') !== type) {
      return false;
    }
    if (unreadOnly && !item.unread) {
      return false;
    }
    return true;
  });
  return clone(filtered);
}

function getMessageSummary() {
  const list = messageList;
  const effective = list.filter((item) => item.id > 0);
  const summary = {
    totalCount: effective.length,
    unreadCount: effective.filter((item) => item.unread).length,
    urgentUnreadCount: effective.filter((item) => item.unread && item.level === 'urgent').length,
    warningUnreadCount: effective.filter((item) => item.unread && item.level === 'warning').length,
    normalUnreadCount: effective.filter((item) => item.unread && item.level === 'normal').length,
    typeSummary: [],
    urgentItems: effective.filter((item) => item.level === 'urgent').slice(0, 3)
  };
  const typeMap = {};
  effective.forEach((item) => {
    const key = item.type || '系统公告';
    if (!typeMap[key]) {
      typeMap[key] = { type: key, count: 0, unreadCount: 0 };
    }
    typeMap[key].count += 1;
    if (item.unread) {
      typeMap[key].unreadCount += 1;
    }
  });
  summary.typeSummary = Object.values(typeMap).sort((a, b) => b.unreadCount - a.unreadCount || b.count - a.count);
  return clone(summary);
}

function getMessageDetail(id) {
  const found = messageList.find((item) => item.id === Number(id));
  return clone(found || messageList[0]);
}

function markMessageRead(id) {
  const idx = messageList.findIndex((item) => item.id === Number(id));
  if (idx >= 0) {
    messageList[idx].unread = false;
  }
  return true;
}

function markAllMessagesRead() {
  messageList.forEach((item) => {
    item.unread = false;
  });
  return true;
}

function getHealthDashboard(range = '7d') {
  const r = healthTrends[range] ? range : '7d';
  const trend = healthTrends[r];
  const latest = trend[trend.length - 1];
  return {
    range: r,
    trend: clone(trend),
    latest: clone(latest),
    riskTips: [
      '血压在午后有上升趋势，建议加强休息观察',
      '饮水量保持 1200ml 左右有助于稳定指标',
      '建议每周一次心血管功能复评'
    ]
  };
}

function getMedicalRecords() {
  return clone(medicalRecords);
}

function getAssessmentReports() {
  return clone(assessmentReports);
}

function getSchedules() {
  return clone(todaySchedules);
}

function getMealCalendar() {
  return clone(mealCalendar);
}

function getCareLogs() {
  return clone(careLogs);
}

function getActivityAlbums() {
  return clone(activityAlbums);
}

function likeAlbum(id) {
  const idx = activityAlbums.findIndex((item) => item.id === Number(id));
  if (idx === -1) {
    return null;
  }
  const current = activityAlbums[idx];
  const nextLiked = !current.liked;
  activityAlbums[idx].liked = nextLiked;
  activityAlbums[idx].likes += nextLiked ? 1 : -1;
  return clone(activityAlbums[idx]);
}

function getActivityAlbumComments(albumId) {
  const id = Number(albumId);
  return clone(activityAlbumComments[id] || []);
}

function addActivityAlbumComment(albumId, content) {
  const id = Number(albumId);
  if (!activityAlbumComments[id]) {
    activityAlbumComments[id] = [];
  }
  const item = {
    id: Math.floor(Math.random() * 100000) + 100,
    albumId: id,
    commenter: appState.profile.realName || '家属',
    content: String(content || '').trim() || '留言互动',
    time: formatDateTime(new Date()),
    mine: true
  };
  activityAlbumComments[id].unshift(item);
  const albumIdx = activityAlbums.findIndex((entry) => entry.id === id);
  if (albumIdx >= 0) {
    activityAlbums[albumIdx].comments = (activityAlbums[albumIdx].comments || 0) + 1;
  }
  return clone(item);
}

function getOutingRecords() {
  return clone(outingRecords);
}

function getEmergencyContacts() {
  return clone(emergencyContacts);
}

function getBillSummary() {
  return clone(billSummary);
}

function getPaymentGuard() {
  const list = getRechargeOrders();
  const guard = {
    elderName: billSummary.elderName,
    accountBalance: billSummary.accountBalance,
    outstanding: billSummary.outstanding,
    paidOrderCount: 0,
    pendingOrderCount: 0,
    closedOrderCount: 0,
    failedOrderCount: 0,
    lastPaidTime: '',
    recentOrders: list.slice(0, 8),
    tips: []
  };
  list.forEach((item) => {
    const status = item.status;
    if (status === 'PAID') {
      guard.paidOrderCount += 1;
      if (!guard.lastPaidTime && item.paidTime) {
        guard.lastPaidTime = item.paidTime;
      }
    } else if (status === 'PREPAY_CREATED') {
      guard.pendingOrderCount += 1;
    } else if (status === 'CLOSED') {
      guard.closedOrderCount += 1;
    } else if (status === 'FAILED') {
      guard.failedOrderCount += 1;
    }
  });
  if (Number(guard.outstanding) > 0) {
    guard.tips.push('本月仍有待缴账单，建议及时处理');
  } else {
    guard.tips.push('本月账单已结清，支付状态稳定');
  }
  if (guard.pendingOrderCount > 0) {
    guard.tips.push('存在待支付充值单，请尽快完成支付');
  }
  if (guard.closedOrderCount > 0 || guard.failedOrderCount > 0) {
    guard.tips.push('近期有异常充值订单，可查看详情后重新发起');
  }
  if (!billSummary.autoPayEnabled) {
    guard.tips.push('建议开启自动扣费提醒，降低忘记缴费风险');
  }
  return guard;
}

function toggleAutoPay(enable) {
  billSummary.autoPayEnabled = !!enable;
  return billSummary.autoPayEnabled;
}

function rechargeBalance(amount) {
  const value = Number(amount || 0);
  if (Number.isNaN(value) || value <= 0) {
    return null;
  }
  billSummary.accountBalance = Number((Number(billSummary.accountBalance) + value).toFixed(2));
  return {
    logId: Math.floor(Math.random() * 100000) + 1000,
    outTradeNo: `MOCK${Date.now()}`,
    rechargeAmount: value,
    accountBalance: billSummary.accountBalance,
    message: '充值成功，余额已更新'
  };
}

function createWechatRechargePrepay(amount) {
  const value = Number(amount || 0);
  if (Number.isNaN(value) || value <= 0) {
    return null;
  }
  const outTradeNo = `FMWX${Date.now()}`;
  const order = {
    outTradeNo,
    elderId: 101,
    elderName: '王秀兰',
    amount: value,
    channel: 'WECHAT_JSAPI',
    status: 'PREPAY_CREATED',
    statusText: '待支付',
    prepayId: `wx${Date.now()}`,
    wxTransactionId: '',
    createTime: '2026-03-08 18:00',
    paidTime: '',
    remark: '模拟微信预下单成功'
  };
  rechargeOrders.unshift(order);
  return {
    outTradeNo,
    rechargeAmount: value,
    amountFen: Math.round(value * 100),
    appId: 'wx-mock-appid',
    timeStamp: `${Math.floor(Date.now() / 1000)}`,
    nonceStr: `nonce${Math.floor(Math.random() * 100000)}`,
    payPackage: `prepay_id=${order.prepayId}`,
    signType: 'RSA',
    paySign: 'mock-sign',
    status: 'PREPAY_CREATED',
    message: '预下单成功'
  };
}

function getRechargeOrder(outTradeNo) {
  const found = rechargeOrders.find((item) => item.outTradeNo === outTradeNo);
  if (!found) {
    return null;
  }
  if (found.status === 'PREPAY_CREATED') {
    found.status = 'PAID';
    found.statusText = '已支付';
    found.wxTransactionId = `420000${Date.now()}`;
    found.paidTime = '2026-03-08 18:01';
    found.remark = '模拟支付成功';
    billSummary.accountBalance = Number((Number(billSummary.accountBalance) + Number(found.amount)).toFixed(2));
  }
  return clone(found);
}

function getRechargeOrders() {
  return clone(rechargeOrders);
}

function getBillHistory() {
  return clone(billHistory);
}

function getServiceCatalog() {
  return clone(serviceCatalog);
}

function getServiceOrders() {
  return clone(serviceOrders);
}

function createServiceOrder(serviceId) {
  const service = serviceCatalog.find((item) => item.id === Number(serviceId));
  if (!service) {
    return null;
  }
  const order = {
    id: Math.floor(Math.random() * 10000) + 800,
    name: service.name,
    amount: service.price,
    status: '待服务',
    date: '2026-03-09'
  };
  serviceOrders.unshift(order);
  return clone(order);
}

function canCancelMallOrder(order) {
  if (!order) {
    return false;
  }
  const status = Number(order.orderStatus || 0);
  return status === 1 || status === 2;
}

function cancelMallOrderHint(order) {
  if (!order) {
    return '订单不存在或已失效';
  }
  const status = Number(order.orderStatus || 0);
  if (status === 1 || status === 2) {
    return '可取消';
  }
  if (status === 3) {
    return '订单已完成，请改为申请退款';
  }
  if (status === 4) {
    return '订单已取消，无法重复取消';
  }
  if (status === 5) {
    return '订单已退款，无法取消';
  }
  return '当前订单状态不可取消';
}

function canRefundMallOrder(order) {
  if (!order) {
    return false;
  }
  const status = Number(order.orderStatus || 0);
  const payStatus = Number(order.payStatus || 0);
  if (status === 4 || status === 5) {
    return false;
  }
  return payStatus === 1 || status === 3;
}

function refundMallOrderHint(order) {
  if (!order) {
    return '订单不存在或已失效';
  }
  const status = Number(order.orderStatus || 0);
  const payStatus = Number(order.payStatus || 0);
  if (status === 4) {
    return '订单已取消，无需退款';
  }
  if (status === 5) {
    return '订单已退款，无需重复提交';
  }
  if (payStatus === 1 || status === 3) {
    return '可申请退款';
  }
  if (status === 1 && payStatus === 0) {
    return '订单尚未支付，建议先取消订单';
  }
  return '当前订单状态不可退款';
}

function withMallOrderActions(order) {
  const canCancel = canCancelMallOrder(order);
  const canRefund = canRefundMallOrder(order);
  return {
    ...order,
    canCancel,
    cancelHint: cancelMallOrderHint(order),
    canRefund,
    refundHint: refundMallOrderHint(order)
  };
}

function getMallProducts(options = {}) {
  const keyword = String(options.keyword || '').trim();
  const category = String(options.category || '').trim();
  let list = mallProducts.slice();
  if (category) {
    list = list.filter((item) => String(item.category || '').trim() === category);
  }
  if (keyword) {
    list = list.filter((item) =>
      String(item.productName || '').includes(keyword) || String(item.productCode || '').includes(keyword));
  }
  return clone(list);
}

function previewMallOrder(payload = {}) {
  const product = mallProducts.find((item) => item.id === Number(payload.productId));
  const qty = Math.max(1, Number(payload.qty || 1));
  if (!product) {
    return {
      allowed: false,
      status: 'PRODUCT_NOT_FOUND',
      message: '商品不存在',
      elderId: Number(payload.elderId || 101),
      productId: Number(payload.productId || 0),
      productName: '',
      qty,
      pointsRequired: 0,
      reasons: ['商品不存在或已下架']
    };
  }
  if (Number(product.currentStock || 0) < qty) {
    return {
      allowed: false,
      status: 'INSUFFICIENT_STOCK',
      message: '库存不足',
      elderId: Number(payload.elderId || 101),
      productId: product.id,
      productName: product.productName,
      qty,
      pointsRequired: qty * Number(product.pointsPrice || 0),
      reasons: ['库存不足，请调整购买数量']
    };
  }
  return {
    allowed: true,
    status: 'ALLOWED',
    message: '可下单',
    elderId: Number(payload.elderId || 101),
    productId: product.id,
    productName: product.productName,
    qty,
    pointsRequired: qty * Number(product.pointsPrice || 0),
    reasons: []
  };
}

function submitMallOrder(payload = {}) {
  const preview = previewMallOrder(payload);
  if (!preview.allowed) {
    return {
      allowed: false,
      status: preview.status,
      message: preview.message,
      preview
    };
  }
  const product = mallProducts.find((item) => item.id === preview.productId);
  if (product) {
    product.currentStock = Math.max(0, Number(product.currentStock || 0) - preview.qty);
    if (product.currentStock <= 0) {
      product.stockStatus = 'OUT';
      product.statusText = '库存不足';
    } else if (product.currentStock <= 5) {
      product.stockStatus = 'LOW';
      product.statusText = '库存紧张';
    } else {
      product.stockStatus = 'OK';
      product.statusText = '可下单';
    }
  }
  const orderId = Date.now();
  const unitPrice = Number(product && product.price ? product.price : 0);
  const totalAmount = Number((unitPrice * preview.qty).toFixed(2));
  const item = {
    orderId,
    orderNo: `SO${orderId}`,
    elderId: preview.elderId,
    elderName: (baseElders.find((elder) => elder.elderId === preview.elderId) || {}).elderName || '老人',
    productId: preview.productId,
    productName: preview.productName,
    quantity: preview.qty,
    unitPrice,
    totalAmount,
    pointsUsed: preview.pointsRequired,
    orderStatus: 1,
    orderStatusText: '待处理',
    payStatus: 0,
    payStatusText: '待支付',
    createTime: formatDateTime(new Date()),
    payTime: ''
  };
  mallOrders.unshift(item);
  return {
    allowed: true,
    status: 'ALLOWED',
    message: '下单成功',
    orderId,
    orderNo: item.orderNo,
    pointsDeducted: preview.pointsRequired,
    balanceAfter: Math.max(0, 1000 - preview.pointsRequired),
    preview
  };
}

function getMallOrders(options = {}) {
  const elderId = Number(options.elderId || 0);
  const list = !elderId
    ? mallOrders
    : mallOrders.filter((item) => Number(item.elderId) === elderId);
  return clone(list.map((item) => withMallOrderActions(item)));
}

function getMallOrderDetail(orderId) {
  const order = mallOrders.find((item) => Number(item.orderId) === Number(orderId));
  if (!order) {
    return null;
  }
  return clone({
    summary: withMallOrderActions(order),
    items: [
      {
        productId: order.productId,
        productName: order.productName,
        quantity: order.quantity,
        unitPrice: order.unitPrice,
        amount: order.totalAmount
      }
    ],
    riskReasons: [],
    fifoLogs: [
      {
        batchNo: `MOCK-${String(order.productId).slice(-4)}`,
        quantity: order.quantity,
        expireDate: '2027-12-31'
      }
    ]
  });
}

function cancelMallOrder(orderId) {
  const order = mallOrders.find((item) => Number(item.orderId) === Number(orderId));
  if (!order) {
    return {
      success: false,
      action: 'CANCEL',
      message: '订单不存在',
      canCancel: false,
      cancelHint: cancelMallOrderHint(null),
      canRefund: false,
      refundHint: refundMallOrderHint(null)
    };
  }
  if (!canCancelMallOrder(order)) {
    return {
      orderId: order.orderId,
      action: 'CANCEL',
      success: false,
      message: cancelMallOrderHint(order),
      orderStatus: order.orderStatus,
      orderStatusText: order.orderStatusText,
      payStatus: order.payStatus,
      payStatusText: order.payStatusText,
      canCancel: false,
      cancelHint: cancelMallOrderHint(order),
      canRefund: canRefundMallOrder(order),
      refundHint: refundMallOrderHint(order)
    };
  }
  order.orderStatus = 4;
  order.orderStatusText = '已取消';
  if (Number(order.payStatus || 0) === 1) {
    order.payStatus = 2;
    order.payStatusText = '已退款';
  } else {
    order.payStatus = 0;
    order.payStatusText = '待支付';
  }
  return {
    orderId: order.orderId,
    action: 'CANCEL',
    success: true,
    message: '订单已取消',
    orderStatus: order.orderStatus,
    orderStatusText: order.orderStatusText,
    payStatus: order.payStatus,
    payStatusText: order.payStatusText,
    canCancel: false,
    cancelHint: cancelMallOrderHint(order),
    canRefund: false,
    refundHint: refundMallOrderHint(order)
  };
}

function refundMallOrder(orderId) {
  const order = mallOrders.find((item) => Number(item.orderId) === Number(orderId));
  if (!order) {
    return {
      success: false,
      action: 'REFUND',
      message: '订单不存在',
      canCancel: false,
      cancelHint: cancelMallOrderHint(null),
      canRefund: false,
      refundHint: refundMallOrderHint(null)
    };
  }
  if (!canRefundMallOrder(order)) {
    return {
      orderId: order.orderId,
      action: 'REFUND',
      success: false,
      message: refundMallOrderHint(order),
      orderStatus: order.orderStatus,
      orderStatusText: order.orderStatusText,
      payStatus: order.payStatus,
      payStatusText: order.payStatusText,
      canCancel: canCancelMallOrder(order),
      cancelHint: cancelMallOrderHint(order),
      canRefund: false,
      refundHint: refundMallOrderHint(order)
    };
  }
  order.orderStatus = 5;
  order.orderStatusText = '已退款';
  order.payStatus = 2;
  order.payStatusText = '已退款';
  return {
    orderId: order.orderId,
    action: 'REFUND',
    success: true,
    message: '退款申请已处理',
    orderStatus: order.orderStatus,
    orderStatusText: order.orderStatusText,
    payStatus: order.payStatus,
    payStatusText: order.payStatusText,
    canCancel: false,
    cancelHint: cancelMallOrderHint(order),
    canRefund: false,
    refundHint: refundMallOrderHint(order)
  };
}

function submitFeedback(payload = {}) {
  const type = String(payload.feedbackType || 'EVALUATION').toUpperCase() === 'COMPLAINT'
    ? 'COMPLAINT'
    : 'EVALUATION';
  const item = {
    id: Math.floor(Math.random() * 100000) + 900,
    feedbackType: type,
    serviceType: payload.serviceType || (type === 'COMPLAINT' ? '投诉建议' : '服务反馈'),
    score: payload.score || null,
    content: payload.content || '',
    status: 'PENDING',
    reply: '已受理，服务团队会尽快跟进。',
    createTime: formatDateTime(new Date()),
    updateTime: formatDateTime(new Date())
  };
  appState.feedbackRecords.unshift(item);
  return true;
}

function getFeedbackRecords(pageNo = 1, pageSize = 20) {
  const pn = Math.max(1, Number(pageNo) || 1);
  const ps = Math.max(1, Number(pageSize) || 20);
  const start = (pn - 1) * ps;
  return clone(appState.feedbackRecords.slice(start, start + ps));
}

function getAffectionMoments() {
  return clone(affectionMoments);
}

function getFestivalTemplates() {
  return clone(festivalTemplates);
}

function addAffectionMoment(payload = {}) {
  const type = payload.type || 'text';
  const title = payload.title || '亲情互动';
  const desc = payload.desc || '';
  affectionMoments.unshift({
    id: Math.floor(Math.random() * 10000) + 10,
    type,
    title,
    time: '2026-03-08 18:00',
    desc,
    mediaType: payload.mediaType || '',
    mediaUrl: payload.mediaUrl || '',
    mediaName: payload.mediaName || ''
  });
}

function getNotificationSettings() {
  return clone(appState.notificationSettings);
}

function updateNotificationSettings(nextState) {
  appState.notificationSettings = {
    ...appState.notificationSettings,
    ...nextState
  };
  return getNotificationSettings();
}

function bindWechatNotifyOpenId(payload = {}) {
  const openId = String(payload.openId || '').trim() || `oMock${Math.random().toString(36).slice(2, 12)}`;
  appState.wechatNotifyOpenId = openId;
  return {
    bound: true,
    openId,
    message: '微信通知接收绑定成功'
  };
}

function getCapabilityStatus() {
  const smsEnabled = true;
  const smsProvider = 'mock';
  const smsDebugReturnCode = true;
  const wechatPayEnabled = false;
  const wechatNotifyEnabled = true;
  const wechatNotifyBound = !!appState.wechatNotifyOpenId;
  const securityPasswordEnabled = true;
  const legacyApiEnabled = false;
  const legacyApiSunsetDate = '2026-09-30';
  const legacyApiSunsetReached = false;
  return {
    smsEnabled,
    smsProvider,
    smsDebugReturnCode,
    wechatPayEnabled,
    wechatNotifyEnabled,
    wechatNotifyBound,
    securityPasswordEnabled,
    legacyApiEnabled,
    legacyApiSunsetDate,
    legacyApiSunsetReached,
    checkedAt: formatDateTime(new Date()),
    items: [
      {
        code: 'SMS_CODE',
        title: '短信验证码',
        status: 'MOCK',
        hint: '当前为 mock 通道，联调后请切换真实短信服务。',
        actionPath: '/pages/settings-security/index'
      },
      {
        code: 'WECHAT_NOTIFY',
        title: '微信通知投递',
        status: wechatNotifyBound ? 'READY' : 'BIND_REQUIRED',
        hint: wechatNotifyBound ? '通知接收已绑定，可投递服务通知。' : '尚未绑定 openId，请先在小程序内授权。',
        actionPath: '/pages/notification-settings/index'
      },
      {
        code: 'WECHAT_PAY',
        title: '微信充值支付',
        status: wechatPayEnabled ? 'READY' : 'OFF',
        hint: wechatPayEnabled ? '预下单与支付回调已可用。' : '当前未开启真实微信支付。',
        actionPath: '/pages/payment/index'
      },
      {
        code: 'SECURITY_PASSWORD',
        title: '密码二次校验',
        status: securityPasswordEnabled ? 'READY' : 'OPTIONAL',
        hint: securityPasswordEnabled ? '默认使用登录密码验证，已支持独立密码。' : '可在隐私设置中开启密码验证。',
        actionPath: '/pages/settings-security/index'
      },
      {
        code: 'LEGACY_API',
        title: '旧接口下线状态',
        status: legacyApiEnabled ? 'DEPRECATED' : 'OFFLINE',
        hint: legacyApiEnabled ? '旧接口已废弃，建议全部迁移聚合接口。' : '旧接口已下线，统一使用 family 聚合接口。',
        actionPath: ''
      }
    ]
  };
}

function getSecuritySettings() {
  appState.securitySettings.verifyWithSmsCode = false;
  appState.securitySettings.verifyWithPassword = true;
  return clone(appState.securitySettings);
}

function updateSecuritySettings(nextState) {
  appState.securitySettings = {
    ...appState.securitySettings,
    ...nextState,
    verifyWithSmsCode: false,
    verifyWithPassword: true
  };
  return getSecuritySettings();
}

function setSecurityPassword(password = '') {
  const value = String(password || '').trim();
  if (value.length < 6) {
    return false;
  }
  appState.securityPassword = value;
  appState.securitySettings.verifyWithPassword = true;
  appState.securitySettings.hasIndependentPassword = true;
  return true;
}

function verifySecurityPassword(payload = {}) {
  const password = String(payload.password || '').trim();
  const passed = appState.securityPassword
    ? appState.securityPassword === password
    : appState.loginPassword === password;
  return {
    passed,
    message: passed ? '密码验证通过' : '密码错误，请重试'
  };
}

function getCommunicationMessages() {
  return clone(appState.communicationMessages);
}

function getCommunicationTemplates() {
  return clone(communicationTemplates);
}

function sendCommunicationMessage(payload = {}) {
  const item = {
    id: Math.floor(Math.random() * 100000) + 10,
    targetRole: payload.targetRole || '客服中心',
    msgType: payload.msgType || 'text',
    content: payload.content || '',
    mediaUrl: payload.mediaUrl || '',
    mediaName: payload.mediaName || '',
    mediaDurationSec: payload.mediaDurationSec || null,
    transcript: payload.transcript || '',
    time: '2026-03-08 18:00',
    sender: appState.profile.realName || '家属',
    status: '已发送'
  };
  appState.communicationMessages.unshift(item);
  return clone(item);
}

function getHelpFaqs() {
  return clone(appState.helpFaqs);
}

function getTodoCenter() {
  const guard = getPaymentGuard();
  const rawItems = [];
  messageList
    .filter((item) => item.unread)
    .slice(0, 3)
    .forEach((item) => {
      rawItems.push({
        type: 'MESSAGE',
        priority: item.level === 'urgent' ? 'URGENT' : (item.level === 'warning' ? 'HIGH' : 'NORMAL'),
        title: item.title,
        description: item.content,
        actionPath: `/pages/message-detail/index?id=${item.id}`,
        dueTime: item.time,
        status: '待处理'
      });
    });
  if (Number(guard.outstanding) > 0) {
    rawItems.push({
      type: 'PAYMENT',
      priority: 'HIGH',
      title: '本月账单待缴费',
      description: `当前仍有 ¥${guard.outstanding} 未结清，建议尽快处理`,
      actionPath: '/pages/payment/index',
      dueTime: '今日内',
      status: '待处理'
    });
  }
  if (guard.pendingOrderCount > 0) {
    rawItems.push({
      type: 'PAYMENT',
      priority: 'HIGH',
      title: '存在待支付充值单',
      description: `有 ${guard.pendingOrderCount} 笔订单待完成，可进入详情继续支付`,
      actionPath: '/pages/payment-guard/index',
      dueTime: '尽快',
      status: '待处理'
    });
  }
  todaySchedules
    .filter((item) => item.status !== '已完成')
    .slice(0, 4)
    .forEach((item) => {
      rawItems.push({
        type: 'SCHEDULE',
        priority: item.status === '进行中' ? 'HIGH' : 'NORMAL',
        title: item.name,
        description: `执行人：${item.owner || '护理组'}`,
        actionPath: '/pages/schedule/index',
        dueTime: item.time,
        status: item.status
      });
    });
  rawItems.push({
    type: 'SECURITY',
    priority: 'NORMAL',
    title: '建议开启二次验证',
    description: '可在安全设置中开启验证码校验，提升隐私保护等级',
    actionPath: '/pages/settings-security/index',
    dueTime: '',
    status: '建议优化'
  });

  const nowMs = Date.now();
  const items = rawItems.map((item) => {
    const todoKey = buildTodoKey(item);
    const actionState = appState.todoActionState[todoKey];
    const result = {
      ...item,
      todoKey,
      actionable: item.status !== '建议优化',
      done: false,
      snoozed: false
    };
    if (!actionState) {
      return result;
    }
    if (actionState.action === 'DONE') {
      result.done = true;
      result.actionable = false;
      result.status = '已处理';
      return result;
    }
    if (actionState.action === 'SNOOZE') {
      if (actionState.snoozeUntilMs && actionState.snoozeUntilMs > nowMs) {
        result.snoozed = true;
        result.actionable = false;
        result.status = '稍后提醒';
        result.dueTime = actionState.snoozeUntilText || result.dueTime;
        return result;
      }
      delete appState.todoActionState[todoKey];
    }
    return result;
  });

  const completedHintCount = todaySchedules.filter((item) => item.status === '已完成').length;
  return {
    summary: {
      urgentCount: items.filter((item) => item.priority === 'URGENT' && !item.done).length,
      dueTodayCount: items.filter((item) => item.status !== '建议优化' && !item.done && !item.snoozed).length,
      completedHintCount
    },
    items,
    refreshTime: '今天 18:00'
  };
}

function handleTodoAction(payload = {}) {
  const todoKey = payload.todoKey || '';
  const action = String(payload.action || '').toUpperCase();
  if (!todoKey || !action) {
    return null;
  }
  if (action === 'DONE') {
    appState.todoActionState[todoKey] = {
      action: 'DONE',
      doneAt: formatDateTime(new Date())
    };
    return {
      todoKey,
      action,
      status: 'DONE',
      message: '该事项已标记为完成'
    };
  }
  if (action === 'SNOOZE') {
    const minutes = Math.max(5, Math.min(Number(payload.snoozeMinutes || 120), 24 * 60));
    const remindAt = new Date(Date.now() + minutes * 60 * 1000);
    appState.todoActionState[todoKey] = {
      action: 'SNOOZE',
      snoozeMinutes: minutes,
      snoozeUntilMs: remindAt.getTime(),
      snoozeUntilText: formatDateTime(remindAt)
    };
    return {
      todoKey,
      action,
      status: 'SNOOZED',
      message: `已稍后提醒 ${minutes} 分钟`,
      nextRemindTime: formatDateTime(remindAt)
    };
  }
  if (action === 'UNDO') {
    delete appState.todoActionState[todoKey];
    return {
      todoKey,
      action,
      status: 'TODO',
      message: '已恢复为待处理'
    };
  }
  return null;
}

function getProfile() {
  return clone(appState.profile);
}

function updateProfile(nextProfile) {
  appState.profile = {
    ...appState.profile,
    ...nextProfile
  };
  return getProfile();
}

function getFamilyBindings() {
  return clone(appState.familyBindings);
}

function bindFamilyElder(payload = {}) {
  const idCardNo = String(payload.elderIdCardNo || '').trim().toUpperCase();
  let elderId = Number(payload.elderId);
  if (!elderId && idCardNo) {
    if (!appState.idCardBindingMap[idCardNo]) {
      const seed = idCardNo.slice(-6).replace(/\D/g, '');
      appState.idCardBindingMap[idCardNo] = Number(seed || Date.now().toString().slice(-6));
    }
    elderId = Number(appState.idCardBindingMap[idCardNo]);
  }
  if (!elderId) {
    return null;
  }
  const relation = payload.relation || '家属';
  const isPrimary = Number(payload.isPrimary) === 1;
  if (isPrimary) {
    appState.familyBindings = appState.familyBindings.map((item) => ({ ...item, isPrimary: false }));
  }
  const existing = appState.familyBindings.find((item) => item.elderId === elderId);
  const baseElder = baseElders.find((item) => item.elderId === elderId);
  const elderName = baseElder ? baseElder.elderName : `老人${elderId}`;
  if (existing) {
    existing.relation = relation;
    existing.isPrimary = isPrimary;
    existing.elderName = elderName;
    return clone(existing);
  }
  const appended = {
    elderId,
    elderName,
    relation,
    isPrimary
  };
  appState.familyBindings.unshift(appended);
  return clone(appended);
}

function removeFamilyBinding(elderId) {
  appState.familyBindings = appState.familyBindings.filter((item) => item.elderId !== Number(elderId));
  return getFamilyBindings();
}

function sendFamilySmsCode(payload = {}) {
  const phone = String(payload.phone || '').trim();
  const code = `${Math.floor(100000 + Math.random() * 900000)}`;
  smsState.byPhone[phone] = code;
  return {
    bizNo: `MOCK-SMS-${Date.now()}`,
    expireSeconds: 300,
    retryAfterSeconds: 60,
    provider: 'mock',
    message: '验证码发送成功',
    debugCode: code
  };
}

function verifyFamilySmsCode(payload = {}) {
  const phone = String(payload.phone || '').trim();
  const code = String(payload.verifyCode || '').trim();
  const expected = smsState.byPhone[phone] || '123456';
  const passed = code === expected;
  return {
    passed,
    remainingAttempts: passed ? 5 : 4,
    message: passed ? '验证码校验通过' : '验证码错误，请重试'
  };
}

function sendSecuritySmsCode(scene = 'SECURITY') {
  const code = `${Math.floor(100000 + Math.random() * 900000)}`;
  smsState.securityCode = code;
  return {
    bizNo: `MOCK-SEC-${Date.now()}`,
    expireSeconds: 300,
    retryAfterSeconds: 60,
    provider: 'mock',
    message: `${scene} 验证码发送成功`,
    debugCode: code
  };
}

function verifySecuritySmsCode(payload = {}) {
  const code = String(payload.verifyCode || '').trim();
  const expected = smsState.securityCode || '123456';
  const passed = code === expected;
  return {
    passed,
    remainingAttempts: passed ? 5 : 4,
    message: passed ? '验证码校验通过' : '验证码错误，请重试'
  };
}

function uploadVoiceMessage(filePath = '', options = {}) {
  const now = Date.now();
  const ext = (options.ext || 'm4a').replace(/[^a-z0-9]/gi, '').toLowerCase() || 'm4a';
  return {
    fileName: `voice-${now}.${ext}`,
    originalFileName: filePath ? filePath.split('/').pop() : `voice.${ext}`,
    fileUrl: `https://mock.smartcare.local/family/voice/${now}.${ext}`,
    fileType: 'audio/' + ext,
    fileSize: 1024 * 120
  };
}

function generateAiAssessmentReports(payload = {}) {
  const elderId = Number(payload.elderId || 101);
  const reportType = String(payload.reportType || 'ALL').toUpperCase();
  const generatedTypes = reportType === 'TCM'
    ? ['TCM']
    : reportType === 'CARDIO'
      ? ['CARDIO']
      : ['TCM', 'CARDIO'];
  generatedTypes.forEach((type) => {
    const id = Math.floor(Math.random() * 100000) + 4000;
    assessmentReports.unshift({
      id,
      reportName: type === 'TCM' ? 'AI中医体质评估' : '心血管功能评估',
      date: formatDateTime(new Date()).slice(0, 10),
      orgName: '智养云照护中心',
      summary: type === 'TCM'
        ? '体质分析已完成，建议规律作息与温和运动。'
        : '心血管评分已更新，建议持续低盐饮食与血压监测。',
      score: type === 'TCM' ? '82 分' : '78 分',
      risk: type === 'TCM' ? '一般风险' : '中等风险',
      type: type === 'TCM' ? '中医体质' : '心血管评估',
      fileName: type === 'TCM' ? `tcm-ai-${id}.pdf` : `cvd-ai-${id}.pdf`,
      fileUrl: `https://example.com/mock-reports/ai-${id}.pdf`,
      elderId
    });
  });
  return {
    elderId,
    generatedTypes,
    generatedCount: generatedTypes.length,
    message: 'AI评估已生成'
  };
}

module.exports = {
  getHomeDashboard,
  getWeeklyBrief,
  getWeeklyBriefHistory,
  getMessages,
  getMessageSummary,
  getMessageDetail,
  markMessageRead,
  markAllMessagesRead,
  getHealthDashboard,
  getMedicalRecords,
  getAssessmentReports,
  getSchedules,
  getMealCalendar,
  getCareLogs,
  getActivityAlbums,
  likeAlbum,
  getActivityAlbumComments,
  addActivityAlbumComment,
  getOutingRecords,
  getEmergencyContacts,
  getBillSummary,
  getPaymentGuard,
  rechargeBalance,
  createWechatRechargePrepay,
  getRechargeOrder,
  getRechargeOrders,
  toggleAutoPay,
  getBillHistory,
  getServiceCatalog,
  getServiceOrders,
  createServiceOrder,
  getMallProducts,
  previewMallOrder,
  submitMallOrder,
  getMallOrders,
  getMallOrderDetail,
  cancelMallOrder,
  refundMallOrder,
  submitFeedback,
  getFeedbackRecords,
  getAffectionMoments,
  getFestivalTemplates,
  addAffectionMoment,
  getNotificationSettings,
  updateNotificationSettings,
  getSecuritySettings,
  updateSecuritySettings,
  setSecurityPassword,
  verifySecurityPassword,
  getCommunicationMessages,
  getCommunicationTemplates,
  sendCommunicationMessage,
  getHelpFaqs,
  getTodoCenter,
  handleTodoAction,
  getProfile,
  updateProfile,
  sendFamilySmsCode,
  verifyFamilySmsCode,
  sendSecuritySmsCode,
  verifySecuritySmsCode,
  uploadVoiceMessage,
  generateAiAssessmentReports,
  bindWechatNotifyOpenId,
  getCapabilityStatus,
  bindFamilyElder,
  getFamilyBindings,
  removeFamilyBinding
};
