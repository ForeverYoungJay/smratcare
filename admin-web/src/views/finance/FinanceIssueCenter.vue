<template>
  <PageContainer title="异常修复中心" subTitle="统一承接对账、一致性巡检、催缴异常与退款修正">
    <div class="issue-shell">
      <a-card class="issue-hero card-elevated" :bordered="false">
        <div>
          <div class="issue-hero__eyebrow">Issue Control</div>
          <h2>把分散在多个菜单里的财务异常压平到一个队列里。</h2>
          <p>按风险和来源聚焦处理，减少“先找问题在哪个页面”的切换成本。</p>
        </div>
        <a-form layout="inline" :model="query" class="search-form">
          <a-form-item label="日期">
            <a-date-picker v-model:value="query.date" />
          </a-form-item>
          <a-form-item label="风险等级">
            <a-select v-model:value="query.riskLevel" allow-clear style="width: 140px">
              <a-select-option value="HIGH">高风险</a-select-option>
              <a-select-option value="MEDIUM">中风险</a-select-option>
              <a-select-option value="LOW">低风险</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="来源">
            <a-select v-model:value="query.sourceModule" allow-clear style="width: 160px">
              <a-select-option value="LEDGER">一致性巡检</a-select-option>
              <a-select-option value="RECONCILE">对账中心</a-select-option>
              <a-select-option value="AUTO_DEBIT">催缴异常</a-select-option>
              <a-select-option value="PAYMENT_ADJUSTMENT">退款/冲正</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-input v-model:value="query.keyword" allow-clear placeholder="长者 / 账单ID / 描述" style="width: 220px" />
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
          <a-col :xs="24" :xl="6"><a-card class="card-elevated stat-slab" :bordered="false"><a-statistic title="当前异常数" :value="rows.length" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated stat-slab" :bordered="false"><a-statistic title="高风险" :value="highRiskCount" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated stat-slab" :bordered="false"><a-statistic title="涉及金额" :value="totalAmount" suffix="元" :precision="2" /></a-card></a-col>
          <a-col :xs="24" :xl="6"><a-card class="card-elevated stat-slab" :bordered="false"><a-statistic title="需立即处理" :value="urgentCount" /></a-card></a-col>
        </a-row>

        <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
          <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="620">
            <vxe-column field="occurredAt" title="发生时间" width="180" />
            <vxe-column field="sourceModuleLabel" title="来源模块" width="130">
              <template #default="{ row }">
                <a-tag :color="sourceColor(row.sourceModule)">{{ row.sourceModuleLabel }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column field="issueTypeLabel" title="异常类型" width="170" />
            <vxe-column title="风险等级" width="110">
              <template #default="{ row }">
                <a-tag :color="riskColor(row.riskLevel)">{{ row.riskLevelLabel }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column title="处理状态" width="120">
              <template #default="{ row }">
                <a-tag :color="row.latestHandleStatus ? 'cyan' : 'default'">{{ row.latestHandleStatusLabel || '未处理' }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column field="latestOwnerName" title="责任人" width="120">
              <template #default="{ row }">{{ row.latestOwnerName || '-' }}</template>
            </vxe-column>
            <vxe-column field="latestDueDate" title="处理时限" width="130">
              <template #default="{ row }">{{ row.latestDueDate || '-' }}</template>
            </vxe-column>
            <vxe-column field="latestHandleAt" title="最近处理时间" width="180">
              <template #default="{ row }">{{ row.latestHandleAt || '-' }}</template>
            </vxe-column>
            <vxe-column field="elderName" title="长者" width="140">
              <template #default="{ row }">{{ row.elderName || '-' }}</template>
            </vxe-column>
            <vxe-column field="amount" title="涉及金额" width="120">
              <template #default="{ row }">{{ money(row.amount) }}</template>
            </vxe-column>
            <vxe-column field="detail" title="异常描述" min-width="240" />
            <vxe-column field="suggestion" title="处理建议" min-width="240" />
            <vxe-column title="操作" width="180" fixed="right">
              <template #default="{ row }">
                <a-space>
                  <a-button type="link" @click="go(row.actionPath || '/finance/reconcile/center')">{{ row.actionLabel || '去处理' }}</a-button>
                  <a-button type="link" @click="openHandleModal(row)">留痕</a-button>
                  <a-button v-if="row.billId" type="link" @click="go(`/finance/bill/${row.billId}`)">账单</a-button>
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
    <a-modal v-model:open="handleVisible" title="异常处理留痕" :confirm-loading="submitLoading" @ok="submitHandle">
      <a-form layout="vertical" :model="handleForm">
        <a-form-item label="处理状态">
          <a-select v-model:value="handleForm.status">
            <a-select-option value="FOLLOWING">处理中</a-select-option>
            <a-select-option value="WAITING">待回访</a-select-option>
            <a-select-option value="DONE">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="下一步动作">
          <a-input v-model:value="handleForm.nextAction" allow-clear />
        </a-form-item>
        <a-form-item label="责任人">
          <a-input v-model:value="handleForm.ownerName" allow-clear />
        </a-form-item>
        <a-form-item label="处理时限">
          <a-date-picker v-model:value="handleForm.dueDate" />
        </a-form-item>
        <a-form-item label="复核结论">
          <a-input v-model:value="handleForm.reviewResult" allow-clear />
        </a-form-item>
        <a-form-item label="处理备注">
          <a-textarea v-model:value="handleForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
      <a-divider orientation="left">最近处理记录</a-divider>
      <a-timeline>
        <a-timeline-item v-for="item in handleLogs" :key="item.id">
          {{ item.createTime }} · {{ item.actorName || '系统' }} · {{ item.statusLabel || item.status }} · {{ item.ownerName || '未分派' }} · {{ item.reviewResult || item.remark || '无备注' }}
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
import { exportFinanceIssueCenterCsv, getFinanceIssueCenterPage, getFinanceIssueHandleLogs, handleFinanceIssueCenter } from '../../api/finance'
import type { FinanceHandleLogItem, FinanceIssueCenterItem, PageResult } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<FinanceIssueCenterItem[]>([])
const total = ref(0)
const handleVisible = ref(false)
const submitLoading = ref(false)
const currentRow = ref<FinanceIssueCenterItem | null>(null)
const handleLogs = ref<FinanceHandleLogItem[]>([])
const handleForm = reactive({
  status: 'FOLLOWING',
  nextAction: '',
  ownerName: '',
  dueDate: undefined as any,
  reviewResult: '',
  remark: ''
})
const query = reactive({
  date: route.query.date === 'today' ? dayjs() : (typeof route.query.date === 'string' ? dayjs(route.query.date) : dayjs()),
  riskLevel: (typeof route.query.riskLevel === 'string' ? route.query.riskLevel : undefined) as string | undefined,
  sourceModule: (typeof route.query.sourceModule === 'string' ? route.query.sourceModule : undefined) as string | undefined,
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
  pageNo: 1,
  pageSize: 20
})

const highRiskCount = computed(() => rows.value.filter(item => item.riskLevel === 'HIGH').length)
const urgentCount = computed(() => rows.value.filter(item => item.riskLevel === 'HIGH' || item.sourceModule === 'LEDGER').length)
const totalAmount = computed(() => rows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

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

function sourceColor(source?: string) {
  if (source === 'LEDGER') return 'blue'
  if (source === 'RECONCILE') return 'purple'
  if (source === 'AUTO_DEBIT') return 'gold'
  return 'volcano'
}

async function openHandleModal(row: FinanceIssueCenterItem) {
  currentRow.value = row
  handleForm.status = row.latestHandleStatus || 'FOLLOWING'
  handleForm.nextAction = ''
  handleForm.ownerName = row.latestOwnerName || ''
  handleForm.dueDate = row.latestDueDate ? dayjs(row.latestDueDate) : undefined
  handleForm.reviewResult = row.latestReviewResult || ''
  handleForm.remark = row.latestHandleRemark || ''
  handleVisible.value = true
  handleLogs.value = await getFinanceIssueHandleLogs({
    billId: row.billId,
    paymentId: row.paymentId,
    elderId: row.elderId,
    sourceModule: row.sourceModule,
    limit: 10
  })
}

async function submitHandle() {
  if (!currentRow.value) return
  submitLoading.value = true
  try {
    await handleFinanceIssueCenter({
      sourceModule: currentRow.value.sourceModule,
      issueType: currentRow.value.issueType,
      billId: currentRow.value.billId,
      paymentId: currentRow.value.paymentId,
      elderId: currentRow.value.elderId,
      status: handleForm.status,
      nextAction: handleForm.nextAction,
      ownerName: handleForm.ownerName,
      dueDate: handleForm.dueDate ? dayjs(handleForm.dueDate).format('YYYY-MM-DD') : undefined,
      reviewResult: handleForm.reviewResult,
      remark: handleForm.remark
    })
    message.success('处理留痕已保存')
    handleVisible.value = false
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
    const res: PageResult<FinanceIssueCenterItem> = await getFinanceIssueCenterPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      date: dayjs(query.date).format('YYYY-MM-DD'),
      riskLevel: query.riskLevel,
      sourceModule: query.sourceModule,
      keyword: query.keyword || undefined
    })
    rows.value = res.list || []
    total.value = Number(res.total || 0)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载异常修复中心失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function exportData() {
  try {
    await exportFinanceIssueCenterCsv({
      date: dayjs(query.date).format('YYYY-MM-DD'),
      riskLevel: query.riskLevel,
      sourceModule: query.sourceModule,
      keyword: query.keyword || undefined
    })
    message.success('异常修复中心已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function reset() {
  query.date = dayjs()
  query.riskLevel = undefined
  query.sourceModule = undefined
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
.issue-shell {
  --issue-ink: #0f172a;
  --issue-red: #b42318;
}

.issue-hero {
  display: grid;
  gap: 18px;
  grid-template-columns: 1.1fr 1fr;
  background:
    linear-gradient(120deg, rgba(180, 35, 24, 0.9), rgba(15, 23, 42, 0.96)),
    linear-gradient(180deg, #fff, #fff);
  color: #fff;
}

.issue-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.issue-hero h2 {
  margin: 10px 0 10px;
  font-size: 28px;
  line-height: 1.18;
}

.issue-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
}

.stat-slab {
  border-radius: 18px;
}

@media (max-width: 1100px) {
  .issue-hero {
    grid-template-columns: 1fr;
  }
}
</style>
