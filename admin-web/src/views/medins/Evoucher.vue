<template>
  <PageContainer title="医保电子凭证" subTitle="长者医保电子凭证绑定登记与有效性校验（令牌仅存授权引用）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="绑定状态">
        <a-select v-model:value="query.bindStatus" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option value="BOUND">已绑定</a-select-option>
          <a-select-option value="UNBOUND">已解绑</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="校验状态">
        <a-select v-model:value="query.verifyStatus" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option value="PENDING">待校验</a-select-option>
          <a-select-option value="PASSED">通过</a-select-option>
          <a-select-option value="FAILED">失败</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openBind">绑定登记</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'bindStatus'">
          <a-tag :color="record.bindStatus === 'BOUND' ? 'blue' : 'default'">
            {{ record.bindStatus === 'BOUND' ? '已绑定' : '已解绑' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'verifyStatus'">
          <a-tag :color="verifyColor(record.verifyStatus)">{{ verifyText(record.verifyStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button
              v-if="record.bindStatus === 'BOUND'"
              type="link"
              size="small"
              @click="doVerify(record)"
            >校验</a-button>
            <a-popconfirm
              v-if="record.bindStatus === 'BOUND'"
              title="确认解绑该长者的医保电子凭证？"
              @confirm="doUnbind(record)"
            >
              <a-button type="link" size="small" danger>解绑</a-button>
            </a-popconfirm>
            <a-button
              v-if="record.bindStatus === 'UNBOUND'"
              type="link"
              size="small"
              @click="openBind(record)"
            >重新绑定</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="bindOpen" title="医保电子凭证绑定登记" :confirm-loading="saving" @ok="submitBind">
      <a-form layout="vertical">
        <a-form-item label="长者" required>
          <a-select v-model:value="bindForm.elderId" show-search :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="医保号">
          <a-input v-model:value="bindForm.insuredNo" />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input v-model:value="bindForm.idCard" />
        </a-form-item>
        <a-form-item label="凭证授权令牌引用" required>
          <a-input v-model:value="bindForm.ecardToken" placeholder="电子凭证授权令牌引用，不填敏感原文" />
        </a-form-item>
        <a-form-item label="校验渠道">
          <a-select v-model:value="bindForm.channel" placeholder="默认 MOCK">
            <a-select-option v-for="c in channelOptions" :key="c.channel" :value="c.channel">
              {{ c.channel }}{{ c.regionCode ? `（${c.regionCode}）` : '' }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="bindForm.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  bindMedinsEvoucher,
  getMedinsEvoucherPage,
  verifyMedinsEvoucher,
  unbindMedinsEvoucher,
  getMedinsChannelOptions
} from '../../api/medins'
import type { MedinsChannelOption, MedinsEvoucher } from '../../api/medins'
import type { Id, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedinsEvoucher[]>([])
const query = reactive({
  elderId: undefined as Id | undefined,
  bindStatus: undefined as string | undefined,
  verifyStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })
const channelOptions = ref<MedinsChannelOption[]>([])

const columns = [
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 90 },
  { title: '医保号', dataIndex: 'insuredNo', key: 'insuredNo', width: 150 },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard', width: 170 },
  { title: '渠道', dataIndex: 'channel', key: 'channel', width: 90 },
  { title: '绑定状态', key: 'bindStatus', width: 95 },
  { title: '校验状态', key: 'verifyStatus', width: 95 },
  { title: '校验结果', dataIndex: 'verifyMessage', key: 'verifyMessage', ellipsis: true },
  { title: '最近校验', dataIndex: 'lastVerifyAt', key: 'lastVerifyAt', width: 165 },
  { title: '操作', key: 'action', width: 150 }
]

const bindOpen = ref(false)
const bindForm = reactive({
  elderId: undefined as Id | undefined,
  insuredNo: '',
  idCard: '',
  ecardToken: '',
  channel: undefined as string | undefined,
  remark: ''
})

function verifyText(s?: string) {
  return ({ PENDING: '待校验', PASSED: '通过', FAILED: '失败' } as Record<string, string>)[s || ''] || s || '-'
}
function verifyColor(s?: string) {
  if (s === 'PASSED') return 'green'
  if (s === 'FAILED') return 'red'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedinsEvoucher> = await getMedinsEvoucherPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      bindStatus: query.bindStatus,
      verifyStatus: query.verifyStatus
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.elderId = undefined
  query.bindStatus = undefined
  query.verifyStatus = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

async function openBind(record?: MedinsEvoucher) {
  bindForm.elderId = record?.elderId
  bindForm.insuredNo = record?.insuredNo || ''
  bindForm.idCard = record?.idCard || ''
  bindForm.ecardToken = record?.ecardToken || ''
  bindForm.channel = record?.channel || undefined
  bindForm.remark = record?.remark || ''
  if (!channelOptions.value.length) {
    try {
      channelOptions.value = (await getMedinsChannelOptions()) || []
    } catch {
      channelOptions.value = []
    }
  }
  bindOpen.value = true
}
async function submitBind() {
  if (!bindForm.elderId || !bindForm.ecardToken) {
    message.error('请选择长者并填写凭证授权令牌引用')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    await bindMedinsEvoucher({
      elderId: bindForm.elderId,
      insuredNo: bindForm.insuredNo || undefined,
      idCard: bindForm.idCard || undefined,
      ecardToken: bindForm.ecardToken,
      channel: bindForm.channel,
      remark: bindForm.remark || undefined
    })
    message.success('电子凭证绑定登记成功')
    bindOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function doVerify(record: MedinsEvoucher) {
  const res = await verifyMedinsEvoucher(record.id)
  if (res.verifyStatus === 'PASSED') {
    message.success('凭证校验通过')
  } else {
    message.error(`凭证校验失败：${res.verifyMessage || '未知原因'}`)
  }
  fetchData()
}

async function doUnbind(record: MedinsEvoucher) {
  await unbindMedinsEvoucher(record.id)
  message.success('已解绑')
  fetchData()
}

fetchData()
searchElders('')
</script>
