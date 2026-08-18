<template>
  <div class="excel-report-panel">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="账期">
          <a-date-picker v-model:value="query.month" picker="month" :allow-clear="false" style="width: 160px" />
        </a-form-item>
        <a-form-item label="押金状态">
          <a-select v-model:value="query.depositStatus" allow-clear placeholder="全部状态" style="width: 150px">
            <a-select-option value="UNPAID">未缴</a-select-option>
            <a-select-option value="PARTIAL">部分缴</a-select-option>
            <a-select-option value="PAID">已缴清</a-select-option>
            <a-select-option value="CLOSED">已结清</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <div class="form-tip">账期只作用于代养费登记表、电费月报与欠费催缴清单；押金台账按状态导出当前全量。</div>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col v-for="item in reports" :key="item.key" :xs="24" :md="12">
        <a-card class="card-elevated report-card" :bordered="false">
          <div class="report-card__head">
            <strong>{{ item.title }}</strong>
            <a-tag color="green">xlsx</a-tag>
          </div>
          <div class="report-card__desc">{{ item.desc }}</div>
          <div class="report-card__cols">列：{{ item.columns }}</div>
          <a-button
            type="primary"
            :loading="downloading === item.key"
            @click="download(item)"
          >导出 {{ item.title }}</a-button>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { exportXlsxByRequest } from '../../../utils/export'

type ReportItem = {
  key: string
  title: string
  desc: string
  columns: string
  path: string
  useMonth: boolean
  useDepositStatus?: boolean
}

const query = reactive({
  month: dayjs().startOf('month') as any,
  depositStatus: undefined as string | undefined
})
const downloading = ref('')

const reports: ReportItem[] = [
  {
    key: 'care-fee-register',
    title: '代养费登记表',
    desc: '按账期列出每位长者的应收、三项抵扣明细、实收现金与欠费。',
    columns: '长者 / 房间床位 / 账单月份 / 应收 / 长护险抵扣 / 折扣减免 / 消费券抵扣 / 实收现金 / 欠费 / 状态',
    path: '/api/finance/report/excel/care-fee-register',
    useMonth: true
  },
  {
    key: 'deposit-ledger',
    title: '押金台账',
    desc: '每位长者的应缴标准、已缴、应缴差额、已扣已退与在押余额。',
    columns: '长者 / 护理等级 / 房间床位 / 应缴标准 / 已缴 / 应缴差额 / 已扣 / 已退 / 在押余额 / 状态',
    path: '/api/finance/report/excel/deposit-ledger',
    useMonth: false,
    useDepositStatus: true
  },
  {
    key: 'electricity-monthly',
    title: '电费月报',
    desc: '按房间列出上期与本期读数、用电量、电价、电费与分摊方式。',
    columns: '楼栋 / 楼层 / 房间 / 上期读数 / 本期读数 / 用电量 / 电价 / 电费 / 分摊方式 / 分摊人数 / 人均 / 缴费状态',
    path: '/api/finance/report/excel/electricity-monthly',
    useMonth: true
  },
  {
    key: 'overdue-collection',
    title: '欠费催缴清单',
    desc: '只列欠费未清的账单，附联系电话与押金差额，可直接拿去催缴。',
    columns: '长者 / 房间床位 / 联系电话 / 账单月份 / 应收 / 已收 / 欠费 / 押金差额 / 催缴说明',
    path: '/api/finance/report/excel/overdue-collection',
    useMonth: true
  }
]

async function download(item: ReportItem) {
  downloading.value = item.key
  try {
    const params: Record<string, string | undefined> = {}
    if (item.useMonth) {
      params.month = dayjs(query.month).format('YYYY-MM')
    }
    if (item.useDepositStatus && query.depositStatus) {
      params.status = query.depositStatus
    }
    await exportXlsxByRequest(item.path, params, item.title)
    message.success(`${item.title} 已导出`)
  } catch {
    message.error(`${item.title} 导出失败`)
  } finally {
    downloading.value = ''
  }
}
</script>

<style scoped>
.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.report-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 15px;
}

.report-card__desc {
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--text-secondary, #666);
}

.report-card__cols {
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
  line-height: 1.6;
}
</style>
