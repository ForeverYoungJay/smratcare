<template>
  <PageContainer title="长者管理 Resident 360" subTitle="长者全周期管理：售前跟进 → 入院办理 → 在院服务 → 变更与退院">
    <a-alert
      type="info"
      show-icon
      message="主页用于统一查看长者状态、风险、护理、合同、费用与跨部门协同事件。"
      style="margin-bottom: 16px"
    />

    <a-row :gutter="16">
      <a-col :xs="24" :lg="12" v-for="card in cards" :key="card.key" style="margin-bottom: 16px">
        <a-card :title="card.title" class="card-elevated" :bordered="false">
          <template #extra>
            <a-tag :color="card.tagColor">{{ card.tag }}</a-tag>
          </template>

          <div v-for="line in card.lines" :key="line" class="line-item">{{ line }}</div>

          <a-space wrap style="margin-top: 12px">
            <a-button v-for="action in card.actions" :key="action.label" :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">
              {{ action.label }}
            </a-button>
            <a-button
              v-if="card.key === 'status-event' && currentStatus === 'OUT'"
              @click="go('/elder/outing?mode=return')"
            >
              外出返院登记
            </a-button>
            <a-button
              v-if="card.key === 'status-event' && hasUnclosedIncident"
              danger
              @click="go('/elder/incident?tab=unclosed')"
            >
              事故结案
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()

const residentId = 1001
const eventId = 8801
const currentStatus = ref<'INTENT' | 'TRIAL' | 'IN' | 'OUT' | 'MEDICAL_OUT' | 'DISCHARGE_PENDING' | 'DISCHARGED' | 'DECEASED'>('OUT')
const hasUnclosedIncident = ref(true)

function go(path: string) {
  router.push(path)
}

const cards = [
  {
    key: 'status-event',
    title: '卡片1：状态与事件总览',
    tag: '全院协同',
    tagColor: 'blue',
    lines: [
      '当前状态：外出',
      '最近事件：外出登记 2026-02-26 09:30（处理人：护理组长）',
      '本月事件：外出 6 / 来访 11 / 事故 2 / 退住 1 / 就医 3',
      '待处理：外出超时未返院 1，未闭环事故 1，待审批退住 1'
    ],
    actions: [
      { label: '发起状态变更', path: `/elder/status-change?residentId=${residentId}`, primary: true },
      { label: '查看事件中心', path: `/elder/change-log?residentId=${residentId}&filter=this_month` },
      { label: '最近一次事件', path: `/elder/change-log?residentId=${residentId}&eventId=${eventId}` }
    ]
  },
  {
    key: 'risk-level',
    title: '卡片2：风险预警与等级',
    tag: '风险驱动',
    tagColor: 'red',
    lines: [
      '风险标签：跌倒(红) / 压疮(黄) / 过敏(绿)，更新时间 2026-02-26 08:00',
      '最新评估：2026-02-20，评估等级 B，建议护理等级 N3',
      '风险处置任务：2 条未完成，1 条待复评'
    ],
    actions: [
      { label: '做评估', path: `/assessment/ability/admission?residentId=${residentId}&template=GBT42195`, primary: true },
      { label: '中医体质评估', path: `/medical-care/assessment/tcm?residentId=${residentId}&from=resident360` },
      { label: '心血管风险评估', path: `/medical-care/assessment/cvd?residentId=${residentId}&from=resident360` },
      { label: '创建风险处置任务', path: `/oa/work-execution/task?residentId=${residentId}&category=risk` },
      { label: '查看历史评估', path: `/assessment/ability/archive?residentId=${residentId}` }
    ]
  },
  {
    key: 'care-execution',
    title: '卡片3：护理执行',
    tag: '服务质量',
    tagColor: 'cyan',
    lines: [
      '今日任务：应做 26 / 已做 19 / 超时 2',
      '最近 7 天完成率：96% · 94% · 97% · 95% · 93% · 98% · 96%',
      '异常：漏做 1，超时 2，投诉关联 0',
      '当前护理等级：N3（防跌倒、夜间巡视、定时翻身）'
    ],
    actions: [
      { label: '查看今日任务', path: `/care/workbench/task-board?residentId=${residentId}&date=today`, primary: true },
      { label: '查看护理计划', path: `/care/workbench/plan?residentId=${residentId}` },
      { label: '异常清单', path: `/care/workbench/task-board?residentId=${residentId}&filter=overdue_or_missed` },
      { label: '扫码执行', path: `/care/workbench/qr?residentId=${residentId}` }
    ]
  },
  {
    key: 'handover',
    title: '卡片4：沟通与交接',
    tag: '跨班组',
    tagColor: 'purple',
    lines: [
      '今日交接重点：夜间防走失；吞咽风险持续观察；1 条未完成事项',
      '家属沟通摘要：最近 3 条已记录，含一次费用沟通',
      '重要提醒：青霉素过敏、禁忌食材、纠纷风险低'
    ],
    actions: [
      { label: '新增交接记录', path: `/oa/work-execution/daily-report?residentId=${residentId}`, primary: true },
      { label: '新增沟通记录', path: `/survey/submit?residentId=${residentId}` },
      { label: '查看全部记录', path: `/oa/work-execution/task?residentId=${residentId}` }
    ]
  },
  {
    key: 'contract',
    title: '卡片5：入住/合同摘要',
    tag: '入住链路',
    tagColor: 'green',
    lines: [
      '入住日期：2025-09-08，合同状态：已签',
      '合同到期倒计时：23 天（<30 天预警）',
      '套餐：护理套餐 A（30 天）+ 服务套餐 B（30 天）',
      '票据：收据 6、发票 2，存在 1 条缺失提醒'
    ],
    actions: [
      { label: '查看合同', path: `/elder/contracts-invoices?residentId=${residentId}`, primary: true },
      { label: '发起续签/变更', path: `/marketing/contract-management?residentId=${residentId}&type=renew_or_change` },
      { label: '上传合同/票据', path: `/elder/contracts-invoices?residentId=${residentId}&tab=attachments` },
      { label: '入住办理', path: `/elder/admission-processing?residentId=${residentId}&step=bed_assign` }
    ]
  },
  {
    key: 'finance',
    title: '卡片6：费用与账户',
    tag: '业财一体',
    tagColor: 'orange',
    lines: [
      '账户余额：1500，押金：6000，当月账单：2200',
      '欠费标识：无，自动催缴：开启（最近提醒 2026-02-25）',
      '近 3 笔交易：缴费 2000 / 扣费 500 / 退款 200'
    ],
    actions: [
      { label: '查看账单', path: `/finance/bill?residentId=${residentId}&period=this_month`, primary: true },
      { label: '缴费/充值', path: `/finance/resident-bill-payment?residentId=${residentId}` },
      { label: '退住结算', path: `/elder/discharge-settlement?residentId=${residentId}` },
      { label: '费用明细', path: `/finance/account-log?residentId=${residentId}` }
    ]
  },
  {
    key: 'medical',
    title: '卡片7：医疗健康摘要',
    tag: '医护入口',
    tagColor: 'magenta',
    lines: [
      '生命体征：血压 138/92（异常），血糖 6.2，体温 36.7',
      '医嘱：总数 12，待执行 3',
      '用药：今日应服 5，已服 4，剩余药品 1 项预警',
      '最近就医：2026-02-10，市第一医院'
    ],
    actions: [
      { label: '进入医护工作站', path: `/life/health-basic?residentId=${residentId}`, primary: true },
      { label: '查看医嘱', path: `/life/health-basic?residentId=${residentId}&tab=orders` },
      { label: '用药登记', path: `/life/health-basic?residentId=${residentId}&tab=medication` },
      { label: '外出就医登记', path: `/elder/status-change?residentId=${residentId}&event=medical_out` }
    ]
  },
  {
    key: 'service-package',
    title: '卡片8：服务与套餐',
    tag: '非医疗',
    tagColor: 'geekblue',
    lines: [
      '已开通：护理、康复、陪诊、洗浴',
      '预约待执行：3 条（最近一次 2026-02-27 10:00）',
      '套餐最近变更：2026-02-18 由 B 升级至 A'
    ],
    actions: [
      { label: '增购服务', path: `/care/elder-packages?residentId=${residentId}`, primary: true },
      { label: '服务预约', path: `/care/workbench/task?residentId=${residentId}&mode=booking` },
      { label: '查看服务计划', path: `/care/workbench/plan?residentId=${residentId}` },
      { label: '服务报表', path: `/stats/elder-info?residentId=${residentId}&period=month` }
    ]
  },
  {
    key: 'dining',
    title: '卡片9：膳食与点餐',
    tag: '餐饮系统',
    tagColor: 'lime',
    lines: [
      '膳食方案：糖尿病餐 + 低盐软食',
      '禁忌/过敏：海鲜、花生',
      '今日订餐：已订并送达',
      '近 7 天异常：退餐 1 次'
    ],
    actions: [
      { label: '查看今日订餐', path: `/dining/order?residentId=${residentId}&date=today`, primary: true },
      { label: '修改膳食方案', path: `/dining/recipe?residentId=${residentId}` },
      { label: '餐饮统计', path: `/dining/stats?residentId=${residentId}&period=week` }
    ]
  },
  {
    key: 'activity',
    title: '卡片10：活动与相册',
    tag: '生活质量',
    tagColor: 'gold',
    lines: [
      '本周活动：参与 3 次，报名 4 次，缺席 1 次',
      '最新照片：4 张（家属可见 3 张）',
      '未来 7 天预告：生日会、手工课、康复操'
    ],
    actions: [
      { label: '查看活动日历', path: `/life/activity?residentId=${residentId}`, primary: true },
      { label: '上传照片', path: `/life/activity?residentId=${residentId}&tab=album-upload` },
      { label: '查看相册', path: `/life/activity?residentId=${residentId}&tab=album` }
    ]
  },
  {
    key: 'family-feedback',
    title: '卡片11：家属互动与满意度',
    tag: '反馈闭环',
    tagColor: 'volcano',
    lines: [
      '最近评分：护理 4.8 / 餐饮 4.3 / 环境 4.6 / 沟通 4.7',
      '最新留言：建议增加晚间巡视频次',
      '处理状态：处理中（预计 2026-02-27 结案）'
    ],
    actions: [
      { label: '发送问卷', path: `/survey/submit?residentId=${residentId}`, primary: true },
      { label: '查看反馈', path: `/survey/stats?residentId=${residentId}` },
      { label: '创建改进任务', path: `/oa/work-execution/task?residentId=${residentId}&source=survey` }
    ]
  },
  {
    key: 'attachments',
    title: '卡片12：文档与附件',
    tag: '证据链',
    tagColor: 'default',
    lines: [
      '分类计数：合同 2、票据 8、评估报告 4、就医单据 3、告知书 2',
      '缺失提醒：发票未关联 1 条',
      '最近上传：2026-02-25 14:10 评估报告.pdf'
    ],
    actions: [
      { label: '上传附件', path: `/elder/contracts-invoices?residentId=${residentId}&tab=attachments`, primary: true },
      { label: '查看全部', path: `/elder/contracts-invoices?residentId=${residentId}&tab=all` },
      { label: '下载打包', path: `/elder/contracts-invoices?residentId=${residentId}&action=zip` },
      { label: '关联到事件/合同/账单', path: `/elder/contracts-invoices?residentId=${residentId}&action=bind` }
    ]
  }
]
</script>

<style scoped>
.line-item {
  line-height: 1.9;
  color: #262626;
}
</style>
