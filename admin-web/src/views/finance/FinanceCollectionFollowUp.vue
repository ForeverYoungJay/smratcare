<template>
  <PageContainer title="欠费催缴跟进" subTitle="按风险、账期和续约状态组织催缴动作">
    <div class="follow-shell">
      <a-card class="follow-hero card-elevated" :bordered="false">
        <div>
          <div class="follow-hero__eyebrow">Collection Follow-up</div>
          <h2>把欠费、低余额和续约临界放进同一张跟进清单。</h2>
          <p>每位长者只保留一行，优先看谁最需要今天联系，而不是按账单散落处理。</p>
        </div>
        <a-form layout="inline" :model="query" class="search-form">
          <a-form-item label="月份">
            <a-date-picker v-model:value="query.month" picker="month" style="width: 160px" />
          </a-form-item>
          <a-form-item label="风险等级">
            <a-select v-model:value="query.riskLevel" allow-clear style="width: 140px">
              <a-select-option value="HIGH">高风险</a-select-option>
              <a-select-option value="MEDIUM">中风险</a-select-option>
              <a-select-option value="LOW">低风险</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="跟进阶段">
            <a-select v-model:value="query.stage" allow-clear style="width: 150px">
              <a-select-option value="URGENT">立即催缴</a-select-option>
              <a-select-option value="FOLLOW_UP">本周跟进</a-select-option>
              <a-select-option value="WATCH">持续观察</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-input v-model:value="query.keyword" allow-clear placeholder="长者 / 账单ID" style="width: 220px" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="loadData">查询</a-button>
              <a-button @click="exportData">导出</a-button>
              <a-button @click="reset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[14, 14]">
          <a-col :xs="24" :xl="6"><a-card class="card-elevated meter-card" :bordered="false"><a-statistic title="跟进人数" :value="rows.length" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated meter-card" :bordered="false"><a-statistic title="欠费金额" :value="totalOutstanding" suffix="元" :precision="2" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated meter-card" :bordered="false"><a-statistic title="高风险" :value="highRiskCount" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated meter-card" :bordered="false"><a-statistic title="30天内到期合同" :value="contractDueSoonCount" /></a-card></a-col>
        </a-row>

        <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
          <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="620">
            <vxe-column field="elderName" title="长者" width="140" />
            <vxe-column field="oldestBillMonth" title="起始账期" width="110" />
            <vxe-column field="latestBillMonth" title="最近账期" width="110" />
            <vxe-column field="outstandingAmount" title="欠费金额" width="130">
              <template #default="{ row }">{{ money(row.outstandingAmount) }}</template>
            </vxe-column>
            <vxe-column field="balance" title="账户余额" width="130">
              <template #default="{ row }">{{ money(row.balance) }}</template>
            </vxe-column>
            <vxe-column field="overdueMonths" title="拖欠月数" width="100" />
            <vxe-column title="风险" width="110">
              <template #default="{ row }">
                <a-tag :color="riskColor(row.riskLevel)">{{ row.riskLevelLabel }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column title="阶段" width="110">
              <template #default="{ row }">
                <a-tag :color="stageColor(row.stage)">{{ row.stageLabel }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column title="最近跟进" width="120">
              <template #default="{ row }">
                <a-tag :color="row.latestHandleStatus ? 'cyan' : 'default'">{{ row.latestHandleStatusLabel || '未留痕' }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column field="latestOwnerName" title="责任人" width="110">
              <template #default="{ row }">{{ row.latestOwnerName || '-' }}</template>
            </vxe-column>
            <vxe-column field="promisedDate" title="承诺付款日" width="130">
              <template #default="{ row }">{{ row.promisedDate || '-' }}</template>
            </vxe-column>
            <vxe-column field="nextReminderAt" title="下次提醒" width="180">
              <template #default="{ row }">{{ row.nextReminderAt || '-' }}</template>
            </vxe-column>
            <vxe-column field="latestHandleAt" title="最近跟进时间" width="180">
              <template #default="{ row }">{{ row.latestHandleAt || '-' }}</template>
            </vxe-column>
            <vxe-column field="lastPaymentAt" title="最近收款" width="180" />
            <vxe-column field="contractExpireDate" title="合同到期" width="130" />
            <vxe-column field="followUpReason" title="跟进理由" min-width="240" />
            <vxe-column field="suggestion" title="建议动作" min-width="240" />
            <vxe-column title="操作" width="170" fixed="right">
              <template #default="{ row }">
                <a-space>
                  <a-button type="link" @click="go(row.actionPath || '/finance/bills/in-resident')">{{ row.actionLabel || '查看账单' }}</a-button>
                  <a-button type="link" @click="openFollowModal(row)">跟进</a-button>
                  <a-button v-if="row.primaryBillId" type="link" @click="go(`/finance/bill/${row.primaryBillId}`)">详情</a-button>
                </a-space>
              </template>
            </vxe-column>
          </vxe-table>
          <a-pagination
            style="margin-top: 16px; text-align: right;"
            :current="query.pageNo"
            :page-size="query.pageSize"
            :total="total"
            show-size-changer
            @change="onPageChange"
            @showSizeChange="onPageSizeChange"
          />
        </a-card>
      </StatefulBlock>
    </div>
    <a-modal v-model:open="followVisible" title="催缴跟进记录" :confirm-loading="submitLoading" @ok="submitFollow">
      <a-form layout="vertical" :model="followForm">
        <a-form-item label="跟进阶段">
          <a-select v-model:value="followForm.stage">
            <a-select-option value="URGENT">立即催缴</a-select-option>
            <a-select-option value="FOLLOW_UP">本周跟进</a-select-option>
            <a-select-option value="WATCH">持续观察</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理状态">
          <a-select v-model:value="followForm.status">
            <a-select-option value="FOLLOWING">跟进中</a-select-option>
            <a-select-option value="WAITING">待回访</a-select-option>
            <a-select-option value="DONE">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="承诺付款日">
          <a-date-picker v-model:value="followForm.promisedDate" />
        </a-form-item>
        <a-form-item label="责任人">
          <a-input v-model:value="followForm.ownerName" allow-clear />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model:value="followForm.contactName" allow-clear />
        </a-form-item>
        <a-form-item label="联系渠道">
          <a-select v-model:value="followForm.contactChannel" allow-clear>
            <a-select-option value="PHONE">电话</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="SMS">短信</a-select-option>
            <a-select-option value="ONSITE">当面沟通</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="联系结果">
          <a-input v-model:value="followForm.contactResult" allow-clear />
        </a-form-item>
        <a-form-item label="下次提醒">
          <a-date-picker v-model:value="followForm.nextReminderAt" show-time />
        </a-form-item>
        <a-form-item label="下一步动作">
          <a-input v-model:value="followForm.nextAction" allow-clear />
        </a-form-item>
        <a-form-item label="跟进备注">
          <a-textarea v-model:value="followForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
      <a-divider orientation="left">最近跟进记录</a-divider>
      <a-timeline>
        <a-timeline-item v-for="item in followLogs" :key="item.id">
          {{ item.createTime }} · {{ item.actorName || '系统' }} · {{ item.stageLabel || item.stage }} · {{ item.contactName || '未记录联系人' }} · {{ item.contactResult || item.remark || '无备注' }}
        </a-timeline-item>
      </a-timeline>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceCollectionFollowUpCsv, getFinanceCollectionFollowUpLogs, getFinanceCollectionFollowUpPage, handleFinanceCollectionFollowUp } from '../../api/finance'
import type { FinanceCollectionFollowUpItem, FinanceHandleLogItem, PageResult } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<FinanceCollectionFollowUpItem[]>([])
const total = ref(0)
const followVisible = ref(false)
const submitLoading = ref(false)
const currentRow = ref<FinanceCollectionFollowUpItem | null>(null)
const followLogs = ref<FinanceHandleLogItem[]>([])
const followForm = reactive({
  stage: 'FOLLOW_UP',
  status: 'FOLLOWING',
  promisedDate: undefined as any,
  ownerName: '',
  contactName: '',
  contactChannel: undefined as string | undefined,
  contactResult: '',
  nextReminderAt: undefined as any,
  nextAction: '',
  remark: ''
})
const query = reactive({
  month: typeof route.query.month === 'string' ? dayjs(`${route.query.month}-01`) : dayjs().startOf('month'),
  riskLevel: (typeof route.query.riskLevel === 'string' ? route.query.riskLevel : undefined) as string | undefined,
  stage: (typeof route.query.stage === 'string' ? route.query.stage : undefined) as string | undefined,
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
  pageNo: 1,
  pageSize: 20
})

const totalOutstanding = computed(() => rows.value.reduce((sum, item) => sum + Number(item.outstandingAmount || 0), 0))
const highRiskCount = computed(() => rows.value.filter(item => item.riskLevel === 'HIGH').length)
const contractDueSoonCount = computed(() => rows.value.filter(item => item.contractExpireDate && dayjs(item.contractExpireDate).diff(dayjs(query.month).endOf('month'), 'day') <= 30).length)

function go(path: string) {
  router.push(path)
}

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function riskColor(level?: string) {
  if (level === 'HIGH') return 'red'
  if (level === 'MEDIUM') return 'orange'
  return 'green'
}

function stageColor(stage?: string) {
  if (stage === 'URGENT') return 'red'
  if (stage === 'FOLLOW_UP') return 'gold'
  return 'blue'
}

async function openFollowModal(row: FinanceCollectionFollowUpItem) {
  currentRow.value = row
  followForm.stage = row.stage || 'FOLLOW_UP'
  followForm.status = row.latestHandleStatus || 'FOLLOWING'
  followForm.promisedDate = row.promisedDate ? dayjs(row.promisedDate) : undefined
  followForm.ownerName = row.latestOwnerName || ''
  followForm.contactName = row.latestContactName || ''
  followForm.contactChannel = row.latestContactChannel || undefined
  followForm.contactResult = row.latestContactResult || ''
  followForm.nextReminderAt = row.nextReminderAt ? dayjs(row.nextReminderAt) : undefined
  followForm.nextAction = ''
  followForm.remark = row.latestHandleRemark || ''
  followVisible.value = true
  followLogs.value = await getFinanceCollectionFollowUpLogs({ elderId: row.elderId, limit: 10 })
}

async function submitFollow() {
  if (!currentRow.value) return
  submitLoading.value = true
  try {
    await handleFinanceCollectionFollowUp({
      sourceModule: 'COLLECTION',
      issueType: 'FOLLOW_UP',
      elderId: currentRow.value.elderId,
      billId: currentRow.value.primaryBillId,
      stage: followForm.stage,
      status: followForm.status,
      promisedDate: followForm.promisedDate ? dayjs(followForm.promisedDate).format('YYYY-MM-DD') : undefined,
      ownerName: followForm.ownerName,
      contactName: followForm.contactName,
      contactChannel: followForm.contactChannel,
      contactResult: followForm.contactResult,
      nextReminderAt: followForm.nextReminderAt ? dayjs(followForm.nextReminderAt).format('YYYY-MM-DDTHH:mm:ss') : undefined,
      nextAction: followForm.nextAction,
      remark: followForm.remark
    })
    message.success('跟进记录已保存')
    followVisible.value = false
    loadData()
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res: PageResult<FinanceCollectionFollowUpItem> = await getFinanceCollectionFollowUpPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: dayjs(query.month).format('YYYY-MM'),
      riskLevel: query.riskLevel,
      stage: query.stage,
      keyword: query.keyword || undefined
    })
    rows.value = res.list || []
    total.value = Number(res.total || 0)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载催缴跟进列表失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function exportData() {
  try {
    await exportFinanceCollectionFollowUpCsv({
      month: dayjs(query.month).format('YYYY-MM'),
      riskLevel: query.riskLevel,
      stage: query.stage,
      keyword: query.keyword || undefined
    })
    message.success('催缴跟进清单已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function reset() {
  query.month = dayjs().startOf('month')
  query.riskLevel = undefined
  query.stage = undefined
  query.keyword = ''
  query.pageNo = 1
  loadData()
}

function onPageChange(page: number) {
  query.pageNo = page
  loadData()
}

function onPageSizeChange(_: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.follow-hero {
  display: grid;
  gap: 18px;
  grid-template-columns: 1.1fr 1fr;
  background:
    radial-gradient(circle at top right, rgba(196, 181, 253, 0.34), transparent 32%),
    linear-gradient(135deg, #102a43 0%, #1f5f8b 48%, #6fb1a0 100%);
  color: #fff;
}

.follow-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.follow-hero h2 {
  margin: 10px 0;
  font-size: 28px;
  line-height: 1.18;
}

.follow-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
}

.meter-card {
  border-radius: 18px;
}

@media (max-width: 1100px) {
  .follow-hero {
    grid-template-columns: 1fr;
  }
}
</style>
