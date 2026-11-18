<template>
  <PageContainer title="卡务管理" subTitle="申领、激活、挂失、补卡、注销；卡只是身份标识，余额与充值由财务办理">
    <template #extra>
      <a-button type="primary" @click="openCreate">开卡</a-button>
    </template>
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="关键词">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="卡号/老人姓名(编号)" width="220px" />
      </a-form-item>

    </SearchForm>

    <DataTable
      :filter-model="query"
      @clear-filters="fetchData"
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tooltip :title="record.statusReason">
            <a-tag :color="CARD_STATUS[cardStatus(record)]?.color">{{ CARD_STATUS[cardStatus(record)]?.label || record.status }}</a-tag>
          </a-tooltip>
        </template>
        <template v-else-if="column.key === 'prev'">{{ record.prevCardId ? '补卡新卡' : '' }}</template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button v-for="item in cardActions(record)" :key="item.action" type="link" size="small" :danger="item.danger" @click="openAction(record, item)">{{ item.label }}</a-button>
            <a-button v-if="!CLOSED.includes(cardStatus(record))" type="link" size="small" @click="openEdit(record)">编辑</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal :mask-closable="false" v-model:open="editOpen" :title="form.id ? '编辑卡务' : '开卡'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="form.elderId"
            show-search
            :disabled="!!form.id"
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请选择老人"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="卡号">
          <a-input v-model:value="form.cardNo" placeholder="不填则自动生成" />
        </a-form-item>
        <a-form-item v-if="!form.id" label="开卡方式">
          <a-radio-group v-model:value="form.status">
            <a-radio value="PENDING">申领（发卡后再激活）</a-radio>
            <a-radio value="ACTIVE">直接激活</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="actionOpen" :title="`${pendingAction?.label || ''} · ${actionCard?.elderName || ''} ${actionCard?.cardNo || ''}`" :confirm-loading="saving" @ok="submitAction">
      <p v-if="pendingAction?.action === 'REISSUE'">补卡后原卡作废并与新卡关联，新卡需激活后使用；余额在长者资金账户中，不随卡变动。</p>
      <a-form layout="vertical">
        <a-form-item v-if="pendingAction?.action === 'REISSUE'" label="新卡号">
          <a-input v-model:value="actionForm.newCardNo" placeholder="不填则自动生成" />
        </a-form-item>
        <a-form-item :label="pendingAction?.needReason ? '原因（必填）' : '原因'">
          <a-input v-model:value="actionForm.reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { message } from 'ant-design-vue'
import { getCardAccountPage, createCardAccount, updateCardAccount, cardLifecycle } from '../../api/card'
import { CARD_STATUS, cardActions, cardStatus } from '../oa/center/adminTabs'
import type { CardAccount, Id, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<CardAccount[]>([])
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 200 })
const query = reactive({ status: undefined as string | undefined, keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '卡号', dataIndex: 'cardNo', key: 'cardNo', width: 180 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 100 },
  { title: '信用额度', dataIndex: 'creditLimit', key: 'creditLimit', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '', key: 'prev', width: 90 },
  { title: '操作', key: 'action', width: 200 }
]

const CLOSED = ['REPLACED', 'CANCELLED']
const statusOptions = Object.entries(CARD_STATUS).map(([value, item]) => ({ value, label: item.label }))
type CardAction = ReturnType<typeof cardActions>[number]
const actionOpen = ref(false)
const actionCard = ref<CardAccount>()
const pendingAction = ref<CardAction>()
const actionForm = reactive({ reason: '', newCardNo: '' })

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as Id | undefined,
  elderId: undefined as Id | undefined,
  cardNo: '',
  status: 'PENDING',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<CardAccount> = await getCardAccountPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.cardNo = ''
  form.status = 'PENDING'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: CardAccount) {
  ensureSelectedElder(record.elderId, record.elderName)
  form.id = record.id
  form.elderId = record.elderId
  form.cardNo = record.cardNo
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    elderId: form.elderId,
    cardNo: form.cardNo || undefined,
    status: form.status,
    remark: form.remark
  }
  if (saving.value) return
  saving.value = true
  try {
    if (form.id) {
      await updateCardAccount(form.id, payload)
    } else {
      await createCardAccount(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function openAction(record: CardAccount, item: CardAction) {
  actionCard.value = record
  pendingAction.value = item
  actionForm.reason = ''
  actionForm.newCardNo = ''
  actionOpen.value = true
}

async function submitAction() {
  if (!actionCard.value || !pendingAction.value) return
  if (pendingAction.value.needReason && !actionForm.reason.trim()) {
    message.warning('请填写原因')
    return
  }
  saving.value = true
  try {
    const card = await cardLifecycle(actionCard.value.id, { action: pendingAction.value.action, reason: actionForm.reason || undefined, newCardNo: actionForm.newCardNo || undefined })
    message.success(pendingAction.value.action === 'REISSUE' ? `已补卡，新卡号 ${card.cardNo}，请激活后使用` : `${pendingAction.value.label}成功`)
    actionOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

searchElders('')
fetchData()

useLiveSyncRefresh({
  topics: ['finance', 'elder', 'system'],
  refresh: () => {
    if (loading.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>
